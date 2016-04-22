package project.weatherapp;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Sampath on 2/11/2016.
 */
public class WeatherDataSource {
    public static String WeatherServer = "";
    //"http://api.wunderground.com/api/1d90ae948de2a0af/geolookup/conditions/forecast10day/hourly/q/zmw:13201.1.99999.json";//6a466ddced2b1209
    public static String WeatherServerPart1 =
            "http://api.wunderground.com/api/1d90ae948de2a0af/geolookup/conditions/forecast10day/hourly/q/";
    public static String WeatherServerPart2 = ".json";
    //3a4c5dfc259788da
    //6a466ddced2b1209

    JSONObject data;
    List<Map<String, ?>> dataSource;

    public WeatherDataSource() {
        //WeatherServer = WeatherServerPart1 + "zmw:13201.1.99999" + WeatherServerPart2;
        //"43.0469"
        //if (WeatherServer.equals(""))
        //    WeatherServer = WeatherServerPart1 + "12.9715987" + "," + "77.5945627" + WeatherServerPart2;
    }

    public JSONObject getData() { return data; }

    public int getSize() { return dataSource.size(); }

    public HashMap getItem(int i) {
        if (i >=0 && i < dataSource.size()) {
            return (HashMap)dataSource.get(i);
        }
        return null;
    }

    public void downloadData(String url) throws UnknownHostException {
        try {
            String dataArray = DownloadUtility.downloadJSON(url);
            if (dataArray == null) {
                Log.d("MyDebugMsg", "Having trouble loading URL: " + url);
                return;
            }
            try {
                data = new JSONObject(dataArray);
            } catch (JSONException e) {
                Log.d("MyDebugMsg", "JSONException in downloadWeatherDataJson");
                e.printStackTrace();
            }
        } catch (UnknownHostException uHE) {
            throw uHE;
        }
    }
}
