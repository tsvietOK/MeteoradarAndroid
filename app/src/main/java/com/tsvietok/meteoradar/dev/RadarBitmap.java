package com.tsvietok.meteoradar.dev;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.tsvietok.meteoradar.dev.utils.BitmapUtils;
import com.tsvietok.meteoradar.dev.utils.NetUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

class RadarBitmap {
    private final Location mLocation;
    private final Bitmap mBackgroundImage;
    private boolean mIsLoaded = false;
    private Bitmap mImage;
    private Bitmap mNightImage;
    private String mTime;
    private int mTimestamp;

    RadarBitmap(Location location) {
        this.mBackgroundImage = location.getLocalMap();
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

        int pixel = image.getPixel(image.getWidth() - 30, image.getHeight() - 30); //get background color
        int red = Color.red(pixel);
        int green = Color.green(pixel);
        int blue = Color.blue(pixel);
        int backgroundColor = Color.rgb(red, green, blue);
        image = BitmapUtils.RemoveColor(image, backgroundColor);

        int riverColor = Color.rgb(204, 202, 204);
        image = BitmapUtils.RemoveColor(image, riverColor);

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
        this.mTime = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(_time);
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
