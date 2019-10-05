package com.tsvietok.meteoradar.dev;

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
import com.tsvietok.meteoradar.dev.utils.NetUtils;

import static com.tsvietok.meteoradar.dev.utils.CustomLog.logDebug;
import static com.tsvietok.meteoradar.dev.utils.CustomLog.logError;
import static com.tsvietok.meteoradar.dev.utils.DeviceUtils.getPixelValue;
import static com.tsvietok.meteoradar.dev.utils.NetUtils.getBitmapFromServer;
import static com.tsvietok.meteoradar.dev.utils.NetUtils.isNetworkConnected;
import static com.tsvietok.meteoradar.dev.utils.SettingsUtils.getBooleanSetting;
import static com.tsvietok.meteoradar.dev.utils.SettingsUtils.getIntSetting;
import static com.tsvietok.meteoradar.dev.utils.SettingsUtils.saveBooleanSetting;
import static com.tsvietok.meteoradar.dev.utils.SettingsUtils.saveIntSetting;
import static com.tsvietok.meteoradar.dev.utils.StorageUtils.getBitmapFromStorage;
import static com.tsvietok.meteoradar.dev.utils.StorageUtils.getJsonFromStorage;
import static com.tsvietok.meteoradar.dev.utils.StorageUtils.removeUnusedBitmap;
import static com.tsvietok.meteoradar.dev.utils.StorageUtils.saveBitmapToStorage;
import static com.tsvietok.meteoradar.dev.utils.StorageUtils.saveJsonToStorage;
import static com.tsvietok.meteoradar.dev.utils.ThemeUtils.switchTheme;

public class MainActivity extends AppCompatActivity {
    private static final String PREF_SELECTED_THEME_KEY = "selectedTheme";
    private static final String PREF_TIMELINE_POSITION_KEY = "timeLinePosition";
    private static final String PREF_FIRST_RUN_KEY = "firstRun";
    private static final String JSON_CONFIG_FILE_NAME = "config";

    ExtendedFloatingActionButton UpdateFab;
    SeekBar TimeLine;
    TextView StatusText;
    ImageView ForegroundMap;
    TextView TimeText;
    ImageView NoConnectionBitmap;
    LinearLayout TimeLayout;

    private RadarBitmap[] mMaps;
    private RadarTime mData;
    private int mLastImageNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        logDebug("onCreate()");

        switchTheme(getIntSetting(getApplicationContext(), PREF_SELECTED_THEME_KEY));

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mMaps = new RadarBitmap[10];
        mLastImageNumber = mMaps.length - 1;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        logDebug("onDestroy()");

        if (mData != null) removeUnusedBitmap(getApplicationContext(), mData.getTimes());
    }

    @Override
    public void onResume() {
        super.onResume();
        logDebug("onResume()");

        GetJsonAsync jsonTask;

        if (getBooleanSetting(getApplicationContext(), PREF_FIRST_RUN_KEY)) {
            if (isNetworkConnected(getApplicationContext())) {
                jsonTask = new GetJsonAsync(true);
                jsonTask.execute();

                saveBooleanSetting(getApplicationContext(), PREF_FIRST_RUN_KEY, false);
            } else {
                Toast.makeText(getApplicationContext(),
                        R.string.no_internet_connection,
                        Toast.LENGTH_SHORT).show();

                NoConnectionBitmap = findViewById(R.id.NoConnectionBitmap);
                NoConnectionBitmap.setVisibility(View.VISIBLE);

                logError("Internet connection is not available");
            }
        } else {
            jsonTask = new GetJsonAsync(false);
            jsonTask.execute();
        }

        UpdateFab = findViewById(R.id.UpdateFab);
        UpdateFab.setOnClickListener(view -> {
            if (isNetworkConnected(getApplicationContext())) {
                GetJsonAsync jsonTaskUpdate = new GetJsonAsync(true);
                jsonTaskUpdate.execute();

                mLastImageNumber = mMaps.length - 1;

                Toast.makeText(getApplicationContext(),
                        R.string.updated,
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        R.string.no_internet_connection,
                        Toast.LENGTH_SHORT).show();

                NoConnectionBitmap = findViewById(R.id.NoConnectionBitmap);
                NoConnectionBitmap.setVisibility(View.VISIBLE);

                logError("Internet connection is not available");
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
        logDebug("onSaveInstanceState()");

        if (mData != null) {
            TimeLine = findViewById(R.id.TimeLine);
            mLastImageNumber = TimeLine.getProgress();
            outState.putInt(PREF_TIMELINE_POSITION_KEY, mLastImageNumber);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        logDebug("onRestoreInstanceState()");

        mLastImageNumber = savedInstanceState.getInt(PREF_TIMELINE_POSITION_KEY);

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
                        GetImageAsync imageAsync = new GetImageAsync();
                        imageAsync.execute(i);

                        Toast.makeText(getApplicationContext(),
                                R.string.no_internet_connection,
                                Toast.LENGTH_SHORT).show();

                        NoConnectionBitmap = findViewById(R.id.NoConnectionBitmap);
                        NoConnectionBitmap.setVisibility(View.VISIBLE);

                        logError("Internet connection is not available");
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
            Toast.makeText(getApplicationContext(),
                    R.string.no_server_connection,
                    Toast.LENGTH_SHORT).show();
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
        @Override
        protected Integer doInBackground(Integer... number) {
            int imageNumber = number[0];
            int timestamp = mMaps[imageNumber].getTimestamp();

            Bitmap bitmap =
                    getBitmapFromStorage(getApplicationContext(), Integer.toString(timestamp));
            if (bitmap == null) {
                bitmap = getBitmapFromServer(mMaps[imageNumber].getImageLink());
                if (bitmap == null)
                    return -1;
                logDebug("GetImageAsync(): Image " + timestamp + " has been loaded.");

                saveBitmapToStorage(getApplicationContext(), bitmap, timestamp);
            } else {
                logDebug("GetImageAsync(): Image " + timestamp + " already exists.");
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
        private boolean forcedUpdate;

        GetJsonAsync(boolean forcedUpdate) {
            this.forcedUpdate = forcedUpdate;
        }

        @Override
        protected String doInBackground(Void... params) {
            String jsonString = getJsonFromStorage(getApplicationContext(), JSON_CONFIG_FILE_NAME);
            if (jsonString == null || jsonString.length() == 0 || forcedUpdate) {
                jsonString = NetUtils.getJsonFromServer();
                if (jsonString == null)
                    return null;
                logDebug("GetJsonAsync(): New JSON config has been loaded.");

                saveJsonToStorage(getApplicationContext(), jsonString, JSON_CONFIG_FILE_NAME);
            } else {
                logDebug("GetJsonAsync(): JSON config already exists.");
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
            if (mData == null || mData.getTime(0) != newData.getTime(0)) {
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
