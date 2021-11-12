package com.tsvietok.meteoradar.dev;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;

import com.tsvietok.meteoradar.dev.utils.BitmapUtils;

import java.util.HashMap;
import java.util.Map;

public class RadarBitmapNew extends RadarBitmap {
    RadarBitmapNew(String city, Context context) {
        super(city, context);
    }

    @Override
    void setImage(Bitmap image) {
        mIsLoaded = true;

        Map<Integer, Integer> replaceColorsMap = new HashMap<>();
        replaceColorsMap.put(Color.rgb(212, 214, 212), Color.WHITE);
        replaceColorsMap.put(Color.rgb(252, 254, 252),
                Color.rgb(222, 222, 222));
        replaceColorsMap.put(Color.rgb(180, 178, 180),
                Color.rgb(245, 245, 245));
        image = BitmapUtils.ReplaceColor(image, replaceColorsMap);

        mImage = Bitmap.createBitmap(image, 2, 2, 760, 760);
        mNightImage = BitmapUtils.InvertBitmap(mImage, mContext);

        replaceColorsMap.clear();
        replaceColorsMap.put(Color.rgb(0, 0, 0),
                Color.argb(0, 0, 0, 0));
        mNightImage = BitmapUtils.ReplaceColor(mNightImage, replaceColorsMap);
    }
}
