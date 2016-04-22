package project.weatherapp;

import android.util.Log;
import android.util.Pair;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Sampath on 2/11/2016.
 */
public class WeatherData {

    private JSONObject dataSource, locationSource, currentConditionsSource;
    private JSONArray textForecastSource, dailyForecastSource, hourlyForecastSource;

    public HashMap currentConditions;
    public List<Pair> currentDetails;
    public List<HashMap> dailyForecast, hourlyForecast, textForecast;
    public String city;

    public WeatherData() {
        currentConditions = new HashMap();
        currentDetails = new ArrayList<>();
        dailyForecast = new ArrayList<>();
        hourlyForecast = new ArrayList<>();
    }


    public void setDataSource(JSONObject source) {
        dataSource = source;
    }

    public void processDataSource() {
        if (dataSource != null) {
            try {
                currentConditionsSource = dataSource.getJSONObject("current_observation");
                dailyForecastSource = dataSource.getJSONObject("forecast").getJSONObject("simpleforecast")
                        .getJSONArray("forecastday");
                hourlyForecastSource = dataSource.getJSONArray("hourly_forecast");
                processCurrentCond();
                processCurrentDetails();
                processSimpleForecast();
                processHourlyForecast();
            } catch (JSONException e) {
                Log.d("processDataSource", "JSONException in downloadWeatherDataJson");
                e.printStackTrace();
            } catch (Exception e) {
                Log.d("processDataSource", "Exception");
                e.printStackTrace();
            }
        }
    }

    private void processCurrentCond() {
        //if (currentConditions != null)
            currentConditions.clear();
        try {
            currentConditions.put("temp_f", currentConditionsSource.getString("temp_f")  + "° F");
            currentConditions.put("temp_c", currentConditionsSource.getString("temp_c")  + "° C");
            currentConditions.put("weather", currentConditionsSource.getString("weather"));
            currentConditions.put("icon", currentConditionsSource.getString("icon"));
            currentConditions.put("feelslike_f", currentConditionsSource.getString("feelslike_f"));
            currentConditions.put("feelslike_c", currentConditionsSource.getString("feelslike_c"));
            currentConditions.put("weather", currentConditionsSource.getString("weather"));

            city = currentConditionsSource.getJSONObject("display_location").getString("full");
        } catch (JSONException e) {
            Log.d("processCurrentCond", "JSONException in downloadWeatherDataJson");
            e.printStackTrace();
        }
    }

    private void processCurrentDetails() {
        currentDetails.clear();
        try {
            currentDetails.add(new Pair<>("Temperature", currentConditionsSource.getString("temperature_string")));
            currentDetails.add(new Pair<>("Humidity", currentConditionsSource.getString("relative_humidity")));
            currentDetails.add(new Pair<>("Feels Like", currentConditionsSource.getString("feelslike_string")));
            currentDetails.add(new Pair<>("Wind", currentConditionsSource.getString("wind_string")));
            /*
            currentDetails.add(new Pair("wind_gust", currentConditionsSource.getString("wind_gust_mph") +
                    " mph (" + currentConditionsSource.getString("wind_gust_kph") + " kph)"));
                    */
            currentDetails.add(new Pair<>("Pressure", currentConditionsSource.getString("pressure_mb") +
                    " mBar (" + currentConditionsSource.getString("pressure_in") + " inches)"));
            currentDetails.add(new Pair<>("Visibility", currentConditionsSource.getString("visibility_mi") +
                    " miles (" + currentConditionsSource.getString("visibility_km") + " km)"));
            currentDetails.add(new Pair<>("weather", currentConditionsSource.getString("weather")));
        } catch (JSONException e) {
            Log.d("processCurrentCond", "JSONException in downloadWeatherDataJson");
            e.printStackTrace();
        }
    }

    private void processSimpleForecast() {
        //if (dailyForecast != null)
            dailyForecast.clear();
        try {
            for (int i = 0; i < dailyForecastSource.length(); ++i) {
                HashMap<String, String> item = new HashMap();
                JSONObject day = dailyForecastSource.getJSONObject(i);
                JSONObject date = day.getJSONObject("date");
                item.put("high_f", day.getJSONObject("high").getString("fahrenheit") + "°");
                item.put("high_c", day.getJSONObject("high").getString("celsius") + "°");
                item.put("low_f", day.getJSONObject("low").getString("fahrenheit") + "°");
                item.put("low_c", day.getJSONObject("low").getString("celsius") + "°");
                item.put("icon", day.getString("icon"));
                item.put("weekday", date.getString("weekday") + ", " +
                        date.getString("monthname_short") + " " + date.getString("day"));
                dailyForecast.add(item);
                if (i > 3) break;
            }
        } catch (JSONException e) {
            Log.d("processCurrentCond", "JSONException in downloadWeatherDataJson");
            e.printStackTrace();
        }

    }

    private void processHourlyForecast() {
        //if (hourlyForecast != null)
            hourlyForecast.clear();
        try {
            for (int i =0; i < hourlyForecastSource.length(); ++i) {
                HashMap<String, String> item = new HashMap();
                JSONObject hour = hourlyForecastSource.getJSONObject(i);
                JSONObject time = hour.getJSONObject("FCTTIME");
                item.put("temp_f", hour.getJSONObject("temp").getString("english") + "°");
                item.put("temp_c", hour.getJSONObject("temp").getString("metric") + "°");
                item.put("icon", hour.getString("icon"));
                item.put("time", time.getString("hour_padded") + ":" + time.getString("min"));
                hourlyForecast.add(item);
                if (i > 11) break;
            }
        } catch (JSONException e) {
            Log.d("processCurrentCond", "JSONException in downloadWeatherDataJson");
            e.printStackTrace();
        }
    }

    class CurrentConditions {
        JSONObject json;
        String tempF, tempC, condiitons;


    }
}
