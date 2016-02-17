package com.fvj.surfbro;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
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

        // -- Waves
        int wave_color = textColorRange.getHSVInterpolation(WaveRanker.wave_rank(forecast.getWaveHeightNow()), 0.7, 1);
        remoteViews.setTextViewText(R.id.wave_forecast_text, forecast.makeWaveString());
        remoteViews.setTextViewText(R.id.wave_add_text, forecast.makeWaveAdditionalString());
        remoteViews.setTextColor(R.id.wave_forecast_text, wave_color);
        remoteViews.setTextColor(R.id.wave_add_text, wave_color);

        // -- Waves Dir
        Matrix wind_matrix = new Matrix();
        wind_matrix.postRotate(forecast.getWaveDirectionNow().floatValue());
        Bitmap arrow = BitmapFactory.decodeResource(context.getResources(), R.drawable.arrow);
        Bitmap wave_direction = Bitmap.createBitmap(arrow, 0, 0, arrow.getWidth(), arrow.getHeight(), wind_matrix, true);
        Paint p = new Paint();
        ColorFilter filter = new PorterDuffColorFilter(wave_color, PorterDuff.Mode.SRC_IN);
        p.setColorFilter(filter);
        Canvas canvas = new Canvas(wave_direction);
        canvas.drawBitmap(wave_direction, new Matrix(), p);
        remoteViews.setImageViewBitmap(R.id.wave_direction, wave_direction);

        // -- Wind
        int wind_color = textColorRange.getHSVInterpolation(WaveRanker.wind_rank(forecast.getWindGustSpeedNow()), 0.7, 1);
        remoteViews.setTextViewText(R.id.wind_forecast_text, forecast.makeWindString());
        remoteViews.setTextColor(R.id.wind_forecast_text, wind_color);

        // -- Wind Dir
        Matrix wave_matrix = new Matrix();
        wave_matrix.postRotate(forecast.getWindDirectionNow().floatValue());
        Bitmap arrow2 = BitmapFactory.decodeResource(context.getResources(), R.drawable.arrow);
        Bitmap wind_direction = Bitmap.createBitmap(arrow2, 0, 0, arrow.getWidth(), arrow.getHeight(), wave_matrix, true);
        p = new Paint();
        filter = new PorterDuffColorFilter(wind_color, PorterDuff.Mode.SRC_IN);
        p.setColorFilter(filter);
        canvas = new Canvas(wind_direction);
        canvas.drawBitmap(wind_direction, new Matrix(), p);
        remoteViews.setImageViewBitmap(R.id.wind_direction, wind_direction);

        // -- Temperature
        remoteViews.setTextViewText(R.id.temperature_text, forecast.makeTemperatureString());

        // -- Date
        remoteViews.setTextViewText(R.id.date_text, String.format("%02d/%02d",
                forecast.timestamp.get(Calendar.DAY_OF_MONTH),
                forecast.timestamp.get(Calendar.MONTH) + 1));

        remoteViews.setTextViewText(R.id.time_text, String.format("%02d:%02d",
                forecast.timestamp.get(Calendar.HOUR_OF_DAY),
                forecast.timestamp.get(Calendar.MINUTE)));

        // -- Rank
        double rank = WaveRanker.rank(forecast);
        int rank_color = (textColorRange.getHSVInterpolation(rank, 0.7, 1)&0xffffff) + 0x44000000;
        remoteViews.setTextViewText(R.id.rank_text, String.format("%.1f", 10 * rank));
        remoteViews.setTextColor(R.id.rank_text, rank_color);

        AppWidgetManager.getInstance( context ).updateAppWidget(widget_id, remoteViews);
    }
}