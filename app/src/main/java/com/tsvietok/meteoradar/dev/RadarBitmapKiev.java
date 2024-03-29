package com.tsvietok.meteoradar.dev;

import android.content.Context;
import android.graphics.Bitmap;

import com.tsvietok.meteoradar.dev.utils.BitmapUtils;

public class RadarBitmapKiev extends RadarBitmap {
    RadarBitmapKiev(String city, Context context) {
        super(city, context);
    }

    @Override
    void setImage(Bitmap image) {
        mIsLoaded = true;

        mImage = image;
        mNightImage = BitmapUtils.InvertBitmap(mImage, mContext);
    }
}
