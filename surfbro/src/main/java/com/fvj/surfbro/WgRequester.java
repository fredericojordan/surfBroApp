package com.fvj.surfbro;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.Calendar;
import java.util.Iterator;

public class WgRequester extends AsyncTask<Integer, Void, Forecast> {

    private static final String TAG = "surfBro-WgRequester";
    private static final String URL_ROOT = "https://surf-api.herokuapp.com/api/1";
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
//            String url = String.format("%s%d", URL_ROOT, locationId);
            forecastData = requestForecastData(URL_ROOT);
            if ( reconnections > MAX_RETRIES ) {
//                Toast.makeText(mContext, NO_FORECAST, Toast.LENGTH_SHORT).show(); // Can't toast on non-UI thread
                break;
            }
        }

        return new Forecast(forecastData, Calendar.getInstance());
    }



    protected JSONObject requestForecastData(String url) {
        try {
            Log.d(TAG, "Jsoup REQUEST");
            Document doc = Jsoup.connect(url).ignoreContentType(true).get();
            Log.d(TAG, "Jsoup RECEIVE");
            String data = doc.select("body").html();
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
        try {
            Log.e(TAG, str);
            JSONObject fullData = new JSONObject(str);
            JSONArray forecasts = fullData.getJSONArray("spot_forecasts");
            return forecasts.getJSONObject(0);
        } catch (Exception e) {
            if (e.getMessage() != null) Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    protected void onPostExecute(Forecast forecast) {
        if ( forecast.forecastData != null ) {
            delegate.processFinish(mContext, forecast);
        }
    }
}