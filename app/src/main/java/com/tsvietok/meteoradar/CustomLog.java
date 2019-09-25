package com.tsvietok.meteoradar;

import android.util.Log;

class CustomLog {
    private static final Boolean DEBUG = true;
    private static String LOG_TAG = "Meteoradar";

    static void logError(String message) {
        if (DEBUG)
            Log.e(LOG_TAG, message);
    }

    static void logDebug(String message) {
        if (DEBUG)
            Log.d(LOG_TAG, message);
    }
}
