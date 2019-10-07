package com.tsvietok.meteoradar.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;

import static android.graphics.Bitmap.Config;
import static android.graphics.Bitmap.createBitmap;

public class BitmapUtils {
    public static Bitmap RemoveColor(Bitmap source) {
        int sourceWidth = source.getWidth();
        int sourceHeight = source.getHeight();
        int sourceSize = sourceWidth * sourceHeight;
        int[] sourcePixels = new int[sourceSize];
        source.getPixels(sourcePixels, 0, sourceWidth, 0, 0, sourceWidth, sourceHeight);
        for (int i = 0; i < sourceSize; i++) {
            if (sourcePixels[i] == Color.rgb(204, 206, 204)
                    || sourcePixels[i] == Color.rgb(196, 194, 196)
                    || sourcePixels[i] == Color.rgb(204, 202, 204)) {
                sourcePixels[i] = Color.TRANSPARENT;
            }
            if (sourcePixels[i] == Color.rgb(252, 254, 252)) {
                sourcePixels[i] = Color.rgb(222, 222, 222);
            }
        }
        return createBitmap(sourcePixels, sourceWidth, sourceHeight, Config.ARGB_8888);
    }

    public static Bitmap OverlayBitmap(Bitmap lowerLayer, Bitmap upperLayer) {
        Bitmap bmOverlay = createBitmap(upperLayer.getWidth(),
                upperLayer.getHeight(),
                Config.ARGB_8888);
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(lowerLayer, new Matrix(), null);
        canvas.drawBitmap(upperLayer, new Matrix(), null);
        return bmOverlay;
    }

    public static Bitmap InvertBitmap(Bitmap source) {
        int sourceWidth = source.getWidth();
        int sourceHeight = source.getHeight();
        int sourceSize = sourceWidth * sourceHeight;
        int[] sourcePixels = new int[sourceSize];
        source.getPixels(sourcePixels, 0, sourceWidth, 0, 0, sourceWidth, sourceHeight);
        for (int i = 0; i < sourceSize; i++) {
            int intValue = sourcePixels[i];
            Color color = Color.valueOf(intValue);
            float red = color.red();
            float green = color.green();
            float blue = color.blue();
            int threshold = 10;
            //filter shades of gray
            if (Math.abs(red - green)
                    + Math.abs(green - blue) + Math.abs(red - blue) < threshold / 255.0) {
                sourcePixels[i] = Color.argb(1, 1 - red, 1 - green, 1 - blue);
            }
        }
        return createBitmap(sourcePixels, sourceWidth, sourceHeight, Config.ARGB_8888);
    }
}
