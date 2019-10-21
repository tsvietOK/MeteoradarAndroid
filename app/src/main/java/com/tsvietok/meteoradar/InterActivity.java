package com.tsvietok.meteoradar;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.tsvietok.meteoradar.utils.SettingsUtils;
import com.tsvietok.meteoradar.utils.ThemeUtils;

public class InterActivity extends AppCompatActivity {
    private static final String PREF_FIRST_RUN_KEY = "firstRun";
    private static final String PREF_SELECTED_THEME_KEY = "selectedTheme";
    private static final String PREF_SELECTED_CITY_KEY = "selectedCity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ThemeUtils.switchTheme(SettingsUtils.getIntSetting(getApplicationContext(), PREF_SELECTED_THEME_KEY));
        setContentView(R.layout.activity_inter);

        if (SettingsUtils.getBooleanSetting(getApplicationContext(), PREF_FIRST_RUN_KEY) && SettingsUtils.getIntSetting(getApplicationContext(), PREF_SELECTED_CITY_KEY) == -1) {
            Intent intent = new Intent(this, WelcomeActivity.class);
            startActivity(intent);
            finish();
        } else {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
