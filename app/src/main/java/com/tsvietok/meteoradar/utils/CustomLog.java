package com.tsvietok.meteoradar.utils;

import android.util.Log;

import static com.tsvietok.meteoradar.BuildConfig.DEBUG;

public class CustomLog {
    private static final String LOG_TAG = "Meteoradar";

    public static void logError(String message) {
        if (DEBUG)
            Log.e(LOG_TAG, message);
    }

    public static void logDebug(String message) {
        if (DEBUG)
            Log.d(LOG_TAG, message);
    }
}
