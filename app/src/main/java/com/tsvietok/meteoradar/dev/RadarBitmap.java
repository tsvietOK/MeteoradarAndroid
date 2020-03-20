package com.tsvietok.meteoradar.dev;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;

import com.tsvietok.meteoradar.dev.utils.BitmapUtils;
import com.tsvietok.meteoradar.dev.utils.NetUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

class RadarBitmap {
    private final Location mLocation;
    private final Bitmap mBackgroundImage;
    private Context mContext;
    private boolean mIsLoaded = false;
    private Bitmap mImage;
    private Bitmap mNightImage;
    private String mTime;
    private int mTimestamp;

    RadarBitmap(Location location, Context context) {
        this.mBackgroundImage = location.getLocalMap();
        mLocation = location;
        mContext = context;
    }

    boolean isLoaded() {
        return mIsLoaded;
    }

    void setImage(Bitmap image) {
        mIsLoaded = true;

        // get background color
        int pixel = image.getPixel(image.getWidth() - 30, image.getHeight() - 30);
        int red = Color.red(pixel);
        int green = Color.green(pixel);
        int blue = Color.blue(pixel);

        Map<Integer, Integer> replaceColorsMap = new HashMap<>();
        replaceColorsMap.put(Color.rgb(red, green, blue), Color.TRANSPARENT);
        replaceColorsMap.put(Color.rgb(204, 202, 204), Color.TRANSPARENT);
        replaceColorsMap.put(Color.rgb(204, 203, 204), Color.TRANSPARENT);
        replaceColorsMap.put(Color.rgb(212, 210, 212), Color.TRANSPARENT);
        replaceColorsMap.put(Color.rgb(204, 206, 204), Color.TRANSPARENT);
        replaceColorsMap.put(Color.rgb(212, 212, 211), Color.TRANSPARENT);
        replaceColorsMap.put(Color.rgb(211, 211, 210), Color.TRANSPARENT);
        replaceColorsMap.put(Color.rgb(213, 213, 212), Color.TRANSPARENT);
        replaceColorsMap.put(Color.rgb(252, 254, 252),
                Color.rgb(222, 222, 222));
        image = BitmapUtils.ReplaceColor(image, replaceColorsMap);

        image = BitmapUtils.OverlayBitmap(mBackgroundImage, image);
        mImage = Bitmap.createBitmap(image, 18, 2, 476, 476);
        mImage = Bitmap.createScaledBitmap(mImage, 952, 952, false);
        mNightImage = BitmapUtils.InvertBitmap(mImage, mContext);
    }

    Bitmap getDayImage() {
        return mImage;
    }

    Bitmap getNightImage() {
        return mNightImage;
    }

    String getTime() {
        return mTime;
    }

    void setTime(int timestamp) {
        mTimestamp = timestamp;
        Date _time = new Date((long) timestamp * 1000);
        mTime = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(_time);
    }

    Integer getTimestamp() {
        return mTimestamp;
    }

    String getImageLink() {
        return NetUtils.getDomain()
                + "/data/"
                + mLocation.getCode()
                + "/images/"
                + mTimestamp
                + ".png";
    }
}
