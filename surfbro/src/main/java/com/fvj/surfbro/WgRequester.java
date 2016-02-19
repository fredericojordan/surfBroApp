package com.fvj.surfbro;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.Calendar;
import java.util.Iterator;

public class WgRequester extends AsyncTask<Integer, Void, Forecast> {

    private static final String TAG = "WgRequester";
    private static final String NO_FORECAST = "No forecast!";
    private static final String URL_ROOT = "http://www.windguru.cz/pt/index.php?sc=";
    private static final int MAX_RETRIES = 5;

    public AsyncResponse delegate = null;

    private Context mContext;

    public WgRequester(Context context) {
        this.mContext = context;
    }

    protected Forecast doInBackground(Integer... locationId) {
        return getForecast(locationId[0]);
    }

    protected Forecast getForecast(Integer locationId) { // FIXME return value when broken
        JSONObject forecastData = null;
        int reconnections = 0;

        while ( forecastData == null ) {
            Log.d(TAG, String.format("Connection attempt #%d - locationID:%d", ++reconnections, locationId));
            String url = String.format("%s%d", URL_ROOT, locationId);
            forecastData = requestForecastData(url);
            if ( reconnections > MAX_RETRIES ) {
                Toast.makeText(mContext, NO_FORECAST, Toast.LENGTH_SHORT).show();
                break;
            }
        }

        return new Forecast(forecastData, Calendar.getInstance());
    }



    protected JSONObject requestForecastData(String url) {
        try {
            Document doc = Jsoup.connect(url).get();
            String data = doc.select("div[id=div_wgfcst1]").select("script").html();
            Log.d(TAG, String.format("Forecast data length = %d", data.length()));
            Log.d(TAG, data);
            return parseJsonForecast(data);
        } catch (Exception e) {
            if (e.getMessage() != null) Log.e(TAG, e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    protected JSONObject parseJsonForecast(String str) {
        String capped_string = removeExtraTags(str);

        try {
            JSONObject fullData = new JSONObject(capped_string);
            if (fullData.has("fcst")) {
                JSONObject forecast = fullData.getJSONObject("fcst");
                Iterator<String> it = forecast.keys();
                while (it.hasNext()) {
                    String element = it.next();
                    if (forecast.getJSONObject(element).has("HTSGW")) {
                        return forecast.getJSONObject(element);
                    }
                }
            }
        } catch (Exception e) {
            if (e.getMessage() != null) Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    protected String removeExtraTags(String str) {
        int start_index = str.indexOf("=") + 2;
        int end_index = str.indexOf("var wgopts_1");

        String res = str.substring(start_index, end_index);
        Log.d(TAG, res);
        return res;
    }

    protected void onPostExecute(Forecast forecast) {
        if ( forecast.forecastData != null ) {
            delegate.processFinish(mContext, forecast);
        }
    }
}