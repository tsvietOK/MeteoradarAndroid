package com.tsvietok.meteoradar;

import android.content.Context;

public class Location {
    private final String mFullName;
    private final String mCity;
    private final RadarMapType mType;

    public Location(String mFullName, String mCity) {
        this(mFullName, mCity, RadarMapType.NEW);
    }

    public Location(String mFullName, String mCity, RadarMapType mType) {
        this.mFullName = mFullName;
        this.mCity = mCity;
        this.mType = mType;
    }

    String getFullName() {
        return mFullName;
    }

    public String getCity() {
        return mCity;
    }

    public RadarBitmap getRadarBitmap(Context context) {
        if (mType == RadarMapType.KIEV) {
            return new RadarBitmapKiev(mCity, context);
        } else if (mType == RadarMapType.NEW) {
            return new RadarBitmapNew(mCity, context);
        } else return new RadarBitmap(mCity, context);
    }

}