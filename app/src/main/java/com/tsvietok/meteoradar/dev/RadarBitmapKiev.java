package com.tsvietok.meteoradar.dev;

import android.content.Context;
import android.graphics.Bitmap;

import com.tsvietok.meteoradar.dev.utils.BitmapUtils;

public class RadarBitmapKiev extends RadarBitmap {
    RadarBitmapKiev(Location location, Context context) {
        super(location, context);
    }

    @Override
    void setImage(Bitmap image) {
        mImage = image;
        mNightImage = BitmapUtils.InvertBitmap(mImage, mContext);
    }
}
