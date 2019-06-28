package com.tsvietok.meteoradar;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;


public class MainActivity extends AppCompatActivity {
    public static final String PREFS_NAME = "com.tsvietok.meteoradar.preferences";
    public String SelectedThemeKey = "Selected_theme";
    public RadarBitmap[] Maps = new RadarBitmap[]{null, null, null, null, null,
            null, null, null, null, null};
    public RadarTime data = null;
    public String LOG_TAG = "Meteoradar";
    public Boolean DEBUG = true;
    public int last_image = 9;

    ExtendedFloatingActionButton UpdateFab;
    SeekBar TimeLine;
    TextView StatusText;
    ImageView ForegroundMap;
    TextView TimeText;
    ImageView NoInternetImage;
    LinearLayout TimeLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        logDebug("onCreate()");
        switchTheme(getIntSetting(SelectedThemeKey));
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
            if (isNetworkConnected()) {
                data = new RadarTime();
                logDebug("First start, getting Json...");
                GetJsonAsync jsonTask = new GetJsonAsync();
                jsonTask.execute();
            } else {
                Toast.makeText(getApplicationContext(), R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
                logError(getString(R.string.no_internet_connection));
                NoInternetImage = findViewById(R.id.NoInternetImage);
                NoInternetImage.setVisibility(View.VISIBLE);
            }

        } else {
            logDebug("Json exists, showing data...");
            getData();
        }

        UpdateFab = findViewById(R.id.UpdateFab);
        UpdateFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isNetworkConnected()) {
                    data = new RadarTime();
                    GetJsonAsync jsonTask = new GetJsonAsync();
                    jsonTask.execute();
                    last_image = 9;
                    Toast.makeText(getApplicationContext(), R.string.updated, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
                    logError(getString(R.string.no_internet_connection));
                    NoInternetImage = findViewById(R.id.NoInternetImage);
                    NoInternetImage.setVisibility(View.VISIBLE);
                }
            }
        });
        ForegroundMap = findViewById(R.id.ForegroundMap);
        ForegroundMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ForegroundMap.getMeasuredHeight() == 1024) {
                    ValueAnimator anim = ValueAnimator.ofInt(ForegroundMap.getMeasuredHeight(), 600);
                    anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator valueAnimator) {
                            int val = (Integer) valueAnimator.getAnimatedValue();
                            ViewGroup.LayoutParams layoutParams = ForegroundMap.getLayoutParams();
                            layoutParams.height = val;
                            ForegroundMap.setLayoutParams(layoutParams);
                        }
                    });
                    anim.setDuration(250);
                    anim.start();
                } else {
                    ValueAnimator anim = ValueAnimator.ofInt(ForegroundMap.getMeasuredHeight(), 1024);
                    anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator valueAnimator) {
                            int val = (Integer) valueAnimator.getAnimatedValue();
                            ViewGroup.LayoutParams layoutParams = ForegroundMap.getLayoutParams();
                            layoutParams.height = val;
                            ForegroundMap.setLayoutParams(layoutParams);
                        }
                    });
                    anim.setDuration(250);
                    anim.start();
                }
            }
        });
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        logDebug("onSaveInstanceState()");
        if (data != null) {
            outState.putIntArray("times", data.times);
            outState.putBoolean("is_down", data.is_down);
            outState.putBoolean("locked", data.locked);
            outState.putInt("timeout", data.timeout);
            outState.putInt("timestamp", data.timestamp);
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
            data.times = savedInstanceState.getIntArray("times");
            data.is_down = savedInstanceState.getBoolean("is_down");
            data.locked = savedInstanceState.getBoolean("locked");
            data.timeout = savedInstanceState.getInt("timeout");
            data.timestamp = savedInstanceState.getInt("timestamp");
            last_image = savedInstanceState.getInt("TimeLinePosition");
            ShowTime();
        }
        if (savedInstanceState.getBoolean("Maps_saved")) {
            Maps = new RadarBitmap[]{null, null, null, null, null,
                    null, null, null, null, null};
            Maps = (RadarBitmap[]) savedInstanceState.getSerializable("Maps");
        }
    }

    private void getData() {
        logDebug("getData()");
        StatusText = findViewById(R.id.StatusText);
        if (data.is_down)
            StatusText.setVisibility(View.VISIBLE);
        else
            StatusText.setVisibility(View.INVISIBLE);
        if ((Maps[last_image] == null) || (Maps[last_image].getTimestamp() != data.times[last_image])) {
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

                if ((Maps[i] == null) || (Maps[i].getTimestamp() != data.times[i])) {
                    if (isNetworkConnected()) {
                        GetImageAsync imageTask = new GetImageAsync();
                        imageTask.execute(i);
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
                        logError(getString(R.string.no_internet_connection));
                        NoInternetImage = findViewById(R.id.NoInternetImage);
                        NoInternetImage.setVisibility(View.VISIBLE);
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
            RadarBitmap map = new RadarBitmap();
            map.setBackgroundImage(BitmapFactory.decodeResource(getResources(), R.drawable.background));
            try {
                bmp = getBitmapFromServer("http://veg.by/meteoradar/data/ukbb/images/" + data.times[number[0]] + ".png");
            } catch (IOException e) {
                e.printStackTrace();
                logError("GetImageAsync(): Image " + data.times[number[0]] + " getting error.");
                return null;
            }
            logDebug("GetImageAsync(): Image " + data.times[number[0]] + " has been loaded.");
            map.setImage(bmp);
            map.setTime(data.times[number[0]]);
            Maps[number[0]] = map;
            return number[0];
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            showData(result);
        }
    }

    private class GetJsonAsync extends AsyncTask<Void, Void, String> {
        String jsonString;
        String url = "http://veg.by/meteoradar/kiev/update.json";

        @Override
        protected String doInBackground(Void... params) {
            try {
                jsonString = getJsonFromServer(url);
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
        for (int i = 0; i < data.times.length; i++) {
            TextView textView = new TextView(getApplicationContext());
            textView.setText(data.getTime()[i]);
            textView.setTextSize(12);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, getPixelValue(getApplicationContext(), getResources().getDimension(R.dimen.time_margin_end)), 0);
            textView.setLayoutParams(params);
            textView.setGravity(Gravity.CENTER);
            textView.setTypeface(Typeface.MONOSPACE);
            textView.setTextColor(getColor(R.color.colorTextDayNight));
            TimeLayout.addView(textView);
        }
    }

    private static int getPixelValue(Context context, float dimenId) {
        Resources resources = context.getResources();
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dimenId,
                resources.getDisplayMetrics()
        );
    }

    private String getJsonFromServer(String url) throws IOException {
        logDebug("getJsonFromServer()");

        URL jsonUrl = new URL(url);
        URLConnection dc = jsonUrl.openConnection();

        dc.setConnectTimeout(5000);
        dc.setReadTimeout(5000);

        BufferedReader inputStream = new BufferedReader(new InputStreamReader(
                dc.getInputStream()));

        return inputStream.readLine();
    }

    private Bitmap getBitmapFromServer(String url) throws IOException {
        logDebug("getBitmapFromServer()");

        URL bitmapUrl = new URL(url);
        URLConnection dc = bitmapUrl.openConnection();

        dc.setConnectTimeout(5000);
        dc.setReadTimeout(5000);
        dc.connect();

        return BitmapFactory.decodeStream(dc.getInputStream());
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
            final int SelectedTheme = getIntSetting(SelectedThemeKey);
            final String[] listItems = {
                    getString(R.string.follow_system_theme),
                    getString(R.string.light_theme),
                    getString(R.string.dark_theme)};
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(MainActivity.this);
            builder.setTitle(R.string.choose_theme)
                    .setCancelable(false)
                    .setSingleChoiceItems(listItems, SelectedTheme,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int item) {
                                    saveIntSetting(SelectedThemeKey, item);
                                }
                            })
                    .setPositiveButton("ОК",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    switchTheme(getIntSetting(SelectedThemeKey));
                                }
                            });
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

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

    private void logError(String message) {
        if (DEBUG)
            Log.e(LOG_TAG, message);
    }

    private void logDebug(String message) {
        if (DEBUG)
            Log.d(LOG_TAG, message);
    }
}
