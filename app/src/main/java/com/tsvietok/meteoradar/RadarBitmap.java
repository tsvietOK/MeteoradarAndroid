package com.tsvietok.meteoradar;

import android.graphics.Bitmap;

import com.tsvietok.meteoradar.utils.BitmapUtils;
import com.tsvietok.meteoradar.utils.NetUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

class RadarBitmap {
    private final Location mLocation;
    private final Bitmap mBackgroundImage;
    private boolean mIsLoaded = false;
    private Bitmap mImage;
    private Bitmap mNightImage;
    private String mTime;
    private int mTimestamp;

    RadarBitmap(Location location) {
        this.mBackgroundImage = Bitmap.createScaledBitmap(location.getLocalMap(), 654, 479, true);
        mLocation = location;
    }

    boolean isLoaded() {
        return mIsLoaded;
    }

    Bitmap getImage() {
        return mImage;
    }

    void setImage(Bitmap image) {
        mIsLoaded = true;
        image = BitmapUtils.RemoveColor(image);
        image = BitmapUtils.OverlayBitmap(this.mBackgroundImage, image);
        mImage = Bitmap.createBitmap(image, 18, 2, 476, 476);
        mNightImage = BitmapUtils.InvertBitmap(this.mImage);
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
        return NetUtils.getDomain() + "/data/" + mLocation.getCode() + "/images/" + mTimestamp + ".png";
    }
}
