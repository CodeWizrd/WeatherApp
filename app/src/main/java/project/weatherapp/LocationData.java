package project.weatherapp;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Sampath on 3/17/2016.
 */
public class LocationData {
    List<Map<String, ?>> locationList;
    private static File dir;
    static city[] cities;

    public List<Map<String, ?>> getLocationList() {
        refreshData();
        return locationList;
    }

    public Map<String, ?> getItem(int position) { return locationList.get(position); }

    public int getSize() {
        return locationList.size();
    }

    public LocationData(File dir) {
        LocationData.dir = dir;
        decodeCities();
        refreshData();
    }

    public void refreshData() {
        if (locationList != null) {
            locationList.clear();
        }

        HashMap item;
        locationList = new ArrayList<>();

        if (cities.length < 2) {
            item = new HashMap();
            item.put("title", "No Saved Locations");
            item.put("id", "Zilch");
            locationList.add(item);
        } else {
            for (int i = 1; i < cities.length; i++) {
                item = new HashMap();
                item.put("title", cities[i].name);
                item.put("id", cities[i].id);
                locationList.add(item);
            }
        }

    }

    class city {
        String name;
        String id;
        boolean current;

        city() {
            name = "";
            id = "";
            current = false;
        }

        city(String name1, String id1) {
            name = name1;
            id = id1;
            current = false;
        }
    }

    public static void setCurntLoc(int pos, String locId) {
        cities[pos].id = locId;
        encodeCities();
    }

    public static void removeLocation(String string) {
        city[] copy = new city[cities.length - 1];
        int j = 0;
        for (int i = 0; i < cities.length; ++i) {
            if (!(cities[i].id.equals(string))) {
                copy[j] = cities[i];
                ++j;
            }
        }
        cities = null;
        cities = copy.clone();

        encodeCities();
    }

    public void addLocation(String ID, String name) {
        city[] copy = cities.clone();
        city temp = new city(name, "zmw:" + ID);
        cities = null;
        cities = new city[copy.length+1];
        for (int i = 0; i < copy.length; ++i) {
            cities[i] = copy[i];
        }
        cities[cities.length - 1] = temp;
        encodeCities();
        decodeCities();
    }

    private static void encodeCities() {
        JSONArray array = new JSONArray();
        for(int i = 0; i < cities.length; ++i) {
            Map<String, String> temp = new HashMap<>();
            temp.put("name", cities[i].name);
            temp.put("id", cities[i].id);
            if (cities[i].current) temp.put("current", "true");
            JSONObject cityObject = new JSONObject(temp);
            try {
                array.put(i, cityObject);
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
        JSONObject root = new JSONObject();
        try {
            root.put("locations", array);
            File file = new File(dir, "pager");
            file.setWritable(true);
            BufferedWriter writer = null;

            writer = new BufferedWriter(new FileWriter(file));
            writer.write(root.toString());
            writer.close();
            Log.d("root", root.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            Log.d("File op", "Error creating file");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    JSONObject readCities() {
        JSONObject data = new JSONObject();
        try {
            File file = new File(dir, "pager");
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String content = "";
            for (String temp = ""; temp != null; temp = reader.readLine()) {
                content += temp;
            }
            data = new JSONObject(content);
            Log.d("data", data.toString());
        } catch (IOException e) {
            Log.d("File op", "Error creating file");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    void decodeCities() {
        JSONObject object = readCities();
        if (object.length() == 0)
            object = initializeCities();
        Log.d("object", object.toString());
        try {
            JSONArray array = object.getJSONArray("locations");
            cities = new city[array.length()];
            for (int i = 0; i < array.length(); ++i) {
                JSONObject temp = array.getJSONObject(i);
                cities[i] = new city(temp.getString("name"), temp.getString("id"));
                if (temp.has("current") && temp.getString("current").equals("true")) {
                    cities[i].current = true;
                }
                temp = null;
                Log.d("data cities", cities[i].name + ", " + cities[i].id + ", " + cities[i].current);
            }
        } catch (JSONException e) {
            Log.d("cities", "err reading json");
            e.printStackTrace();
        }

    }

    JSONObject initializeCities() {
        JSONObject root = new JSONObject();
        Map<String, String> cityTemp = new HashMap<>();

        cityTemp.put("name", "Current Location");
        cityTemp.put("id", "CurLoc");
        cityTemp.put("current", "true");
        JSONObject city0 = new JSONObject(cityTemp);

        cityTemp.clear();
        cityTemp.put("name", "Bangalore");
        cityTemp.put("id", "zmw:00000.1.43295");
        JSONObject city1 = new JSONObject(cityTemp);

        cityTemp.clear();
        cityTemp.put("name", "Mysore");
        cityTemp.put("id", "zmw:00000.1.43291");
        JSONObject city2 = new JSONObject(cityTemp);

        try {
            JSONArray array = new JSONArray();
            array.put(0, city0);
            array.put(1, city1);
            array.put(2, city2);

            root.put("locations", array);
            Log.d("root", root.toString());

            File file = new File(dir, "pager");
            file.delete();
            file = new File(dir, "pager");
            file.createNewFile();
            file.setWritable(true);
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(root.toString());
            writer.close();
            Log.d("root", root.toString());
        } catch (JSONException e) {
            Log.d("JSON Ex root", "malformed json");
            e.printStackTrace();
        } catch (IOException e) {
            Log.d("File op root", "Error creating file");
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return root;
    }

}
