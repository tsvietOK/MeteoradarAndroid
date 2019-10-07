package com.tsvietok.meteoradar;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.Gson;
import com.tsvietok.meteoradar.utils.CustomLog;
import com.tsvietok.meteoradar.utils.LocationUtils;
import com.tsvietok.meteoradar.utils.NetUtils;
import com.tsvietok.meteoradar.utils.SettingsUtils;
import com.tsvietok.meteoradar.utils.StorageUtils;
import com.tsvietok.meteoradar.utils.ThemeUtils;

public class MainActivity extends AppCompatActivity {
    private static final String PREF_SELECTED_THEME_KEY = "selectedTheme";
    private static final String PREF_TIMELINE_POSITION_KEY = "timeLinePosition";
    private static final String PREF_FIRST_RUN_KEY = "firstRun";
    private static final String PREF_SELECTED_CITY_KEY = "selectedCity";
    private static final String PREF_CITY_CHANGED_KEY = "cityChanged";

    private Location location;
    private ExtendedFloatingActionButton UpdateFab;
    private SeekBar TimeLine;
    private TextView StatusText;
    private ImageView ForegroundMap;
    private TextView TimeText;
    private ImageView NoConnectionBitmap;
    private LinearLayout TimeLayout;
    private RadarBitmap[] mMaps;
    private RadarTime mData;
    private int mLastImageNumber;
    private Context context;
    private boolean mCityChanged;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CustomLog.logDebug("onCreate()");

        this.context = getApplicationContext();

        ThemeUtils.switchTheme(SettingsUtils.getIntSetting(context, PREF_SELECTED_THEME_KEY));

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        location = LocationUtils.switchCity(context, SettingsUtils.getIntSetting(context, PREF_SELECTED_CITY_KEY));
        CustomLog.logDebug("Current city: " + location.getFullName());

        Button changeCityButton = findViewById(R.id.selectedCityButton);
        changeCityButton.setText(location.getFullName());
        changeCityButton.setOnClickListener(v -> {
            int selectedCity = SettingsUtils.getIntSetting(context, PREF_SELECTED_CITY_KEY);
            final String[] listItems = {
                    getString(R.string.kiev),
                    getString(R.string.minsk),
                    getString(R.string.brest),
                    getString(R.string.gomel),
                    getString(R.string.smolensk),
                    getString(R.string.bryansk),
                    getString(R.string.kursk),
                    getString(R.string.velikiye_luki)
            };
            MaterialAlertDialogBuilder builder =
                    new MaterialAlertDialogBuilder(MainActivity.this);
            builder.setTitle(R.string.choose_city)
                    .setSingleChoiceItems(listItems, selectedCity,
                            (dialog, item) -> {
                                if (selectedCity != item) {
                                    SettingsUtils.saveIntSetting(context, PREF_SELECTED_CITY_KEY, item);
                                    changeCityButton.setText(location.getFullName());
                                    SettingsUtils.saveBooleanSetting(context, PREF_CITY_CHANGED_KEY, true);
                                    mCityChanged = true;

                                    CustomLog.logDebug("Selected city: " + location.getFullName());

                                    recreate();
                                }
                                dialog.dismiss();
                            })
                    .setNegativeButton(getString(R.string.cancel),
                            (dialog, id) -> dialog.cancel());
            AlertDialog alert = builder.create();
            alert.show();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CustomLog.logDebug("onDestroy()");

        if (mData != null) {
            StorageUtils.removeUnusedBitmap(context, mData.getTimes(), location);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        CustomLog.logDebug("onResume()");

        GetJsonAsync jsonTask;

        if (SettingsUtils.getBooleanSetting(context, PREF_FIRST_RUN_KEY)) {
            if (NetUtils.isNetworkConnected(context)) {
                jsonTask = new GetJsonAsync(true);
                jsonTask.execute();

                SettingsUtils.saveBooleanSetting(context, PREF_FIRST_RUN_KEY, false);
            } else {
                Toast.makeText(context,
                        R.string.no_internet_connection,
                        Toast.LENGTH_SHORT).show();

                NoConnectionBitmap = findViewById(R.id.NoConnectionBitmap);
                NoConnectionBitmap.setVisibility(View.VISIBLE);

                CustomLog.logError("Internet connection is not available");
            }
        } else {
            jsonTask = new GetJsonAsync(false);
            jsonTask.execute();
        }

        UpdateFab = findViewById(R.id.UpdateFab);
        UpdateFab.setOnClickListener(view -> {
            if (NetUtils.isNetworkConnected(context)) {
                GetJsonAsync jsonTaskUpdate = new GetJsonAsync(true);
                jsonTaskUpdate.execute();

                Toast.makeText(context,
                        R.string.updated,
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context,
                        R.string.no_internet_connection,
                        Toast.LENGTH_SHORT).show();

                NoConnectionBitmap = findViewById(R.id.NoConnectionBitmap);
                NoConnectionBitmap.setVisibility(View.VISIBLE);

                CustomLog.logError("Internet connection is not available");
            }
        });
        ForegroundMap = findViewById(R.id.ForegroundMap);
        ForegroundMap.setOnClickListener(v -> {
            if (ForegroundMap.getMeasuredHeight() == 1024) {
                ValueAnimator anim = ValueAnimator.ofInt(ForegroundMap.getMeasuredHeight(), 600);
                anim.addUpdateListener(valueAnimator -> {
                    int val = (Integer) valueAnimator.getAnimatedValue();
                    ViewGroup.LayoutParams layoutParams = ForegroundMap.getLayoutParams();
                    layoutParams.height = val;
                    ForegroundMap.setLayoutParams(layoutParams);
                });
                anim.setDuration(250);
                anim.start();
            } else {
                ValueAnimator anim = ValueAnimator.ofInt(ForegroundMap.getMeasuredHeight(), 1024);
                anim.addUpdateListener(valueAnimator -> {
                    int val = (Integer) valueAnimator.getAnimatedValue();
                    ViewGroup.LayoutParams layoutParams = ForegroundMap.getLayoutParams();
                    layoutParams.height = val;
                    ForegroundMap.setLayoutParams(layoutParams);
                });
                anim.setDuration(250);
                anim.start();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        CustomLog.logDebug("onSaveInstanceState()");

        if (mData != null) {
            TimeLine = findViewById(R.id.TimeLine);
            if (mCityChanged) {
                mLastImageNumber = mMaps.length - 1;
            } else {
                mLastImageNumber = TimeLine.getProgress();
            }
            outState.putInt(PREF_TIMELINE_POSITION_KEY, mLastImageNumber);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        CustomLog.logDebug("onRestoreInstanceState()");
        mLastImageNumber = savedInstanceState.getInt(PREF_TIMELINE_POSITION_KEY);

    }

    private void getData() {
        CustomLog.logDebug("getData()");

        StatusText = findViewById(R.id.StatusText);
        StatusText.setVisibility(mData.getMode() ? View.VISIBLE : View.INVISIBLE);

        if (!mMaps[mLastImageNumber].isLoaded()) {
            GetImageAsync imageAsync = new GetImageAsync();
            imageAsync.execute(mLastImageNumber);
        } else {
            showData(mLastImageNumber);
        }

        TimeLine = findViewById(R.id.TimeLine);
        TimeLine.setMax(mLastImageNumber);
        TimeLine.setProgress(mLastImageNumber, true);
        TimeLine.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (!mMaps[i].isLoaded()) {
                    GetImageAsync imageAsync = new GetImageAsync();
                    imageAsync.execute(i);
                } else {
                    showData(i);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void showData(int number) {
        CustomLog.logDebug("showData()");

        if (number != -1) {
            TimeText = findViewById(R.id.TimeText);
            TimeText.setText(mMaps[number].getTime());
            ForegroundMap = findViewById(R.id.ForegroundMap);
            int currentNightMode = getResources().getConfiguration().uiMode
                    & Configuration.UI_MODE_NIGHT_MASK;
            switch (currentNightMode) {
                case Configuration.UI_MODE_NIGHT_NO:
                    ForegroundMap.setImageBitmap(mMaps[number].getImage());
                    break;
                case Configuration.UI_MODE_NIGHT_YES:
                    ForegroundMap.setImageBitmap(mMaps[number].getNightImage());
                    break;
            }
        } else {
            Toast.makeText(context,
                    R.string.image_not_available,
                    Toast.LENGTH_SHORT).show();
            NoConnectionBitmap = findViewById(R.id.NoConnectionBitmap);
            NoConnectionBitmap.setVisibility(View.VISIBLE);
        }
    }

    private void ShowTime() {
        TimeLayout = findViewById(R.id.TimeLayout);
        TimeLayout.removeAllViews();
        int timesNumber = mData.getTimes().length;
        for (int i = 0; i < timesNumber; i++) {
            MaterialTextView timeLayoutText = new MaterialTextView(context);
            timeLayoutText.setText(mData.getTimeString()[i]);
            timeLayoutText.setTextSize(12);
            LinearLayout.LayoutParams params = new LayoutParams(
                    LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT, 1f);
            timeLayoutText.setLayoutParams(params);
            timeLayoutText.setGravity(Gravity.CENTER);
            timeLayoutText.setTypeface(Typeface.MONOSPACE);
            timeLayoutText.setTextColor(getColor(R.color.colorTextDayNight));
            TimeLayout.addView(timeLayoutText);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = menuItem.getItemId();

        if (id == R.id.action_theme) {
            int selectedTheme = SettingsUtils.getIntSetting(context, PREF_SELECTED_THEME_KEY);
            if (selectedTheme == -1) {
                selectedTheme = 0;
            }
            final String[] listItems = {
                    getString(R.string.follow_system_theme),
                    getString(R.string.light_theme),
                    getString(R.string.dark_theme)};
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(MainActivity.this);
            builder.setTitle(R.string.choose_theme)
                    .setSingleChoiceItems(listItems, selectedTheme,
                            (dialog, item) -> {
                                SettingsUtils.saveIntSetting(context, PREF_SELECTED_THEME_KEY, item);
                                ThemeUtils.switchTheme(item);
                                dialog.dismiss();
                            })
                    .setNegativeButton(getString(R.string.cancel),
                            (dialog, id1) -> dialog.cancel());
            AlertDialog alert = builder.create();
            alert.show();
        }
        if (id == R.id.action_exit) {
            finish();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    private class GetImageAsync extends AsyncTask<Integer, Void, Integer> {
        @Override
        protected Integer doInBackground(Integer... number) {
            int imageNumber = number[0];
            int timestamp = mMaps[imageNumber].getTimestamp();

            Bitmap bitmap =
                    StorageUtils.getBitmapFromStorage(context, Integer.toString(timestamp), location);
            if (bitmap == null) {
                bitmap = NetUtils.getBitmapFromServer(mMaps[imageNumber].getImageLink());

                if (bitmap == null) {
                    return -1;
                }

                CustomLog.logDebug("GetImageAsync(): Image " + timestamp + " has been loaded.");
                StorageUtils.saveBitmapToStorage(context, bitmap, timestamp, location);
            } else {
                CustomLog.logDebug("GetImageAsync(): Image " + timestamp + " already exists.");
            }
            mMaps[imageNumber].setImage(bitmap);
            return imageNumber;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            showData(result);
        }
    }

    private class GetJsonAsync extends AsyncTask<Void, Void, String> {
        private final boolean forcedUpdate;

        GetJsonAsync(boolean forcedUpdate) {
            this.forcedUpdate = forcedUpdate;
        }

        @Override
        protected String doInBackground(Void... params) {
            String jsonString = StorageUtils.getJsonFromStorage(context, location);
            if (jsonString == null || jsonString.length() == 0 || forcedUpdate) {
                jsonString = NetUtils.getJsonFromServer(location);

                if (jsonString == null) {
                    return null;
                }

                CustomLog.logDebug("GetJsonAsync(): New JSON config has been loaded.");
                StorageUtils.saveJsonToStorage(context, jsonString, location);
            } else {
                CustomLog.logDebug("GetJsonAsync(): JSON config already exists.");
            }
            return jsonString;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (result == null) {
                return;
            }

            RadarTime newData = new Gson().fromJson(result, RadarTime.class);
            if (newData.getTimes().length == 0) {
                NoConnectionBitmap = findViewById(R.id.NoConnectionBitmap);
                NoConnectionBitmap.setVisibility(View.VISIBLE);
                return;
            }

            mMaps = new RadarBitmap[newData.getTimes().length];
            mLastImageNumber = mMaps.length - 1;
            if (mData == null
                    || mData.getTime(0) != newData.getTime(0)
                    || forcedUpdate) {
                mData = newData;
            }

            for (int i = 0; i < mData.getTimes().length; i++) {
                mMaps[i] = new RadarBitmap(location);
                mMaps[i].setTime(mData.getTime(i));
            }

            ShowTime();
            getData();
        }
    }
}
