package com.tsvietok.meteoradar.dev;

class MapInfo {
    private String mTitle;
    private int mColor;

    MapInfo(String title, int color) {
        mTitle = title;
        mColor = color;
    }

    String getTitle() {
        return mTitle;
    }

    int getColor() {
        return mColor;
    }
}
