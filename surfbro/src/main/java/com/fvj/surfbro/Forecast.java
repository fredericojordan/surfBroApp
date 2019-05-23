package com.fvj.surfbro;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;


public class Forecast {

    private static final String TAG = "surfBro-Forecast";

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

    public ArrayList<Double> getWaveHeight() { return getArrayFromForecast(forecastData, "wave_height"); }
    public ArrayList<Double> getWaveDirection() { return getArrayFromForecast(forecastData, "wave_direction"); }
    public ArrayList<Double> getWavePeriod() { return getArrayFromForecast(forecastData, "wave_period"); }
    public ArrayList<Double> getWindSpeed() { return getArrayFromForecast(forecastData, "wind_speed"); }
    public ArrayList<Double> getWindGustSpeed() { return getArrayFromForecast(forecastData, "wind_gust"); }
    public ArrayList<Double> getWindDirection() { return getArrayFromForecast(forecastData, "wind_direction"); }
    public ArrayList<Double> getTemperature() { return getArrayFromForecast(forecastData, "temperature"); }

    public Double getWaveHeightNow() { if (getWaveHeight() == null) { return 0.0; } else { return getWaveHeight().get(0); } }
    public Double getWaveDirectionNow() { if (getWaveDirection() == null) { return 0.0; } else { return getWaveDirection().get(0); } }
    public Double getWavePeriodNow() { if (getWavePeriod() == null) { return 0.0; } else { return getWavePeriod().get(0); } }
    public Double getWindSpeedNow() { if (getWindSpeed() == null) { return 0.0; } else { return getWindSpeed().get(0); } }
    public Double getWindGustSpeedNow() { if (getWindGustSpeed() == null) { return 0.0; } else { return getWindGustSpeed().get(0); } }
    public Double getWindDirectionNow() { if (getWindDirection() == null) { return 0.0; } else { return getWindDirection().get(0); } }
    public Double getTemperatureNow() { if (getTemperature() == null) { return 0.0; } else { return getTemperature().get(0); } }

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
