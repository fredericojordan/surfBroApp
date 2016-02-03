package com.fvj.surfbro;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by fvj on 02/02/2016.
 * <p/>
 * <p/>
 * initial_timestamp = "initstamp"
 * day = "hr_d"
 * hour = "hr_h"
 * timezone_id = "tzid"
 * temperature = "TMPE"
 * wave_height = "HTSGW"
 * wave_period = "PERPW"
 * wave_direction = "DIRPW"
 * wind_speed = "WINDSPD"
 * wind_gust = "GUST"
 * wind_direction = "WINDDIR"
 */

public class WgParser extends AsyncTask<String, Void, String> {

    private static final String TAG = "WgParser";
    private static final String NO_FORECAST = "No forecast!";
    private static final int MAX_RETRIES = 3;

    private Context mContext;
    public AsyncResponse delegate = null;

    public WgParser(Context context) {
        this.mContext = context;
    }

    protected String doInBackground(String... urls) {
        return getForecast(urls);
    }

    protected String getForecast(String... urls) {
        String forecastData = null;
        int reconnections = 0;

        while ( forecastData == null ) {
            forecastData = requestForecastData(urls[0]);
            if ( ++reconnections > MAX_RETRIES ) {
                return NO_FORECAST;
            }
        }

        JSONObject forecast_json = parseJsonForecast(forecastData);
        String forecast_str = String.format("Waves: %.1fm %s\nWind: %.1fkn %s",
                getWaveHeight(forecast_json).get(0),
                parseDirection(getWaveDirection(forecast_json).get(0)),
                getWindSpeed(forecast_json).get(0),
                parseDirection(getWindDirection(forecast_json).get(0)));
        Log.d(TAG, forecast_str);
        return forecast_str;
    }

    protected ArrayList<Double> getWaveHeight(JSONObject forecast) { return getArrayFromForecast(forecast, "HTSGW"); }
    protected ArrayList<Double> getWaveDirection(JSONObject forecast) { return getArrayFromForecast(forecast, "DIRPW"); }
    protected ArrayList<Double> getWindSpeed(JSONObject forecast) { return getArrayFromForecast(forecast, "WINDSPD"); }
    protected ArrayList<Double> getWindDirection(JSONObject forecast) { return getArrayFromForecast(forecast, "WINDDIR"); }
    protected ArrayList<Double> getTemperature(JSONObject forecast) { return getArrayFromForecast(forecast, "TMPE"); }

    protected ArrayList<Double> getArrayFromForecast(JSONObject forecast, String key) {
        try {
            if (forecast.has(key)) {
                Log.d(TAG, String.format("Forecast has key {%s}", key));
                JSONArray json_array = forecast.getJSONArray(key);
                Log.d(TAG, String.format("%s: %s", key, json_array.toString()));
                return parseArray(json_array);
            } else {
                Log.w(TAG, String.format("FORECAST DOES NOT HAVE KEY {%s}", key));
                return null;
            }
        } catch (Exception e) {
            if (e.getMessage() != null) Log.e(TAG, e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    protected ArrayList<Double> parseArray(JSONArray array_in) {
        try {
            ArrayList<Double> list = new ArrayList<Double>();
            for (int i = 0; i < array_in.length(); i++) {
                if (array_in.get(i).toString().equals("null"))
                    list.add(0.0);
                else
                    list.add(array_in.getDouble(i));
            }
            return list;
        } catch (Exception e) {
            if (e.getMessage() != null) Log.e(TAG, e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    protected String requestForecastData(String url) {
        try {
            Document doc = Jsoup.connect(url).get();
            String data = doc.select("div[id=div_wgfcst1]").select("script").html();
            Log.d(TAG, String.format("Forecast data length = %d", data.length()));
            Log.d(TAG, data);
            return data;
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

    protected void onPostExecute(String str) {
        Toast.makeText(mContext, str, Toast.LENGTH_LONG).show();
        if ( !str.equals(NO_FORECAST) ) {
            delegate.processFinish(str);
        }
    }

    protected String parseDirection(double dir) {
        if (dir >= 337.5 || dir < 22.5)
            return "N";
        if (dir >= 22.5 && dir < 67.5)
            return "NE";
        if (dir >= 67.5 && dir < 112.5)
            return "E";
        if (dir >= 112.5 && dir < 157.5)
            return "SE";
        if (dir >= 157.5 && dir < 202.5)
            return "S";
        if (dir >= 202.5 && dir < 247.5)
            return "SW";
        if (dir >= 247.5 && dir < 292.5)
            return "W";
        if (dir >= 292.5 && dir < 337.5)
            return "NW";
        return "";
    }
}