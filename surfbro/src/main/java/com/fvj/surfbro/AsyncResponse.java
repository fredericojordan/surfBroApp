package com.fvj.surfbro;

import android.content.Context;

/**
 * Created by fvj on 03/02/2016.
 */
public interface AsyncResponse {
    void processFinish(Context context, Forecast forecast);
}