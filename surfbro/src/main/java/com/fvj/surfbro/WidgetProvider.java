package com.fvj.surfbro;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.Toast;

public class WidgetProvider extends AppWidgetProvider {

//    private static final String TAG = "WidgetProvider";
    private static final String LOGO_CLICKED = "com.fvj.surfbro.LOGO_CLICKED";
    private LayoutBuilder layoutBuilder = new LayoutBuilder();

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
        parser.delegate = layoutBuilder;
        Integer floripaId = context.getResources().getInteger(R.integer.Floripa);
        parser.execute(floripaId);
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
}