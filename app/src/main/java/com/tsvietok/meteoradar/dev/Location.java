package com.tsvietok.meteoradar.dev;

import android.graphics.Bitmap;

import java.io.File;

public class Location {
    private final String mFullName;
    private final String mCode;
    private final String mCity;
    private final Bitmap mLocalMap;

    public Location(String mFullName, String mCode, String mCity, Bitmap mLocalMap) {
        this.mFullName = mFullName;
        this.mCode = mCode;
        this.mCity = mCity;
        this.mLocalMap = mLocalMap;
    }

    Bitmap getLocalMap() {
        return mLocalMap;
    }

    String getFullName() {
        return mFullName;
    }

    String getCode() {
        return mCode;
    }

    public String getCity() {
        return File.separator + mCity + File.separator;
    }

}

