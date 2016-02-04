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

import java.util.Calendar;

public class SurfBroWidgetProvider extends AppWidgetProvider implements AsyncResponse {

    private static final String TAG = "WidgetProvider";
    private static final String LOGO_CLICKED = "com.fvj.surfbro.LOGO_CLICKED";

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
        WgParser parser = new WgParser(context);
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

    public void processFinish(Context context, String output, Calendar timestamp) {

        ComponentName name = new ComponentName(context, SurfBroWidgetProvider.class);
        int widget_id = AppWidgetManager.getInstance(context).getAppWidgetIds(name)[0];

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.surf_bro_widget);
        remoteViews.setTextViewText(R.id.forecast_text, output);
        remoteViews.setTextViewText(R.id.date_text, String.format("%02d/%02d", timestamp.get(Calendar.DAY_OF_MONTH), timestamp.get(Calendar.MONTH)+1));
        remoteViews.setTextViewText(R.id.time_text, String.format("%02d:%02d", timestamp.get(Calendar.HOUR_OF_DAY), timestamp.get(Calendar.MINUTE)));

        AppWidgetManager.getInstance( context ).updateAppWidget(widget_id, remoteViews);
    }
}