package com.tsvietok.meteoradar.dev;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.tsvietok.meteoradar.dev.utils.SettingsUtils;

public class WelcomeActivity extends AppCompatActivity {
    private static final String PREF_SELECTED_CITY_KEY = "selectedCity";
    private RadioGroup mCityGroup;
    private int mSelectedCityId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        mCityGroup = findViewById(R.id.cityRadioGroup);
        mCityGroup.setOnCheckedChangeListener((rGroup, checkedId) -> {

            int radioButtonId = rGroup.getCheckedRadioButtonId();
            View radioB = rGroup.findViewById(radioButtonId);
            mSelectedCityId = rGroup.indexOfChild(radioB);
        });

        FloatingActionButton mSelectCityFab = findViewById(R.id.SelectCityFab);
        mSelectCityFab.setOnClickListener(view -> {
            mCityGroup = findViewById(R.id.cityRadioGroup);

            SettingsUtils.saveIntSetting(getApplicationContext(), PREF_SELECTED_CITY_KEY, mSelectedCityId);

            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
