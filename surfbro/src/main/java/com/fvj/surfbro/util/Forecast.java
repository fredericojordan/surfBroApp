package com.fvj.surfbro.util;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

/**
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

public class Forecast {
    private static final String TAG = "Forecast";

    public JSONObject forecastData;
    public Calendar timestamp;

    public Forecast(JSONObject forecastData, Calendar timestamp) {
        this.forecastData = forecastData;
        this.timestamp = timestamp;
    }

    public String makeWaveString() { return String.format("%.1fm", getWaveHeightNow()); }
    public String makeWaveAdditionalString() { return String.format("%.0fs", getWavePeriodNow()); }
    public String makeTemperatureString() { return String.format("%.0f \u00b0C", getTemperatureNow()); }

    public String makeWindString() {
        return String.format("%.0f-%.0fkn",
                getWindSpeedNow(),
                getWindGustSpeedNow());
    }

    public int getForecastIndex() {
        long initial_timestamp = getInitialTimestamp() * 1000;
        long now_timestamp = Calendar.getInstance().getTimeInMillis();

        int millis_diff = (int) (now_timestamp - initial_timestamp);

        double _3H_IN_MILLIS = 10800000.0;
        return (int) (millis_diff / _3H_IN_MILLIS);
    }

    public int getInitialTimestamp() { return getIntFromForecast(forecastData, "initstamp"); }
    public ArrayList<Double> getWaveHeight() { return getArrayFromForecast(forecastData, "HTSGW"); }
    public ArrayList<Double> getWaveDirection() { return getArrayFromForecast(forecastData, "DIRPW"); }
    public ArrayList<Double> getWavePeriod() { return getArrayFromForecast(forecastData, "PERPW"); }
    public ArrayList<Double> getWindSpeed() { return getArrayFromForecast(forecastData, "WINDSPD"); }
    public ArrayList<Double> getWindGustSpeed() { return getArrayFromForecast(forecastData, "GUST"); }
    public ArrayList<Double> getWindDirection() { return getArrayFromForecast(forecastData, "WINDDIR"); }
    public ArrayList<Double> getTemperature() { return getArrayFromForecast(forecastData, "TMPE"); }

    public Double getWaveHeightNow() { return getWaveHeight().get(getForecastIndex()); }
    public Double getWaveDirectionNow() { return getWaveDirection().get(getForecastIndex()); }
    public Double getWavePeriodNow() { return getWavePeriod().get(getForecastIndex()); }
    public Double getWindSpeedNow() { return getWindSpeed().get(getForecastIndex()); }
    public Double getWindGustSpeedNow() { return getWindGustSpeed().get(getForecastIndex()); }
    public Double getWindDirectionNow() { return getWindDirection().get(getForecastIndex()); }
    public Double getTemperatureNow() { return getTemperature().get(getForecastIndex()); }

    protected int getIntFromForecast(JSONObject forecast, String key) {
        try {
            if (forecast.has(key)) {
                Log.d(TAG, String.format("Forecast has key {%s}", key));
                Log.d(TAG, String.format("%s: %d", key, forecast.getInt(key)));
                return forecast.getInt(key);
            } else {
                Log.w(TAG, String.format("FORECAST DOES NOT HAVE KEY {%s}", key));
                return -1;
            }
        } catch (Exception e) {
            if (e.getMessage() != null) Log.e(TAG, e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }

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
