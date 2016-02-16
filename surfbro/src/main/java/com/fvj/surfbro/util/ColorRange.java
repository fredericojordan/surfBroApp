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

    private double getProportion(double value) {
        double p = (value-60.0) / 40.0;
        if (p<0) p=0;
        return p;
    }

    public int getHSVInterpolation(double value) {

        double p = getProportion(value);

        float[] color0_hsv = new float[3];
        float[] color100_hsv = new float[3];
        float[] result_hsv = new float[3];

        Color.colorToHSV(color0, color0_hsv);
        Color.colorToHSV(color100, color100_hsv);

        int A = (int) (p * Color.alpha(color100) + (1 - p) * Color.alpha(color0));
        for (int i=0; i<3; i++)
            result_hsv[i] = (float) (p * color100_hsv[i] + (1 - p) * color0_hsv[i]);

        return Color.HSVToColor(A, result_hsv);
    }

    public int getRGBInterpolation(double value) {

        double p = getProportion(value);

        int A = (int) (p * Color.alpha(color100) + (1 - p) * Color.alpha(color0));
        int R = (int) (p * Color.red(color100) + (1 - p) * Color.red(color0));
        int G = (int) (p * Color.green(color100) + (1 - p) * Color.green(color0));
        int B = (int) (p * Color.blue(color100) + (1 - p) * Color.blue(color0));

        return Color.argb(A, R, G, B);
    }
}
