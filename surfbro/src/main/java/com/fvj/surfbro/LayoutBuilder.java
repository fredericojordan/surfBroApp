package com.fvj.surfbro;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.widget.RemoteViews;

import com.fvj.surfbro.util.ColorRange;
import com.fvj.surfbro.util.WaveRanker;

import java.util.Calendar;

public class LayoutBuilder implements AsyncResponse {

    private ColorRange textColorRange = new ColorRange("#cc0000", "#00cc00");

    private Context context;
    private Forecast forecast;

    public void processFinish(Context context, Forecast forecast) {
        this.context = context;
        this.forecast = forecast;

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.surf_bro_widget);

        buildWaveInfo(remoteViews);
        buildWindInfo(remoteViews);
        buildTemperatureInfo(remoteViews);
        buildDateTimeInfo(remoteViews);
        buildRankInfo(remoteViews);

        ComponentName name = new ComponentName(context, WidgetProvider.class);
        int widget_id = AppWidgetManager.getInstance(context).getAppWidgetIds(name)[0];
        AppWidgetManager.getInstance( context ).updateAppWidget(widget_id, remoteViews);
    }

    private void buildWaveInfo(RemoteViews remoteViews) {
        int wave_color = textColorRange.getHSVInterpolation(WaveRanker.wave_rank(forecast.getWaveHeightNow()), 0.7, 1);
        remoteViews.setTextViewText(R.id.wave_forecast_text, forecast.makeWaveString());
        remoteViews.setTextViewText(R.id.wave_add_text, forecast.makeWaveAdditionalString());
        remoteViews.setTextColor(R.id.wave_forecast_text, wave_color);
        remoteViews.setTextColor(R.id.wave_add_text, wave_color);
        Bitmap wave_direction = generateDirectionArrow(forecast.getWaveDirectionNow(), wave_color);
        remoteViews.setImageViewBitmap(R.id.wave_direction, wave_direction);
    }

    private void buildWindInfo(RemoteViews remoteViews) {
        int wind_color = textColorRange.getHSVInterpolation(WaveRanker.wind_rank(forecast.getWindGustSpeedNow()), 0.7, 1);
        remoteViews.setTextViewText(R.id.wind_forecast_text, forecast.makeWindString());
        remoteViews.setTextColor(R.id.wind_forecast_text, wind_color);
        Bitmap wind_direction = generateDirectionArrow(forecast.getWindDirectionNow(), wind_color);
        remoteViews.setImageViewBitmap(R.id.wind_direction, wind_direction);
    }

    private void buildTemperatureInfo(RemoteViews remoteViews) {
        remoteViews.setTextViewText(R.id.temperature_text, forecast.makeTemperatureString());
    }

    private void buildDateTimeInfo(RemoteViews remoteViews) {
        remoteViews.setTextViewText(R.id.date_text, String.format("%02d/%02d",
                forecast.timestamp.get(Calendar.DAY_OF_MONTH),
                forecast.timestamp.get(Calendar.MONTH) + 1));

        remoteViews.setTextViewText(R.id.time_text, String.format("%02d:%02d",
                forecast.timestamp.get(Calendar.HOUR_OF_DAY),
                forecast.timestamp.get(Calendar.MINUTE)));
    }

    private void buildRankInfo(RemoteViews remoteViews) {
        double rank = WaveRanker.rank(forecast);
        int rank_color = textColorRange.getHSVInterpolation(rank, 0.7, 1) & 0x1fffffff;
        remoteViews.setTextViewText(R.id.rank_text, String.format("%.1f", 10 * rank));
        remoteViews.setTextColor(R.id.rank_text, rank_color);
    }

    private Bitmap generateDirectionArrow(Double direction, int color) {
        Matrix rotation_matrix = new Matrix();
        rotation_matrix.postRotate(direction.floatValue());
        Bitmap arrow = BitmapFactory.decodeResource(context.getResources(), R.drawable.arrow);
        Bitmap direction_arrow = Bitmap.createBitmap(arrow, 0, 0, arrow.getWidth(), arrow.getHeight(), rotation_matrix, true);
        Paint p = new Paint();
        p.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
        Canvas canvas = new Canvas(direction_arrow);
        canvas.drawBitmap(direction_arrow, new Matrix(), p);
        return direction_arrow;
    }
}