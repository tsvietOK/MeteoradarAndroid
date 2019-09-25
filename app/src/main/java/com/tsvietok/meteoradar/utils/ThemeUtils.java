package com.tsvietok.meteoradar.utils;

import androidx.appcompat.app.AppCompatDelegate;

import static com.tsvietok.meteoradar.utils.CustomLog.logDebug;

public class ThemeUtils {
    public static void switchTheme(int key) {
        logDebug("switchTheme()");
        switch (key) {
            case 0: //System theme
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
            case 1: //Light theme
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case 2: //Dark theme
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            default:
                break;
        }
    }
}
