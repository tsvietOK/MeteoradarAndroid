package com.tsvietok.meteoradar;

import android.animation.ValueAnimator;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.gson.Gson;
import com.tsvietok.meteoradar.utils.NetUtils;

import java.io.IOException;

import static com.tsvietok.meteoradar.utils.CustomLog.logDebug;
import static com.tsvietok.meteoradar.utils.CustomLog.logError;
import static com.tsvietok.meteoradar.utils.DeviceUtils.getPixelValue;
import static com.tsvietok.meteoradar.utils.NetUtils.getBitmapFromServer;
import static com.tsvietok.meteoradar.utils.NetUtils.isNetworkConnected;
import static com.tsvietok.meteoradar.utils.SettingsUtils.getIntSetting;
import static com.tsvietok.meteoradar.utils.SettingsUtils.saveIntSetting;
import static com.tsvietok.meteoradar.utils.ThemeUtils.switchTheme;

public class MainActivity extends AppCompatActivity {
    private static final String PREF_SELECTED_THEME_KEY = "selectedTheme";
    private static final String PREF_TIMES_KEY = "times";
    private static final String PREF_MODE_KEY = "isDown";
    private static final String PREF_LOCKED_STATE_KEY = "locked";
    private static final String PREF_TIMEOUT_KEY = "timeout";
    private static final String PREF_TIMESTAMP_KEY = "timestamp";
    private static final String PREF_TIMELINE_POSITION_KEY = "timeLinePosition";
    private static final String PREF_DATA_SAVED_KEY = "dataSaved";
    private static final String PREF_MAPS_SAVED_KEY = "mapsSaved";
    private static final String PREF_MAPS_KEY = "maps";

    ExtendedFloatingActionButton UpdateFab;
    SeekBar TimeLine;
    TextView StatusText;
    ImageView ForegroundMap;
    TextView TimeText;
    ImageView NoConnectionBitmap;
    LinearLayout TimeLayout;

    private RadarBitmap[] mMaps = new RadarBitmap[10];
    private RadarTime mData = null;
    private int mLastImageNumber = mMaps.length - 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        logDebug("onCreate()");
        switchTheme(getIntSetting(getApplicationContext(), PREF_SELECTED_THEME_KEY));
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public void onResume() {
        super.onResume();
        logDebug("onResume()");
        if (mData == null) {
            if (isNetworkConnected(getApplicationContext())) {
                mData = new RadarTime();
                logDebug("First start, getting Json...");
                GetJsonAsync jsonTask = new GetJsonAsync();
                jsonTask.execute();
            } else {
                Toast.makeText(getApplicationContext(),
                        R.string.no_internet_connection,
                        Toast.LENGTH_SHORT).show();
                logError(getString(R.string.no_internet_connection));
                NoConnectionBitmap = findViewById(R.id.NoConnectionBitmap);
                NoConnectionBitmap.setVisibility(View.VISIBLE);
            }

        } else {
            logDebug("Json exists, showing mData...");
            getData();
        }

        UpdateFab = findViewById(R.id.UpdateFab);
        UpdateFab.setOnClickListener(view -> {
            if (isNetworkConnected(getApplicationContext())) {
                GetJsonAsync jsonTask = new GetJsonAsync();
                jsonTask.execute();
                Toast.makeText(getApplicationContext(),
                        R.string.updated,
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        R.string.no_internet_connection,
                        Toast.LENGTH_SHORT).show();
                logError(getString(R.string.no_internet_connection));
                NoConnectionBitmap = findViewById(R.id.NoConnectionBitmap);
                NoConnectionBitmap.setVisibility(View.VISIBLE);
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

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        logDebug("onSaveInstanceState()");
        if (mData != null) {
            outState.putIntArray(PREF_TIMES_KEY, mData.getTimes());
            outState.putBoolean(PREF_MODE_KEY, mData.getMode());
            outState.putBoolean(PREF_LOCKED_STATE_KEY, mData.getLockedState());
            outState.putInt(PREF_TIMEOUT_KEY, mData.getTimeout());
            outState.putInt(PREF_TIMESTAMP_KEY, mData.getTimestamp());
            TimeLine = findViewById(R.id.TimeLine);
            outState.putInt(PREF_TIMELINE_POSITION_KEY, TimeLine.getProgress());
            outState.putBoolean(PREF_DATA_SAVED_KEY, true);
        }
        if (mMaps != null) {
            outState.putSerializable(PREF_MAPS_KEY, mMaps);
            outState.putBoolean(PREF_MAPS_SAVED_KEY, true);
        }
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        logDebug("onRestoreInstanceState()");
        if (savedInstanceState.getBoolean(PREF_DATA_SAVED_KEY)) {
            mData = new RadarTime();
            mData.setTimes(savedInstanceState.getIntArray(PREF_TIMES_KEY));
            mData.setMode(savedInstanceState.getBoolean(PREF_MODE_KEY));
            mData.setLockedState(savedInstanceState.getBoolean(PREF_LOCKED_STATE_KEY));
            mData.setTimeout(savedInstanceState.getInt(PREF_TIMEOUT_KEY));
            mData.setTimestamp(savedInstanceState.getInt(PREF_TIMESTAMP_KEY));
            mLastImageNumber = savedInstanceState.getInt(PREF_TIMELINE_POSITION_KEY);
            ShowTime();
        }
        if (savedInstanceState.getBoolean(PREF_MAPS_SAVED_KEY)) {
            mMaps = new RadarBitmap[10];
            mMaps = (RadarBitmap[]) savedInstanceState.getSerializable(PREF_MAPS_KEY);
        }
    }

    private void getData() {
        logDebug("getData()");
        StatusText = findViewById(R.id.StatusText);
        StatusText.setVisibility(mData.getMode() ? View.VISIBLE : View.INVISIBLE);
        if (!mMaps[mLastImageNumber].isLoaded()) {
            GetImageAsync imageAsync = new GetImageAsync();
            imageAsync.execute(mLastImageNumber);
        } else {
            showData(mLastImageNumber);
        }

        TimeLine = findViewById(R.id.TimeLine);
        TimeLine.setProgress(mLastImageNumber, true);
        TimeLine.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                if (!mMaps[i].isLoaded()) {
                    if (isNetworkConnected(getApplicationContext())) {
                        GetImageAsync imageAsync = new GetImageAsync();
                        imageAsync.execute(i);
                    } else {
                        Toast.makeText(getApplicationContext(),
                                R.string.no_internet_connection,
                                Toast.LENGTH_SHORT).show();
                        logError(getString(R.string.no_internet_connection));
                        NoConnectionBitmap = findViewById(R.id.NoConnectionBitmap);
                        NoConnectionBitmap.setVisibility(View.VISIBLE);
                    }
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
        logDebug("showData()");
        ForegroundMap = findViewById(R.id.ForegroundMap);
        TimeText = findViewById(R.id.TimeText);
        TimeText.setText(mMaps[number].getTime());
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
    }

    private void ShowTime() {
        TimeLayout = findViewById(R.id.TimeLayout);
        TimeLayout.removeAllViews();
        for (int i = 0; i < mData.getTimes().length; i++) {
            TextView timeLayoutText = new TextView(getApplicationContext());
            timeLayoutText.setText(mData.getTimeString()[i]);
            timeLayoutText.setTextSize(12);
            LinearLayout.LayoutParams params =
                    new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            int pixelValue =
                    getPixelValue(getApplicationContext(),
                            getResources().getDimension(R.dimen.time_margin_end));
            params.setMargins(0, 0, pixelValue, 0);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_theme) {
            final int selectedTheme = getIntSetting(getApplicationContext(), PREF_SELECTED_THEME_KEY);
            final String[] listItems = {
                    getString(R.string.follow_system_theme),
                    getString(R.string.light_theme),
                    getString(R.string.dark_theme)};
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(MainActivity.this);
            builder.setTitle(R.string.choose_theme)
                    .setSingleChoiceItems(listItems, selectedTheme,
                            (dialog, item1) -> {
                                saveIntSetting(getApplicationContext(), PREF_SELECTED_THEME_KEY, item1);
                                switchTheme(getIntSetting(getApplicationContext(), PREF_SELECTED_THEME_KEY));
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
        return super.onOptionsItemSelected(item);
    }

    private class GetImageAsync extends AsyncTask<Integer, Void, Integer> {
        Bitmap bitmap;

        @Override
        protected Integer doInBackground(Integer... number) {
            int imageNumber = number[0];
            int timestamp = mMaps[imageNumber].getTimestamp();
            try {
                bitmap = getBitmapFromServer(mMaps[imageNumber].getImageLink());
            } catch (IOException e) {
                e.printStackTrace();
                logError("GetImageAsync(): Image " + timestamp + " getting error.");
                return null;
            }
            logDebug("GetImageAsync(): Image " + timestamp + " has been loaded.");
            mMaps[imageNumber].setImage(bitmap);
            return imageNumber;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            showData(result);
            mMaps[result].setLoaded();
        }
    }

    private class GetJsonAsync extends AsyncTask<Void, Void, String> {
        String jsonString;

        @Override
        protected String doInBackground(Void... params) {
            try {
                jsonString = NetUtils.getJsonFromServer();
            } catch (IOException e) {
                e.printStackTrace();
                logError("GetJsonAsync(): Json config getting error.");
            }
            logDebug("GetJsonAsync(): Json config has been loaded.");
            return jsonString;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            RadarTime newData = new Gson().fromJson(result, RadarTime.class);
            if (mData.getLockedState() == null || mData.getTime(0) != newData.getTime(0)) {
                mData = newData;
                Bitmap bitmap;
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.background);
                for (int i = 0; i < mData.getTimes().length; i++) {
                    mMaps[i] = new RadarBitmap(bitmap);
                    mMaps[i].setTime(mData.getTime(i));
                }
                ShowTime();
            }
            getData();
        }
    }
}
