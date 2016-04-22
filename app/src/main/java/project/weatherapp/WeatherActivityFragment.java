package project.weatherapp;

//TO DO: v4 fragment vs native fragment?

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Locale;


public class WeatherActivityFragment extends Fragment implements CityPagerAdapter.Updateable {

    HomeScreenAdapter homeScreenAdapter;
    WeatherDataSource weatherDataSource;
    LruCache<String, Bitmap> imgMemoryCache;
    Bitmap background;
    fragmentInteractionListener fInteractionListener;
    public String WeatherServer = "http://api.wunderground.com/api/1d90ae948de2a0af/geolookup/conditions/forecast10day/hourly/q/";
    int position;
    String cityID;
    boolean current;
    Toolbar toolbar_bottom;
    ActionMenuView actionMenuView;
    ImageButton left, right;
    View thisView;

    public static WeatherActivityFragment newInstance(int position) {
        WeatherActivityFragment fragment = new WeatherActivityFragment();
        Bundle args = new Bundle();
        args.putBoolean("getData", true);
        if (LocationData.cities.length > 0) {
            fragment.position = position;
            fragment.cityID = LocationData.cities[position].id;
            fragment.current = LocationData.cities[position].current;
            if (fragment.current) {
                //fragment.WeatherServer += fragment.getLastLocation() + ".json";
            } else {
                fragment.WeatherServer += fragment.cityID + ".json";
            }
        }
        fragment.setArguments(args);
        return fragment;
    }

    public WeatherActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);

        if (imgMemoryCache == null) {
            final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
            imgMemoryCache = new LruCache<String, Bitmap>((int) maxMemory / 8) {
                @Override
                protected int sizeOf(String key, Bitmap bitmap) {
                    return bitmap.getByteCount() / 1024;
                }
            };
        }
        //refresh every half hour
        if (savedInstanceState == null) {
            final Handler handler = new Handler(Looper.getMainLooper());
            final Runnable r = new Runnable() {
                public void run() {
                    refreshData();
                    handler.postDelayed(this, (30 * 60 * 1000));
                }
            };
            handler.postDelayed(r, (30 * 60 * 1000));
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        try {
            fInteractionListener = (fragmentInteractionListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() + "must implement fragmentInteractionListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weather, container, false);
        thisView = view;
        setupBottomToolbar(view);

        boolean reloadData = true;
        if (savedInstanceState != null)
            reloadData = savedInstanceState.getBoolean("getData");
        if (reloadData) {
            if (current && getLastLocation().equals("")) {
                WeatherServer += getLastLocation() + ".json";
            } else {
                WeatherServer += cityID + ".json";
            }

            weatherDataSource = new WeatherDataSource();
            setupAdapters(view);
            refreshData();
        } else {
            setupAdapters(view);
            loadOldData();
        }
        loadBackgroundImage(view);
        setPageScrollers(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        setPageScrollers(null);
        datasetChanged();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        thisView = view;
    }

    @Override
    public void setUserVisibleHint(boolean visible) {
        super.setUserVisibleHint(visible);
        setPageScrollers(null);
        if (visible && isResumed()) {
            setPageScrollers(null);
            if (DisplayWeatherActivity.metric) {
                actionMenuView.getMenu().findItem(R.id.action_units1).setTitle("Imperial Units (°F)");
                actionMenuView.getMenu().findItem(R.id.action_units1).setTitleCondensed("°F");
            } else {
                actionMenuView.getMenu().findItem(R.id.action_units1).setTitle("Metric Units (°C)");
                actionMenuView.getMenu().findItem(R.id.action_units1).setTitleCondensed("°C");
            }
            datasetChanged();
        }
    }

    void setupBottomToolbar(View view) {
        toolbar_bottom = (Toolbar) view.findViewById(R.id.toolbar_bottom);
        actionMenuView = (ActionMenuView) toolbar_bottom.findViewById(R.id.toolbar_bottom_menu);
        actionMenuView.getMenu().clear();
        getActivity().getMenuInflater().inflate(R.menu.menu_bottom, actionMenuView.getMenu());

        actionMenuView.setOnMenuItemClickListener(new ActionMenuView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_refresh1:
                        if (current) {
                            if (fInteractionListener != null) {
                                fInteractionListener.checkForLocUpdt();
                            }
                        }
                        refreshData();
                        break;
                    case R.id.action_units1:
                        Log.d("bottom menu", "units");
                        changeUnits(item);
                        break;
                    case R.id.action_manage:
                        fInteractionListener.goToLocMgr();
                        break;
                }
                return true;
            }
        });
    }

    void setPageScrollers(View view) {
        if (thisView == null) {
            thisView = getView();
        }
        if (view == null) {
            view = thisView;
        }
        if (view != null) {
            if (left == null) left = (ImageButton) view.findViewById(R.id.button_left_page);
            if (right == null) right = (ImageButton) view.findViewById(R.id.button_right_page);
        }
        if (left != null && right != null) {
            if (position == 0) left.setVisibility(View.INVISIBLE);
            if (position == LocationData.cities.length - 1) right.setVisibility(View.INVISIBLE);
            if (!left.hasOnClickListeners()) {
                left.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d("leftpage", "onclick");
                        fInteractionListener.scrollLeft();
                    }
                });
            }
            if (!right.hasOnClickListeners()) {
                right.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d("rightpage", "onclick");
                        fInteractionListener.scrollRight();
                    }
                });
            }
        }
    }

    String getLastLocation() {
        final LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
        Location loc;
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        } else {
            loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        if (loc != null)
            return String.format(loc.getLatitude() + "," + loc.getLongitude());
        else return "";
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("getData", false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // ...
        if (savedInstanceState != null) {
            //Restore the fragment's state here
        }
    }

    private void setupAdapters(View view) {
        RecyclerView homeScreenView = (RecyclerView)view.findViewById(R.id.home_rclview);
        homeScreenView.setLayoutManager(new LinearLayoutManager(getActivity()));
        homeScreenView.setHasFixedSize(true);
        if (weatherDataSource == null) weatherDataSource = new WeatherDataSource();
        homeScreenAdapter = new HomeScreenAdapter(getActivity().getApplicationContext(),
                weatherDataSource.getData(), imgMemoryCache);
        homeScreenView.setAdapter(homeScreenAdapter);
        homeScreenAdapter.notifyDataSetChanged();
        setPageScrollers(view);
    }

    public void refreshData() {
        weatherDataSource = new WeatherDataSource();
        if (current) refreshLocation();
        else {
            DownloadJSONAsyncTask task = new DownloadJSONAsyncTask(homeScreenAdapter);
            task.execute(WeatherServer);
            datasetChanged();
            CityPagerAdapter.setTitle(position);
            Log.d("refresh", "Data refreshed");
        }
        setPageScrollers(null);
    }

    public void refreshLocation() {
        if (current) {
            //refresh location on googleapiclient. How?


            weatherDataSource = new WeatherDataSource();
            WeatherServer = WeatherDataSource.WeatherServer;
            DownloadJSONAsyncTask task = new DownloadJSONAsyncTask(homeScreenAdapter);
            if (task != null) task.execute(WeatherServer);
            datasetChanged();
            CityPagerAdapter.setTitle(position);
            Log.d("refresh", "Data refreshed");
        }
        setPageScrollers(null);
    }

    public void datasetChanged() {
        if (homeScreenAdapter != null)
        homeScreenAdapter.notifyDataSetChanged();
        if (homeScreenAdapter.dailyForecastAdapter != null)
        homeScreenAdapter.dailyForecastAdapter.notifyDataSetChanged();
        if (homeScreenAdapter.hourlyForecastAdapter != null)
        homeScreenAdapter.hourlyForecastAdapter.notifyDataSetChanged();
    }

    private void loadOldData() {
        File file = new File(getActivity().getApplicationContext().getCacheDir(), "city");
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String content = "";
            for (String temp = ""; temp != null; temp = reader.readLine()) {
                content += temp;
            }
            JSONObject data = new JSONObject(content);
            homeScreenAdapter.dataSource = data;
            homeScreenAdapter.processDataSource();
        } catch (IOException e) {
            Log.d("File open", "error in reader");
        }
        catch (JSONException e) {
            Log.d("File open", "error reading json from file");
        }
        homeScreenAdapter.notifyDataSetChanged();
        homeScreenAdapter.dailyForecastAdapter.notifyDataSetChanged();
        homeScreenAdapter.hourlyForecastAdapter.notifyDataSetChanged();
    }

    private void loadBackgroundImage(View view) {
        //TO DO: save downloaded images to file.
        RelativeLayout layout = (RelativeLayout)view.findViewById(R.id.home_layout);
        try {
            if (background == null) {
                String imgUrl = "http://www.sampathjanardhan.com/img/background" + position%3 + ".jpg";
                new MyDownloadImageAsyncTask(layout).execute(imgUrl);
                background = imgMemoryCache.get("background");
            }
            layout.setBackground(new BitmapDrawable(layout.getResources(), background));
        } catch (Exception e) {
            //e.printStackTrace();
            Log.d("loadBgroundImg", "Error downloading background");
        }

    }

    private class DownloadJSONAsyncTask extends AsyncTask<String, Void, WeatherDataSource> {
        private final WeakReference<HomeScreenAdapter> adapterWeakReference;
        private int error = -1;

        public DownloadJSONAsyncTask(HomeScreenAdapter adapter) {
            adapterWeakReference = new WeakReference<HomeScreenAdapter>(adapter);
            dialog = new ProgressDialog(getContext(), ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
        }

        private ProgressDialog dialog;
        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("Loading...");
            this.dialog.setCancelable(false);
            this.dialog.setIndeterminate(true);
            this.dialog.show();
        }

        @Override
        protected WeatherDataSource doInBackground(String... urls) {
            WeatherDataSource weatherDataSource = new WeatherDataSource();
            try {
                for(String url: urls) {
                    weatherDataSource.downloadData(url);
                }
            } catch (UnknownHostException e) {
                error = 0;
            } catch (Exception e) {

            }
            return weatherDataSource;
        }

        @Override
        protected void onPostExecute(WeatherDataSource dataSource) {
            if (error == 0) {
                //TO DO: buildAlertForNoNet();
                return;
            }
            homeScreenAdapter.dataSource = dataSource.data;
            homeScreenAdapter.processDataSource();

            if (dataSource != null) {
                if (homeScreenAdapter.weatherData.city != null) {
                    LocationData.cities[position].name = homeScreenAdapter.weatherData.city;
                    if (current) {
                        CityPagerAdapter.setTitle(position);
                        LocationData.setCurntLoc(position, getLastLocation());
                    }
                }
            }

            if (adapterWeakReference != null) {
                final HomeScreenAdapter adapter =adapterWeakReference.get();
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                    adapter.dailyForecastAdapter.notifyDataSetChanged();
                    adapter.hourlyForecastAdapter.notifyDataSetChanged();
                }
            }

            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    }

    private  class MyDownloadImageAsyncTask extends AsyncTask<String, Void, Bitmap> {

        private  final WeakReference<RelativeLayout> layoutReference;
        public MyDownloadImageAsyncTask(RelativeLayout layout)
        {
            layoutReference = new WeakReference<>(layout);
            dialog = new ProgressDialog(getContext(), ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
        }

        private ProgressDialog dialog;
        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("Loading...");
            this.dialog.setCancelable(false);
            this.dialog.setIndeterminate(true);
            this.dialog.show();
        }

        @Override
        protected  Bitmap doInBackground(String... urls)
        {
            Bitmap bitmap=null;
            try {
                for (String url :urls)
                {
                    bitmap=DownloadUtility.downloadImage(url);
                    if(bitmap!=null) {
                        imgMemoryCache.put("background", bitmap);
                    }
                }
            } catch (Exception e) {
                //e.printStackTrace();
            }

            return  bitmap;
        }

        @Override
        protected  void  onPostExecute(Bitmap bitmap)
        {
            if(layoutReference != null && bitmap != null)
            {
                final  RelativeLayout layout = layoutReference.get();
                if(layout != null)
                {
                    Drawable drawable = new BitmapDrawable(layout.getResources(), bitmap);
                    layout.setBackground(drawable);
                }
                //bitmap.recycle(); bitmap = null; System.gc();

                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /*
        switch (item.getItemId()) {
            case R.id.action_more:
                break;
        }
        */
        return super.onOptionsItemSelected(item);
    }

    private void changeUnits(MenuItem item) {
        if (item.getTitleCondensed().equals("°C")) {
            DisplayWeatherActivity.metric = true;
            datasetChanged();
            item.setTitleCondensed("°F");
            item.setTitle("Imperial Units (°F)");
        } else {
            DisplayWeatherActivity.metric = false;
            datasetChanged();
            item.setTitleCondensed("°C");
            item.setTitle("Metric Units (°C)");
        }
    }

    public interface fragmentInteractionListener {
        public void setActivityTitle(String title);
        public void refreshLocation();
        //void changePage(int pos);
        void scrollLeft();
        void scrollRight();
        void checkForLocUpdt();
        void goToLocMgr();
    }

    @Override
    public void update() {
        homeScreenAdapter.notifyDataSetChanged();
        setPageScrollers(null);
        refreshData();
    }
}
