package com.tsvietok.meteoradar;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;

import java.text.SimpleDateFormat;
import java.util.Date;

class RadarBitmap {
    private Bitmap image;
    private Bitmap nightImage;
    private String time;

    RadarBitmap(Bitmap image, int time, Bitmap _backgroundMap) {
        Bitmap _map;
        Date _time = new Date((long) time * 1000);
        this.time = new SimpleDateFormat("dd.MM HH:mm").format(_time);
        _backgroundMap = Bitmap.createScaledBitmap(_backgroundMap, 654, 479, true);
        _map = RemoveColor(image);
        _map = OverlayBitmap(_backgroundMap, _map);
        this.nightImage = InvertBitmap(_map);
        this.image = _map;
    }

    Bitmap getImage() {
        return image;
    }

    Bitmap getNightImage() {
        return nightImage;
    }

    String getTime() {
        return time;
    }

    private Bitmap RemoveColor(Bitmap source) {
        int sourceWidth = source.getWidth();
        int sourceHeight = source.getHeight();
        int sourceSize = sourceWidth * sourceHeight;
        int[] sourcePixels = new int[sourceSize];
        source.getPixels(sourcePixels, 0, sourceWidth, 0, 0, sourceWidth, sourceHeight);
        for (int i = 0; i < sourceSize; i++) {
            if (sourcePixels[i] == Color.rgb(204, 206, 204) || sourcePixels[i] == Color.rgb(196, 194, 196) || sourcePixels[i] == Color.rgb(204, 202, 204)) {
                sourcePixels[i] = Color.TRANSPARENT;
            }
        }
        return Bitmap.createBitmap(sourcePixels, sourceWidth, sourceHeight, Bitmap.Config.ARGB_8888);
    }

    private Bitmap OverlayBitmap(Bitmap lowerLayer, Bitmap upperLayer) {
        Bitmap bmOverlay = Bitmap.createBitmap(upperLayer.getWidth(), upperLayer.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(lowerLayer, new Matrix(), null);
        canvas.drawBitmap(upperLayer, new Matrix(), null);
        return bmOverlay;
    }

    private Bitmap InvertBitmap(Bitmap source) {
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
            if (Math.abs(red - green) + Math.abs(green - blue) + Math.abs(red - blue) < threshold / 255.0) //filter shades of gray
            {
                sourcePixels[i] = Color.argb(1, 1 - red, 1 - green, 1 - blue);
            }
        }
        return Bitmap.createBitmap(sourcePixels, sourceWidth, sourceHeight, Bitmap.Config.ARGB_8888);
    }
}
