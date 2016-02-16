package com.fvj.surfbro;
/**
 * Created by fvj on 27/01/2016.
 */

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.fvj.surfbro.util.ColorRange;
import com.fvj.surfbro.util.Forecast;
import com.fvj.surfbro.util.WaveRanker;

import java.util.Calendar;

public class SurfBroWidgetProvider extends AppWidgetProvider implements AsyncResponse {

    private static final String TAG = "WidgetProvider";
    private static final String LOGO_CLICKED = "com.fvj.surfbro.LOGO_CLICKED";
    private ColorRange textColorRange = new ColorRange("#ffff0000", "#ff00ff00");

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(LOGO_CLICKED)) {
            Toast.makeText(context, "Refreshing...", Toast.LENGTH_SHORT).show();
            callForRefresh(context);
        }
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        // update each of the widgets with the remote adapter
        for (int i = 0; i < appWidgetIds.length; ++i) {

            callForRefresh(context);
            setupClickIntent(context, appWidgetManager, appWidgetIds[i]);

        }
    }

    protected void callForRefresh(Context context) {
        WgRequester parser = new WgRequester(context);
        parser.delegate = this;
        parser.execute("http://www.windguru.cz/pt/index.php?sc=105160");
    }

    protected void setupClickIntent(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.surf_bro_widget);
        remoteViews.setOnClickPendingIntent(R.id.logo_button, getPendingSelfIntent(context, LOGO_CLICKED));
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
    }

    protected PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, getClass());
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    public void processFinish(Context context, Forecast forecast) {

        ComponentName name = new ComponentName(context, SurfBroWidgetProvider.class);
        int widget_id = AppWidgetManager.getInstance(context).getAppWidgetIds(name)[0];

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.surf_bro_widget);

        remoteViews.setTextViewText(R.id.wave_forecast_text, forecast.makeWaveString());
        remoteViews.setTextColor(R.id.wave_forecast_text, textColorRange.getHSVInterpolation(WaveRanker.wave_rank(forecast.getWaveHeightNow()), 0.6, 1));

        remoteViews.setTextViewText(R.id.wind_forecast_text, forecast.makeWindString());
        remoteViews.setTextColor(R.id.wind_forecast_text, textColorRange.getHSVInterpolation(WaveRanker.wind_rank(forecast.getWindGustSpeedNow()), 0.6, 1));

        remoteViews.setTextViewText(R.id.temperature_text, forecast.makeTemperatureString());

        remoteViews.setTextViewText(R.id.date_text, String.format("%02d/%02d",
                forecast.timestamp.get(Calendar.DAY_OF_MONTH),
                forecast.timestamp.get(Calendar.MONTH)+1));

        remoteViews.setTextViewText(R.id.time_text, String.format("%02d:%02d",
                forecast.timestamp.get(Calendar.HOUR_OF_DAY),
                forecast.timestamp.get(Calendar.MINUTE)));

        double rank = WaveRanker.rank(forecast);
        remoteViews.setTextViewText(R.id.rank_text, String.format("%.1f", 10*rank));
        remoteViews.setTextColor(R.id.rank_text, textColorRange.getHSVInterpolation(rank, 0.6, 1));

        AppWidgetManager.getInstance( context ).updateAppWidget(widget_id, remoteViews);
    }
}