package project.weatherapp;

import android.Manifest;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.File;
import java.util.HashMap;

public class DisplayWeatherActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener, WeatherActivityFragment.fragmentInteractionListener {

    ViewPager viewPager;
    CityPagerAdapter pagerAdapter;
    GoogleApiClient googleApiClient;
    Toolbar toolbar;
    public static boolean metric = false;
    public static SharedPreferences sharedPreferences;
    static File filesDir;
    private static int CURRENT_PAGE;
    // Get list of stored cities

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_weather);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean follow_me = sharedPreferences.getBoolean("follow_me", true);
        LocationData locationData = new LocationData(getFilesDir());
        filesDir = getFilesDir();

        viewPager = (ViewPager)findViewById(R.id.viewpager);
        pagerAdapter = new CityPagerAdapter(getSupportFragmentManager(),
                viewPager, toolbar, getFilesDir());
        /*
        pagerAdapter = new CityPagerAdapter(getSupportFragmentManager(),
                viewPager, toolbar, getFilesDir());
                */
        viewPager.setCurrentItem(0);
        viewPager.setOffscreenPageLimit(pagerAdapter.getCount());

        final LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        if (/*Settings.FOLLOW_ME &&*/ !locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            Log.d("GPS", "disabled");
            buildAlertForNoGps();
        } else {
            Log.d("GPS", "enabled");
        }

        //Location
        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();
    }

    public void buildAlertForNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled. Would you like to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog,
                                        @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private static final int submenu_order = 50;
    private static final int submenu_id = Menu.FIRST;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_weather, menu);

        MenuItem item = menu.findItem(R.id.action_more);
        SubMenu subMenu = item.getSubMenu();
        int k = 1;
        String[] cityNames = new String[LocationData.cities.length];
        for (int i = 0; i < cityNames.length; ++i) {
            cityNames[i] = LocationData.cities[i].name;
            subMenu.add(0, submenu_id + k, submenu_order + k, cityNames[i]);
            ++k;
        }


        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.action_plus) {
            buildDialogForAdd();
        } else {
            for (int k = 1; k <= LocationData.cities.length; ++k) {
                if (itemId == Menu.FIRST + k) {
                    // To Do: change page to k
                    viewPager.setCurrentItem(k-1);
                }
            }
        }


        return super.onOptionsItemSelected(item);
    }

    LocationRequest locationRequest;
    protected void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        //Log.d("createLocReq", "created");

        final Handler handler = new Handler(Looper.getMainLooper());
        final Runnable r = new Runnable() {
            public void run() {
                stopLocationUpdates();
                locationRequest.setInterval(1000 * 1000);
                locationRequest.setFastestInterval(500 * 1000);
                locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

                startLocationUpdates();
                //handler.postDelayed(this, (30*60*1000));
            }
        };
        handler.postDelayed(r, (10 * 1000));

    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
        //To Do: save title and data
        CURRENT_PAGE = viewPager.getCurrentItem();
    }


    @Override
    public void onResume() {
        super.onResume();
        if (googleApiClient.isConnected() /*&& !mRequestingLocationUpdates*/) {
            startLocationUpdates();
        }
        pagerAdapter.notifyDataSetChanged();
        viewPager.setCurrentItem(CURRENT_PAGE);
        CityPagerAdapter.setTitle(CURRENT_PAGE);
    }

    Location lastLocation;
    @Override
    public void onConnected(Bundle connectionHint) {

        createLocationRequest();
        startLocationUpdates();
        /*
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
        */
    }

    @Override
    public void onLocationChanged(Location location) {
        //Log.d("onLocChng", "changed");
        if (location != null && (location != lastLocation)) {
            //Log.d("onLocChng", WeatherDataSource.WeatherServer);
            if (WeatherDataSource.WeatherServer.equals("")) {
                WeatherDataSource.WeatherServer = WeatherDataSource.WeatherServerPart1 + location.getLatitude()
                        + "," + location.getLongitude() + WeatherDataSource.WeatherServerPart2;
                refreshLocation();
            }
            lastLocation = location;
        }
        Log.d("location changed", WeatherDataSource.WeatherServer);
    }

    public void startLocationUpdates() {
        //Log.d("LocUpdt", "started");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        // permission has been granted, continue as usual
        if (googleApiClient.isConnected())
        LocationServices.FusedLocationApi
                .requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    protected void stopLocationUpdates() {
        if (googleApiClient.isConnected())
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        //Log.d("LocUpdt", "stopped");
    }

    @Override
    public void onConnectionSuspended(int cause) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (mResolvingError) {
            // Already attempting to resolve an error.
            mResolvingError = true;
        } else if (result.hasResolution()) {
            try {
                mResolvingError = true;
                result.startResolutionForResult(this, result.getErrorCode());
            } catch (IntentSender.SendIntentException e) {
                googleApiClient.connect();
            }

        } else {
            final ConnectionResult res = result;
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                // Check Permissions Now
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        1);
            } else {
                // permission has been granted, continue as usual
                //Location myLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

            }
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    showErrorDialog(res.getErrorCode());
                }
            });

            mResolvingError = true;
        }
    }

    /*
     * Error handling for onConnectionFailed
     *
     */
    // The rest of this code is all about building the error dialog
    // Request code to use when launching the resolution activity
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    // Unique tag for the error dialog fragment
    private static final String DIALOG_ERROR = "dialog_error";
    // Bool to track whether the app is already resolving an error
    private boolean mResolvingError = false;
    /* Creates a dialog for an error message */
    private void showErrorDialog(int errorCode) {
        // Create a fragment for the error dialog
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
        // Pass the error that should be displayed
        Bundle args = new Bundle();
        args.putInt(DIALOG_ERROR, errorCode);
        dialogFragment.setArguments(args);
        final ErrorDialogFragment temp = dialogFragment;
        //dialogFragment.show(getFragmentManager(), "errordialog");
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                temp.show(getFragmentManager(), DIALOG_ERROR);
            }
        });
    }

    /* Called from ErrorDialogFragment when the dialog is dismissed. */
    public void onDialogDismissed() {
        mResolvingError = false;
    }

    /* A fragment to display an error dialog */
    public static class ErrorDialogFragment extends DialogFragment {
        public ErrorDialogFragment() { }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Get the error code and retrieve the appropriate dialog
            int errorCode = this.getArguments().getInt(DIALOG_ERROR);
            return GoogleApiAvailability.getInstance().getErrorDialog(
                    this.getActivity(), errorCode, REQUEST_RESOLVE_ERROR);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            ((DisplayWeatherActivity) getActivity()).onDialogDismissed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_RESOLVE_ERROR:
                mResolvingError = false;
                if (resultCode == RESULT_OK) {
                    // Make sure the app is not already connected or attempting to connect
                    if (!googleApiClient.isConnecting() &&
                            !googleApiClient.isConnected()) {
                        googleApiClient.connect();
                    }
                }
                break;

            case REQUEST_LOC_MGR:
                pagerAdapter.reloadData();
                viewPager.setCurrentItem(data.getIntExtra("PAGE", 0));
                break;
        }
    }

    //fragment interaction listener
    @Override
    public void setActivityTitle(String title) {
        if (title != null && getSupportActionBar() != null) getSupportActionBar().setTitle(title);
    }

    @Override
    public void refreshLocation() {
        if (lastLocation != null)
        CityPagerAdapter.curntLoc = String.format(lastLocation.getLatitude() + "," + lastLocation.getLongitude());
        pagerAdapter.refreshLocation();
    }

    //@Override
    public void changePage(int pos) {
        if (pos >= 0 )
        viewPager.setCurrentItem(pos, true);
    }

    @Override
    public void scrollLeft() {
        pagerAdapter.scrollLeft();
    }

    @Override
    public void scrollRight() {
        pagerAdapter.scrollRight();
    }

    @Override
    public void checkForLocUpdt() {
        stopLocationUpdates();
        createLocationRequest();
        startLocationUpdates();
    }

    private static final int REQUEST_LOC_MGR = 1005;
    @Override
    public void goToLocMgr() {
        Intent intent = new Intent(this, ManageLocation.class);
        System.gc();
        intent.putExtra("PAGE", viewPager.getCurrentItem());
        startActivityForResult(intent, REQUEST_LOC_MGR);
    }

    AutoCompleteAdapter autoCompleteAdapter;
    int listSelection;
    LocationAdapter locationAdapter;
    void buildDialogForAdd() {
        autoCompleteAdapter = new AutoCompleteAdapter(this, R.layout.autocomplete);
        locationAdapter = new LocationAdapter(this, getFilesDir());

        final android.app.AlertDialog.Builder  builder = new android.app.AlertDialog.Builder
                (this, android.app.AlertDialog.THEME_HOLO_DARK  /*THEME_DEVICE_DEFAULT_DARK*/);

        builder.setMessage("Add Location")
                .setCancelable(false);

        final AutoCompleteTextView autoComplete = new AutoCompleteTextView(this);
        autoComplete.setTextColor(getResources().getColor(R.color.colorAccent));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        autoComplete.setLayoutParams(lp);
        autoComplete.setAdapter(autoCompleteAdapter);

        //autoComplete.setAdapter(new AutoCompleteAdapter(mContext, R.layout.autocompletetextview));

        final Handler handler = new Handler();
        final Runnable r = new Runnable() {
            public void run() {
                autoCompleteAdapter.notifyDataSetChanged();
                handler.postDelayed(this, 500);
            }
        };
        handler.postDelayed(r, 500);

        autoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                pagerAdapter.notifyDataSetChanged();
                listSelection = position;
            }
        });
        builder.setView(autoComplete);
        builder.setIcon(R.drawable.ic_add_white_24dp);

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(@SuppressWarnings("unused")
                                final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                //int listSelection = autoComplete.getListSelection();
                Log.d("autocomplete listSel", Integer.toString(listSelection));
                String selection = autoCompleteAdapter.getItem(listSelection);
                int position = autoCompleteAdapter.autocompleteNamesList.indexOf(selection);
                Log.d("autocomplete position", Integer.toString(position));
                locationAdapter.locationData.addLocation(autoCompleteAdapter.autocompleteResultList.get(position),
                        autoCompleteAdapter.autocompleteNamesList.get(position));

                pagerAdapter.notifyDataSetChanged();
                pagerAdapter.reloadData();
                pagerAdapter.notifyDataSetChanged();

                handler.removeCallbacksAndMessages(null);
            }
        })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                        handler.removeCallbacksAndMessages(null);
                        handler.removeCallbacks(r);
                    }
                });
        final android.app.AlertDialog alert = builder.create();
        alert.show();
    }
}
