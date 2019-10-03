package com.tsvietok.meteoradar.dev;

import android.animation.ValueAnimator;
import android.content.SharedPreferences;
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
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.gson.Gson;
import com.tsvietok.meteoradar.utils.NetUtils;

import java.io.IOException;

import static com.tsvietok.meteoradar.utils.CustomLog.*;
import static com.tsvietok.meteoradar.utils.DeviceUtils.getPixelValue;
import static com.tsvietok.meteoradar.utils.NetUtils.*;

public class MainActivity extends AppCompatActivity {
    public static final String PREFS_NAME = "com.tsvietok.meteoradar.preferences";
    public String selectedTheme = "Selected_theme";
    public RadarBitmap[] Maps = new RadarBitmap[10];
    public RadarTime data = null;
    public int last_image = 9;

    ExtendedFloatingActionButton UpdateFab;
    SeekBar TimeLine;
    TextView StatusText;
    ImageView ForegroundMap;
    TextView TimeText;
    ImageView NoConnectionBitmap;
    LinearLayout TimeLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        logDebug("onCreate()");
        switchTheme(getIntSetting(selectedTheme));
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public void onResume() {
        super.onResume();
        logDebug("onResume()");
        if (data == null) {
            if (isNetworkConnected(getApplicationContext())) {
                data = new RadarTime();
                logDebug("First start, getting Json...");
                GetJsonAsync jsonTask = new GetJsonAsync();
                jsonTask.execute();
            } else {
                Toast.makeText(getApplicationContext(), R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
                logError(getString(R.string.no_internet_connection));
                NoConnectionBitmap = findViewById(R.id.NoConnectionBitmap);
                NoConnectionBitmap.setVisibility(View.VISIBLE);
            }

        } else {
            logDebug("Json exists, showing data...");
            getData();
        }

        UpdateFab = findViewById(R.id.UpdateFab);
        UpdateFab.setOnClickListener(view -> {
            if (isNetworkConnected(getApplicationContext())) {
                data = new RadarTime();
                GetJsonAsync jsonTask = new GetJsonAsync();
                jsonTask.execute();
                last_image = 9;
                Toast.makeText(getApplicationContext(), R.string.updated, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
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
        if (data != null) {
            outState.putIntArray("times", data.getTimes());
            outState.putBoolean("is_down", data.getMode());
            outState.putBoolean("locked", data.getLockedState());
            outState.putInt("timeout", data.getTimeout());
            outState.putInt("timestamp", data.getTimestamp());
            TimeLine = findViewById(R.id.TimeLine);
            outState.putInt("TimeLinePosition", TimeLine.getProgress());
            outState.putBoolean("Data_saved", true);
        }
        if (Maps != null) {
            outState.putSerializable("Maps", Maps);
            outState.putBoolean("Maps_saved", true);
        }
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        logDebug("onRestoreInstanceState()");
        if (savedInstanceState.getBoolean("Data_saved")) {
            data = new RadarTime();
            data.setTimes(savedInstanceState.getIntArray("times"));
            data.setMode(savedInstanceState.getBoolean("is_down"));
            ;
            data.setLockedState(savedInstanceState.getBoolean("locked"));
            data.setTimeout(savedInstanceState.getInt("timeout"));
            data.setTimestamp(savedInstanceState.getInt("timestamp"));
            last_image = savedInstanceState.getInt("TimeLinePosition");
            ShowTime();
        }
        if (savedInstanceState.getBoolean("Maps_saved")) {
            Maps = new RadarBitmap[10];
            Maps = (RadarBitmap[]) savedInstanceState.getSerializable("Maps");
        }
    }

    private void getData() {
        logDebug("getData()");
        StatusText = findViewById(R.id.StatusText);
        StatusText.setVisibility(data.getMode() ? View.VISIBLE : View.INVISIBLE);
        if ((Maps[last_image] == null) || (Maps[last_image].getTimestamp() != data.getTime(last_image))) {
            GetImageAsync imageTask = new GetImageAsync();
            imageTask.execute(last_image);
        } else {
            showData(last_image);
        }

        TimeLine = findViewById(R.id.TimeLine);
        TimeLine.setProgress(last_image, true);
        TimeLine.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                if ((Maps[i] == null) || (Maps[i].getTimestamp() != data.getTime(i))) {
                    if (isNetworkConnected(getApplicationContext())) {
                        GetImageAsync imageTask = new GetImageAsync();
                        imageTask.execute(i);
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
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
        TimeText.setText(Maps[number].getTime());
        int currentNightMode = getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;
        switch (currentNightMode) {
            case Configuration.UI_MODE_NIGHT_NO:
                ForegroundMap.setImageBitmap(Maps[number].getImage());
                break;
            case Configuration.UI_MODE_NIGHT_YES:
                ForegroundMap.setImageBitmap(Maps[number].getNightImage());
                break;
        }
    }

    private class GetImageAsync extends AsyncTask<Integer, Void, Integer> {
        Bitmap bmp;

        @Override
        protected Integer doInBackground(Integer... number) {
            int imageNumber = number[0];
            RadarBitmap map = new RadarBitmap();
            map.setBackgroundImage(BitmapFactory.decodeResource(getResources(), R.drawable.background));
            try {
                bmp = getBitmapFromServer(data.getImageLink(imageNumber));
            } catch (IOException e) {
                e.printStackTrace();
                logError("GetImageAsync(): Image " + data.getTime(imageNumber) + " getting error.");
                return null;
            }
            logDebug("GetImageAsync(): Image " + data.getTime(imageNumber) + " has been loaded.");
            map.setImage(bmp);
            map.setTime(data.getTime(imageNumber));
            Maps[imageNumber] = map;
            return imageNumber;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            showData(result);
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
            data = new Gson().fromJson(result, RadarTime.class);
            ShowTime();
            getData();
        }
    }

    private void ShowTime() {
        TimeLayout = findViewById(R.id.TimeLayout);
        TimeLayout.removeAllViews();
        for (int i = 0; i < data.getTimes().length; i++) {
            TextView timeLayoutText = new TextView(getApplicationContext());
            timeLayoutText.setText(data.getTimeString()[i]);
            timeLayoutText.setTextSize(12);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, getPixelValue(getApplicationContext(), getResources().getDimension(R.dimen.time_margin_end)), 0);
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
            final int SelectedTheme = getIntSetting(selectedTheme);
            final String[] listItems = {
                    getString(R.string.follow_system_theme),
                    getString(R.string.light_theme),
                    getString(R.string.dark_theme)};
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(MainActivity.this);
            builder.setTitle(R.string.choose_theme)
                    .setSingleChoiceItems(listItems, SelectedTheme,
                            (dialog, item1) -> {
                                saveIntSetting(selectedTheme, item1);
                                switchTheme(getIntSetting(selectedTheme));
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

    private void switchTheme(int key) {
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

    private void saveIntSetting(String key, int value) {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, 0).edit();
        editor.putInt(key, value);
        editor.commit();
    }

    private int getIntSetting(String key) {
        SharedPreferences sharedPref = getSharedPreferences(PREFS_NAME, 0);
        return sharedPref.getInt(key, 0);
    }


}
