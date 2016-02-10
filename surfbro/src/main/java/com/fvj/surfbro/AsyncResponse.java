package com.fvj.surfbro;

import android.content.Context;
import java.util.Calendar;

/**
 * Created by fvj on 03/02/2016.
 */
public interface AsyncResponse {
    void processFinish(Context context, double rank, String wave_output, String wind_output, String temperature_output, Calendar timestamp);
}