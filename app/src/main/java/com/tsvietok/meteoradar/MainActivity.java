package com.tsvietok.meteoradar;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
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
import java.util.concurrent.ExecutionException;



public class MainActivity extends AppCompatActivity {
    public static final String PREFS_NAME = "com.tsvietok.meteoradar.preferences";
    public String SelectedThemeKey = "Selected_theme";
    public RadarBitmap[] Maps = new RadarBitmap[]{null, null, null, null, null,
            null, null, null, null, null};
    public RadarTime data = new RadarTime();

    ExtendedFloatingActionButton UpdateFab;
    SeekBar TimeLine;
    TextView StatusText;
    ImageView ForegroundMap;
    TextView TimeText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SwitchTheme(GetIntSetting(SelectedThemeKey));

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        try {
            GetData(true);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        UpdateFab = findViewById(R.id.UpdateFab);
        UpdateFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    GetData(false);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt("TimeLinePosition", TimeLine.getProgress());
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        TimeLine.setProgress(savedInstanceState.getInt("TimeLinePosition"));
    }

    public void GetData(boolean firstStart) throws ExecutionException, InterruptedException {
        GetJsonAsync jsonTask = new GetJsonAsync();
        jsonTask.execute();
        data = new Gson().fromJson(jsonTask.get(), RadarTime.class);
        StatusText = findViewById(R.id.StatusText);
        if (data.is_down) StatusText.setVisibility(View.VISIBLE);
        else StatusText.setVisibility(View.INVISIBLE);

        GetImage(9);
        TimeLine = findViewById(R.id.TimeLine);
        TimeLine.setProgress(9, true);
        TimeLine.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                GetImage(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        if (!firstStart) Toast.makeText(this, R.string.updated, Toast.LENGTH_SHORT).show();
    }

    private void GetImage(int i) {
        ForegroundMap = findViewById(R.id.ForegroundMap);
        TimeText = findViewById(R.id.TimeText);
        if (Maps[i] == null) {
            GetImageAsync imageTask = new GetImageAsync();
            imageTask.execute(data.times[i]);
            Bitmap BackgroundMap = BitmapFactory.decodeResource(getResources(), R.drawable.background);
            try {
                Maps[i] = new RadarBitmap(imageTask.get(), data.times[i], BackgroundMap);
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        TimeText.setText(Maps[i].getTime());
        int currentNightMode = getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;
        switch (currentNightMode) {
            case Configuration.UI_MODE_NIGHT_NO:
                ForegroundMap.setImageBitmap(Maps[i].getImage());
                break;
            case Configuration.UI_MODE_NIGHT_YES:
                ForegroundMap.setImageBitmap(Maps[i].getNightImage());
                break;
        }
    }

    public class GetImageAsync extends AsyncTask<Integer, Void, Bitmap> {
        Bitmap bmp;

        @Override
        protected Bitmap doInBackground(Integer... number) {
            try {
                bmp = getBitmapFromServer("http://veg.by/meteoradar/data/ukbb/images/" + number[0] + ".png");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return bmp;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
        }
    }

    public class GetJsonAsync extends AsyncTask<Void, Void, String> {
        String jsonString;
        String url = "http://veg.by/meteoradar/kiev/update.json";

        @Override
        protected String doInBackground(Void... params) {
            try {
                jsonString = getJsonFromServer(url);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return jsonString;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }
    }

    public static String getJsonFromServer(String url) throws IOException {

        BufferedReader inputStream = null;

        URL jsonUrl = new URL(url);
        URLConnection dc = jsonUrl.openConnection();

        dc.setConnectTimeout(5000);
        dc.setReadTimeout(5000);

        inputStream = new BufferedReader(new InputStreamReader(
                dc.getInputStream()));

        // read the JSON results into a string
        return inputStream.readLine();
    }

    public static Bitmap getBitmapFromServer(String url) throws IOException {

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
            final int SelectedTheme = GetIntSetting(SelectedThemeKey);
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
                                    SaveIntSetting(SelectedThemeKey, item);
                                }
                            })
                    .setPositiveButton("ОК",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    SwitchTheme(GetIntSetting(SelectedThemeKey));
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

    public void SwitchTheme(int key) {
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

    public void SaveIntSetting(String key, int value) {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, 0).edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public int GetIntSetting(String key) {
        SharedPreferences sharedPref = getSharedPreferences(PREFS_NAME, 0);
        return sharedPref.getInt(key, 0);
    }

}
