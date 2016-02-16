package com.fvj.surfbro.util;

import android.graphics.Color;

/**
 * Created by fvj on 05/02/2016.
 */
public class ColorRange {

    public int color0;
    public int color100;

    public ColorRange(String color0, String color100) {
        this.color0 = Color.parseColor(color0);
        this.color100 = Color.parseColor(color100);
    }

    public int getHSVInterpolation(double value, double min, double max) {
        if (value<=min)
            return getHSVInterpolation(0.0);
        if (value>=max)
            return getHSVInterpolation(1.0);

        double p = (value-min)/(max-min);
        return getHSVInterpolation(p);
    }

    public int getHSVInterpolation(double proportion) {
        float[] color0_hsv = new float[3];
        float[] color100_hsv = new float[3];
        float[] result_hsv = new float[3];

        Color.colorToHSV(color0, color0_hsv);
        Color.colorToHSV(color100, color100_hsv);

        int A = (int) (proportion * Color.alpha(color100) + (1 - proportion) * Color.alpha(color0));
        for (int i=0; i<3; i++)
            result_hsv[i] = (float) (proportion * color100_hsv[i] + (1 - proportion) * color0_hsv[i]);

        return Color.HSVToColor(A, result_hsv);
    }

    public int getRGBInterpolation(double value, double min, double max) {
        if (value<=min)
            return getRGBInterpolation(0.0);
        if (value>=max)
            return getRGBInterpolation(1.0);

        double p = (value-min)/(max-min);
        return getRGBInterpolation(p);
    }

    public int getRGBInterpolation(double proportion) {
        int A = (int) (proportion * Color.alpha(color100) + (1 - proportion) * Color.alpha(color0));
        int R = (int) (proportion * Color.red(color100) + (1 - proportion) * Color.red(color0));
        int G = (int) (proportion * Color.green(color100) + (1 - proportion) * Color.green(color0));
        int B = (int) (proportion * Color.blue(color100) + (1 - proportion) * Color.blue(color0));

        return Color.argb(A, R, G, B);
    }
}
