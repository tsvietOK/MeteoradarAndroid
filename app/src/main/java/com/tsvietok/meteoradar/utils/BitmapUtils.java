package com.tsvietok.meteoradar.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;

import java.io.IOException;
import java.io.InputStream;

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
        return Bitmap.createBitmap(sourcePixels, sourceWidth, sourceHeight, Bitmap.Config.ARGB_8888);
    }

    public static Bitmap OverlayBitmap(Bitmap lowerLayer, Bitmap upperLayer) {
        Bitmap bmOverlay = Bitmap.createBitmap(upperLayer.getWidth(),
                upperLayer.getHeight(),
                Bitmap.Config.ARGB_8888);
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
        return Bitmap.createBitmap(sourcePixels, sourceWidth, sourceHeight, Bitmap.Config.ARGB_8888);
    }

    static Bitmap getBitmapFromAsset(Context context, String filePath) {
        AssetManager assetManager = context.getAssets();

        InputStream inputStream;
        Bitmap bitmap = null;
        try {
            inputStream = assetManager.open(filePath);
            bitmap = BitmapFactory.decodeStream(inputStream);
        } catch (IOException e) {
            CustomLog.logError("Can't load bitmap from asset: " + e.getMessage());
        }

        return bitmap;
    }
}
