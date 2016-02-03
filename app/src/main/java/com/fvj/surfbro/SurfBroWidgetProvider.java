package com.fvj.surfbro;
/**
 * Created by fvj on 27/01/2016.
 */

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.Toast;

public class SurfBroWidgetProvider extends AppWidgetProvider implements AsyncResponse {
    public static final String TOAST_ACTION = "com.fvj.surfbro.TOAST_ACTION";
    public static final String EXTRA_ITEM = "com.fvj.surfbro.EXTRA_ITEM";

    private Context mContext;
    private AppWidgetManager mAppWidgetManager;
    private int mAppWidgetId;

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
//        AppWidgetManager mgr = AppWidgetManager.getInstance(context);

        if (intent.getAction().equals(TOAST_ACTION)) {

//            int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
//            int viewIndex = intent.getIntExtra(EXTRA_ITEM, 0);

            Toast.makeText(context, "Intent!", Toast.LENGTH_SHORT).show();
        }
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        this.mContext = context;
        this.mAppWidgetManager = appWidgetManager;
        this.mAppWidgetId = appWidgetIds[0];

        // update each of the widgets with the remote adapter
        for (int i = 0; i < appWidgetIds.length; ++i) {

            WgParser parser = new WgParser(context);
            parser.delegate = this;
            parser.execute("http://www.windguru.cz/pt/index.php?sc=105160");

        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    public void processFinish(String output) {
        RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.surf_bro_widget);
        remoteViews.setTextViewText(R.id.forecast_text, output);
        mAppWidgetManager.updateAppWidget(mAppWidgetId, remoteViews);
    }
}