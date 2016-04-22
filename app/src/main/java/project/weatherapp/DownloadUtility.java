package project.weatherapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;

/**
 * Created by Sampath on 2/11/2016.
 * Source: Prof. Kevin Du, Syracuse University
 */
public class DownloadUtility {
    // Download an image from online
    public static Bitmap downloadImage(String url) {
        Bitmap bitmap = null;
        try {
            InputStream stream = getHttpConnection(url);
            if (stream != null) {

                try {
                    bitmap = BitmapFactory.decodeStream(stream);
                    stream.close();
                } catch (IOException e1) {
                    Log.d("MyDebugMsg", "IOException in downloadImage()");
                    e1.printStackTrace();
                } catch (Exception e3) {
                    Log.d("Debug Bitmap", "Error");
                    e3.printStackTrace();
                }
            }
        } catch (UnknownHostException e2) {
            Log.d("Debug HTTP", "UnknownHostException in downloadImage()");
            e2.printStackTrace();
        }

        return bitmap;
    }

    // Download a Json file from online
    public static String downloadJSON(String url) throws UnknownHostException {
        String json=null, line;

        try {
            InputStream stream = getHttpConnection(url);
            if (stream != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                StringBuilder out = new StringBuilder();
                try {
                    while ((line = reader.readLine()) != null) {
                        out.append(line);
                    }
                    reader.close();
                    json = out.toString();
                } catch (IOException ex) {
                    Log.d("MyDebugMsg", "IOException in downloadJSON()");
                    ex.printStackTrace();
                }
            }
        } catch (UnknownHostException e1) {
            throw e1;
        }
        return json;
    }

    // Upload Json data to server
    public static void uploadJSON(String url,JSONObject jsonParam) {
        String line;
        try {
            URL urlObj = new URL(url);
            HttpURLConnection httpConnection = (HttpURLConnection) urlObj.openConnection();
            httpConnection.setDoOutput(true);
            httpConnection.setRequestMethod("POST");
            httpConnection.setRequestProperty("Content-Type", "application/json");
            httpConnection.connect();
            OutputStreamWriter out = new   OutputStreamWriter(httpConnection.getOutputStream());
            out.write(jsonParam.toString());
            out.close();
            // Read the response, if needed
            if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream stream = httpConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                while ((line = reader.readLine()) != null) {
                    Log.d("Uploading Finished", line); // for debugging purpose
                }
                reader.close();
            }
        }  catch (UnknownHostException e1) {
            Log.d("MyDebugMsg", "Unknown Host exception in uploadJSON()");
            e1.printStackTrace();
        } catch (Exception ex) {
            Log.d("MyDebugMsg", "Exception in uploadJSON()");
            ex.printStackTrace();
        }
    }

    public static String loadJSONFromAsset(Context context, String fileName) {
        String json = null, line;
        try {
            InputStream stream = context.getAssets().open(fileName);
            if (stream != null) {

                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                StringBuilder out = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    out.append(line);
                }
                reader.close();
                json = out.toString();
            }
        } catch (IOException ex) {
            Log.d("MyDebugMsg", "IOException in loadJSONFromAsset()");
            ex.printStackTrace();
        }
        return json;
    }

    // Makes HttpURLConnection and returns InputStream
    public static InputStream getHttpConnection(String urlString) throws UnknownHostException {
        InputStream stream = null;
        try {
            //Log.d("MyDebugMsg url", urlString);
            URL url = new URL(urlString);
            HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setRequestMethod("GET");
            httpConnection.connect();
            if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                stream = httpConnection.getInputStream();
            }
        }  catch (UnknownHostException e1) {
            throw e1;
        } catch (Exception ex) {
            Log.d("MyDebugMsg", "Exception in getHttpConnection()");
            ex.printStackTrace();
        }
        return stream;
    }
}
