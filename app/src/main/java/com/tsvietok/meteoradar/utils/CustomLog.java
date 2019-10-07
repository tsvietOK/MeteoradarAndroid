package com.tsvietok.meteoradar.utils;

import android.util.Log;

import com.tsvietok.meteoradar.BuildConfig;

public class CustomLog {
    private static final String LOG_TAG = "Meteoradar";

    public static void logError(String message) {
        if (BuildConfig.DEBUG) {
            Log.e(LOG_TAG, message);
        }
    }

    public static void logDebug(String message) {
        if (BuildConfig.DEBUG) {
            Log.d(LOG_TAG, message);
        }
    }
}
