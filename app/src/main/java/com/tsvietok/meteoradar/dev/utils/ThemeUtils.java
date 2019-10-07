package com.tsvietok.meteoradar.dev.utils;

import androidx.appcompat.app.AppCompatDelegate;

public class ThemeUtils {
    public static void switchTheme(int key) {
        CustomLog.logDebug("switchTheme()");
        if (key == -1) {
            key = 0;
        }
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
