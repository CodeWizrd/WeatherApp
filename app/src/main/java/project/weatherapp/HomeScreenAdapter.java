package project.weatherapp;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.LruCache;
import android.util.Pair;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Sampath on 2/10/2016.
 */
public class HomeScreenAdapter extends RecyclerView.Adapter<HomeScreenAdapter.ViewHolder> {

    OnItemClickListener itemClickListener;
    private Context context;
    JSONObject dataSource;
    WeatherData weatherData;
    final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
    LruCache<String, Bitmap> imgMemoryCache;
    DailyForecastAdapter dailyForecastAdapter;
    HourlyForecastAdapter hourlyForecastAdapter;
    DetailsAdapter detailsAdapter;

    public interface OnItemClickListener {
        void onClick(View v, int position);
    }

    public HomeScreenAdapter(Context context, JSONObject dataSource,
                             LruCache<String, Bitmap> imageMemeorycache) {
        this.context = context;
        this.dataSource = dataSource;
        weatherData = new WeatherData();

        imgMemoryCache = imageMemeorycache;
        dailyForecastAdapter = new DailyForecastAdapter(context, weatherData.dailyForecast,
                imgMemoryCache);
        hourlyForecastAdapter = new HourlyForecastAdapter(context, weatherData.hourlyForecast,
                imageMemeorycache);
        detailsAdapter = new DetailsAdapter(context, weatherData.currentDetails, imageMemeorycache);
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public HomeScreenAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;

        switch (viewType) {
            case 0:
                v = setupMainScreen(parent);
                break;
            case 1:
                v = setupDetailsCard(parent);
                break;
            case 2:
                v = setupForecastCard(parent);
                break;
            default:
                v = setupMainScreen(parent);
                break;
        }

        ViewHolder vh = new ViewHolder(v, viewType);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        if (dataSource != null) {
            if (position == 0) {
                holder.bindHomeScreen(weatherData.currentConditions);
            } else if (position == 1) {
                holder.bindDetails(weatherData.currentDetails);
            } else if (position == 2) {
                holder.bindForecast(weatherData.dailyForecast, weatherData.hourlyForecast);
            }

        }

        //holder.bindData();
    }

    @Override
    public int getItemCount() {
        return 3;
    }

    public void processDataSource() {
        weatherData.setDataSource(dataSource);
        weatherData.processDataSource();
        populateRecyclerviews();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private View view;
        private int viewType;

        public ViewHolder(View v, int ViewType) {
            super(v);
            view = v;
            viewType = ViewType;

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (itemClickListener != null) {
                        //itemClickListener.OnItemClick(v, getLayoutPosition());
                    }
                }
            });
        }

        private void bindHomeScreen(HashMap data) {
            TextView temp = (TextView)view.findViewById(R.id.maintemp);
            if (data.size() > 0) {
                if (DisplayWeatherActivity.metric) {
                    temp.setText(data.get("temp_c").toString());
                } else {
                    temp.setText(data.get("temp_f").toString());
                }
            }
        }

        private void bindForecast(List<HashMap> daily, List<HashMap> hourly) {
            dailyForecastAdapter.dataset = daily;
            dailyForecastAdapter.notifyDataSetChanged();
            hourlyForecastAdapter.dataset = hourly;
            hourlyForecastAdapter.notifyDataSetChanged();
        }

        private void bindDetails(List<Pair> details) {
            detailsAdapter.dataset = details;
            detailsAdapter.notifyDataSetChanged();
        }

    }

    private void populateRecyclerviews() {
        dailyForecastAdapter.dataset = weatherData.dailyForecast;
        dailyForecastAdapter.notifyDataSetChanged();
        hourlyForecastAdapter.dataset = weatherData.hourlyForecast;
        hourlyForecastAdapter.notifyDataSetChanged();
    }


    public void setOnItemClickListener(final OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    /* Private Helpers */

    private View setupMainScreen(ViewGroup parent) {
        View v = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.homescren, parent, false);

        RelativeLayout container = (RelativeLayout)v.findViewById(R.id.homescreen);
        /*
        Point size = getDisplaySize(parent);
        if (container != null) {
            container.setLayoutParams(new CardView.LayoutParams(
                    (int) (0.95 * size.x),
                    (int) (0.95 * size.y)
            ));
        }
        */

        if (container != null) {
            container.setLayoutParams(new CardView.LayoutParams(
                    parent.getWidth(),
                    parent.getHeight()
            ));
        }
        v.setPadding(10, 10, 10, 10);

        return v;
    }

    private Point getDisplaySize(ViewGroup parent) {
        WindowManager wm = (WindowManager) parent.getContext().
                getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        TypedValue tv = new TypedValue();
        int appBarHeight = 0;
        // Retrieve height of app bar
        if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
        {
            appBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, context.getResources().getDisplayMetrics());
        }
        // Calculate display height of view minus appbar
        size.y = parent.getHeight();
        //size. y -= appBarHeight;

        return size;
    }

    /*TO DO: calculate height of forecast card
    private void getForecastCardSize(ViewGroup parent) {

    }*/

    private View setupForecastCard(ViewGroup parent) {

        View v = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.forecast_card, parent, false);
        CardView forecastCard = (CardView)v.findViewById(R.id.fcast_card);
        if (forecastCard != null) {
            forecastCard.setLayoutParams(new CardView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));

            RecyclerView hourly = (RecyclerView) v.findViewById(R.id.hourly_rclview);
            hourly.setLayoutManager(new LinearLayoutManager(context,
                    LinearLayoutManager.HORIZONTAL, false));
            hourly.setAdapter(hourlyForecastAdapter);

            RecyclerView daily = (RecyclerView) v.findViewById(R.id.daily_rclview);
            daily.setLayoutManager(new LinearLayoutManager(context));
            daily.setAdapter(dailyForecastAdapter);
        }
        return v;
    }

    private View setupDetailsCard(ViewGroup parent) {
        View v = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.currentdetails_card, parent, false);
        CardView curCard = (CardView)v.findViewById(R.id.details_card);
        if (curCard != null) {
            curCard.setLayoutParams(new CardView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));

            RecyclerView details = (RecyclerView) v.findViewById(R.id.details_rclview);
            details.setLayoutManager(new LinearLayoutManager(context));
            details.setAdapter(detailsAdapter);
        }
        return v;
    }
}
