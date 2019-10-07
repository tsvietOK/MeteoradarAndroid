package com.tsvietok.meteoradar.dev;

public class Location {
    private final String mFullName;
    private final String mCode;
    private final String mCity;

    public Location(String mFullName, String mCode, String mCity) {
        this.mFullName = mFullName;
        this.mCode = mCode;
        this.mCity = mCity;
    }

    String getFullName() {
        return mFullName;
    }

    String getCode() {
        return mCode;
    }

    public String getCity() {
        return mCity;
    }
}

