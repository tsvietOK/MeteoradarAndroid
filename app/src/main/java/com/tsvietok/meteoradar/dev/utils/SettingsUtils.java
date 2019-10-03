package com.tsvietok.meteoradar.dev.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingsUtils {
    private static final String PREFS_NAME = "com.tsvietok.meteoradar.preferences";

    public static void saveIntSetting(Context context, String key, int value) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME, 0).edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static int getIntSetting(Context context, String key) {
        SharedPreferences sharedPref = context.getSharedPreferences(PREFS_NAME, 0);
        return sharedPref.getInt(key, 0);
    }
}
