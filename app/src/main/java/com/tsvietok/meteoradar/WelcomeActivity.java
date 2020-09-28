package com.tsvietok.meteoradar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.tsvietok.meteoradar.utils.SettingsUtils;

import java.util.LinkedList;

public class WelcomeActivity extends AppCompatActivity {
    private static final String PREF_SELECTED_CITY_KEY = "selectedCity";
    private int mSelectedCityPosition;
    private LinkedList<String> mCityList = new LinkedList<>();
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        mContext = getApplicationContext();

        mCityList.add(getString(R.string.kiev));
        mCityList.add(getString(R.string.minsk));
        mCityList.add(getString(R.string.brest));
        mCityList.add(getString(R.string.gomel));
        mCityList.add(getString(R.string.smolensk));
        mCityList.add(getString(R.string.bryansk));
        mCityList.add(getString(R.string.kursk));
        mCityList.add(getString(R.string.velikiye_luki));
        mCityList.add(getString(R.string.zaporozhye));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.dropdown_menu_popup_item, mCityList);

        FloatingActionButton mSelectCityFab = findViewById(R.id.SelectCityFab);

        AutoCompleteTextView autoCompleteTextView = findViewById(R.id.filled_exposed_dropdown);
        autoCompleteTextView.setInputType(0);
        autoCompleteTextView.setAdapter(adapter);
        autoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
            mSelectedCityPosition = position;
            mSelectCityFab.show();
        });

        mSelectCityFab.setOnClickListener(view -> {
            SettingsUtils.saveIntSetting(mContext, PREF_SELECTED_CITY_KEY,
                    mSelectedCityPosition);

            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
