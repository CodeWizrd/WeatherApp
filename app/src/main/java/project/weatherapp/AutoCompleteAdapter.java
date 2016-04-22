package project.weatherapp;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 * Created by Sampath on 3/17/2016.
 */

public class AutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {

    private ArrayList<String> resultList;
    ArrayList<String> autocompleteResultList;
    ArrayList<String> autocompleteNamesList;

    public AutoCompleteAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    @Override
    public int getCount() {
        if (resultList != null) {
            Log.d("mAuto ", Integer.toString(resultList.size()));
            return resultList.size();
        } else {
            Log.d("mAuto 0", "0");
            return 0;
        }
    }

    @Override
    public String getItem(int index) {
        if (resultList != null) {
            Log.d("mAuto item", resultList.get(index));
            return resultList.get(index);
        } else {
            Log.d("mAuto item", "null..");
            return null;
        }
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();

                if (constraint != null) {
                    ArrayList<String> queryResults;
                    // Retrieve the autocomplete results.
                    autocomplete(constraint.toString());
                    queryResults = autocompleteNamesList;

                    // Assign the data to the FilterResults
                    filterResults.values = queryResults;//queryResults;
                    filterResults.count = queryResults.size();//queryResults.size();
                    //notifyDataSetChanged();
                }


                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, Filter.FilterResults results) {

                resultList = (ArrayList<String>)results.values;
                if (results != null && results.count > 0) {
                    notifyDataSetChanged();
                }
                else {
                    notifyDataSetInvalidated();
                }
            }};
        return filter;
    }

    public ArrayList<String> autocomplete(String constraint) {
        ArrayList<String> list = new ArrayList<>();

        String url = "http://autocomplete.wunderground.com/aq?query=" + constraint;
        url =  url.replace(" ", "%20");
        MyDownloadJsonAsyncTask downloadJson = new MyDownloadJsonAsyncTask();
        downloadJson.execute(new String[]{url});

        return  list;
    }

    private class MyDownloadJsonAsyncTask extends AsyncTask<String, Void, String> {

        public MyDownloadJsonAsyncTask() {
        }

        @Override
        protected String doInBackground(String... urls) {
            String json = null;
            for (String url : urls) {
                Log.d("Autocomplete URL", url);
                try {
                    json = DownloadUtility.downloadJSON(url);
                } catch (UnknownHostException e1) {
                    //MainActivity.buildAlertForNoNet();
                    e1.printStackTrace();
                }
            }
            //if (json != null)
            //Log.d("AutoComplete", json.toString());
            return json;
        }

        @Override
        protected void onPostExecute(String json) {
            if (json != null) {
                try {
                    JSONObject jsonObject = new JSONObject(json);
                    JSONArray results = jsonObject.getJSONArray("RESULTS");
                    //Log.d("AutoComplete", results.toString());

                    if (autocompleteResultList != null) {
                        autocompleteResultList.clear();
                        autocompleteNamesList.clear();
                    }
                    else {
                        autocompleteResultList = new ArrayList<String>();
                        autocompleteNamesList = new ArrayList<String>();
                    }
                    for (int i = 0; i < results.length(); i++) {
                        JSONObject item = results.getJSONObject(i);
                        String id = item.getString("zmw");
                        String name = item.getString("name");
                        autocompleteNamesList.add(i, name);
                        autocompleteResultList.add(i, id);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}