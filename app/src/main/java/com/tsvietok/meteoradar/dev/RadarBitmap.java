package com.tsvietok.meteoradar.dev;

import android.graphics.Bitmap;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.tsvietok.meteoradar.dev.utils.BitmapUtils.InvertBitmap;
import static com.tsvietok.meteoradar.dev.utils.BitmapUtils.OverlayBitmap;
import static com.tsvietok.meteoradar.dev.utils.BitmapUtils.RemoveColor;

class RadarBitmap {
    private static final String DOMAIN = "http://radar.veg.by/";
    private static final String REGION = "ukbb";
    private boolean mIsLoaded = false;
    private Bitmap mImage;
    private Bitmap mNightImage;
    private Bitmap mBackgroundImage;
    private String mTime;
    private int mTimestamp;

    RadarBitmap(Bitmap bitmap) {
        this.mBackgroundImage = Bitmap.createScaledBitmap(bitmap, 654, 479, true);
    }

    boolean isLoaded() {
        return mIsLoaded;
    }

    Bitmap getImage() {
        return mImage;
    }

    void setImage(Bitmap image) {
        mIsLoaded = true;
        image = RemoveColor(image);
        image = OverlayBitmap(this.mBackgroundImage, image);
        mImage = Bitmap.createBitmap(image, 18, 2, 476, 476);
        mNightImage = InvertBitmap(this.mImage);
    }

    Bitmap getNightImage() {
        return mNightImage;
    }

    String getTime() {
        return mTime;
    }

    void setTime(int timestamp) {
        this.mTimestamp = timestamp;
        Date _time = new Date((long) timestamp * 1000);
        this.mTime = new SimpleDateFormat("dd.MM HH:mm").format(_time);
    }

    Integer getTimestamp() {
        return mTimestamp;
    }

    String getImageLink() {
        return DOMAIN + "data/" + REGION + "/images/" + mTimestamp + ".png";
    }
}
