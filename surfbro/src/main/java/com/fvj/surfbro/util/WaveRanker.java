package com.fvj.surfbro.util;

import android.util.Log;

import com.fvj.surfbro.WgParser;

import org.json.JSONObject;

/**
 * Created by fvj on 05/02/2016.
 */
public class WaveRanker {

    static private String TAG = "WaveRanker";
    static private double TEMPERATURE_CONSTANT = 0.2;
    static private double WAVE_CONSTANT = 2.5;
    static private double WIND_CONSTANT = 0.0015;

    static public double rank(JSONObject forecastData) {
        double temperature_rank = temp_rank(WgParser.getTemperature(forecastData).get(WgParser.getForecastIndex(forecastData)));
        double wave_height_rank = wave_rank(WgParser.getWaveHeight(forecastData).get(WgParser.getForecastIndex(forecastData)));
        double wind_speed_rank = wind_rank(WgParser.getWindSpeed(forecastData).get(WgParser.getForecastIndex(forecastData)));

        Log.d(TAG, String.format("Temperature rank: %.3f - Wave rank: %.3f - Wind rank: %.3f",
                temperature_rank,
                wave_height_rank,
                wind_speed_rank));

        if ( temperature_rank <= 0 || wave_height_rank <= 0 || wind_speed_rank <= 0 )
            return 0;
        else
            return temperature_rank*wave_height_rank*wind_speed_rank;
//            return 3/((1/temperature_rank)+(1/wave_height_rank)+(1/wind_speed_rank));

    }

    static private double temp_rank(double temperature) {
        return 1 - Math.exp(-temperature * TEMPERATURE_CONSTANT);
    }

    static private double wave_rank(double wave_height) {
        return 1 - Math.exp(-wave_height*WAVE_CONSTANT);
    }

    static private double wind_rank(double wind_speed) {
        return 1 - wind_speed*wind_speed*WIND_CONSTANT;
    }
}
