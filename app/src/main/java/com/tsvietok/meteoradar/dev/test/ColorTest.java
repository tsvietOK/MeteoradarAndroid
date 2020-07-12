package com.tsvietok.meteoradar.dev.test;

import android.graphics.Bitmap;

public class ColorTest {
    private Bitmap mBitmapColorOriginal;
    private Bitmap mBitmapColorDark;
    private Bitmap mBitmapColorInverted;

    ColorTest(Bitmap originalColor, Bitmap darkColor, Bitmap invertedColor) {
        mBitmapColorOriginal = originalColor;
        mBitmapColorDark = darkColor;
        mBitmapColorInverted = invertedColor;
    }

    Bitmap getOriginalColorBitmap() {
        return mBitmapColorOriginal;
    }

    Bitmap getDarkColorBitmap() {
        return mBitmapColorDark;
    }

    Bitmap getInvertedColorBitmap() {
        return mBitmapColorInverted;
    }

    String getInvertedBitmapColorHexValue() {
        int pixel = mBitmapColorInverted.getPixel(0, 0);
        return "#" + Integer.toHexString(pixel).substring(2).toUpperCase();
    }
}