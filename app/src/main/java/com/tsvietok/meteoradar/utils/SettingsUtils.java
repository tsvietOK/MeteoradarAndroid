package com.tsvietok.meteoradar.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingsUtils {
    private static final String PREF_NAME = "com.tsvietok.meteoradar.dev.preferences";

    public static void saveIntSetting(Context context, String key, int value) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREF_NAME, 0).edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static int getIntSetting(Context context, String key) {
        SharedPreferences sharedPref = context.getSharedPreferences(PREF_NAME, 0);
        return sharedPref.getInt(key, 0);
    }
}
