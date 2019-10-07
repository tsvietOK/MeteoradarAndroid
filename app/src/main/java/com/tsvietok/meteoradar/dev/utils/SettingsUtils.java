package com.tsvietok.meteoradar.dev.utils;

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
        return sharedPref.getInt(key, -1);
    }

    public static void saveBooleanSetting(Context context, String key, boolean value) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREF_NAME, 0).edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static Boolean getBooleanSetting(Context context, String key) {
        SharedPreferences sharedPref = context.getSharedPreferences(PREF_NAME, 0);
        return sharedPref.getBoolean(key, true);
    }
}
