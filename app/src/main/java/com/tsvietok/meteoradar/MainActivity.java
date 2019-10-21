package com.tsvietok.meteoradar;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.gson.Gson;
import com.tsvietok.meteoradar.utils.CustomLog;
import com.tsvietok.meteoradar.utils.LocationUtils;
import com.tsvietok.meteoradar.utils.NetUtils;
import com.tsvietok.meteoradar.utils.ScreenUtils;
import com.tsvietok.meteoradar.utils.SettingsUtils;
import com.tsvietok.meteoradar.utils.StorageUtils;
import com.tsvietok.meteoradar.utils.ThemeUtils;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String PREF_SELECTED_THEME_KEY = "selectedTheme";
    private static final String PREF_TIMELINE_POSITION_KEY = "timeLinePosition";
    private static final String PREF_FIRST_RUN_KEY = "firstRun";
    private static final String PREF_SELECTED_CITY_KEY = "selectedCity";
    private static final String PREF_CITY_CHANGED_KEY = "cityChanged";
    private static final String PREF_FOREGROUND_MAP_HEIGHT_KEY = "foregroundMapHeight";
    private static final String PREF_STATUS_TEXT_VISIBILITY_KEY = "statusTextVisibility";
    private static int firstVisibleInListView;
    private Location location;
    private ExtendedFloatingActionButton UpdateFab;
    private LinearLayout StatusText;
    private ImageView ForegroundMap;
    private TextView TimeText;
    private ImageView NoConnectionBitmap;
    private RadarBitmap[] mMaps;
    private RadarTime mData;
    private int mLastSelectedItem = -1;
    private Context context;
    private boolean mCityChanged;
    private boolean mFirstActivityStart = true;
    private CustomRecyclerView HorizontalPicker;
    private HorizontalPickerLayoutManager mLayoutManager;
    private TimeAdapter mAdapter;
    private LinearSnapHelper mSnapHelper;
    private int mStartForegroundMapHeight;
    private int mStartForegroundMapWidth;
    private ArrayList<MapInfo> mMapInfoList;
    private int mSavedForegroundMapHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CustomLog.logDebug("onCreate()");

        this.context = getApplicationContext();

        ThemeUtils.switchTheme(SettingsUtils.getIntSetting(context, PREF_SELECTED_THEME_KEY));

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        location = LocationUtils.switchCity(context,
                SettingsUtils.getIntSetting(context, PREF_SELECTED_CITY_KEY));
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
                                    SettingsUtils.saveIntSetting(context,
                                            PREF_SELECTED_CITY_KEY,
                                            item);
                                    changeCityButton.setText(location.getFullName());
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

        initMapInfoAdapter();

        MapInfoListAdapter mapInfoListAdapter = new MapInfoListAdapter(this, mMapInfoList);

        ListView mapInfoList = findViewById(R.id.mapInfoList);
        mapInfoList.setAdapter(mapInfoListAdapter);

        setHorizontalPicker();
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
            if (mData == null) {
                jsonTask = new GetJsonAsync(false);
                jsonTask.execute();
            }
        }

        UpdateFab = findViewById(R.id.UpdateFab);
        UpdateFab.setOnClickListener(view -> {
            if (NetUtils.isNetworkConnected(context)) {
                GetJsonAsync jsonTaskUpdate = new GetJsonAsync(true);
                jsonTaskUpdate.execute();
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
        ForegroundMap.getViewTreeObserver()
                .addOnGlobalLayoutListener(() -> {
                    ForegroundMap.getViewTreeObserver().removeOnGlobalLayoutListener(this::onResume);
                    if (mStartForegroundMapHeight == 0) {
                        mStartForegroundMapHeight = ForegroundMap.getWidth();
                        ForegroundMap.getLayoutParams().height = mStartForegroundMapHeight;
                    } else {
                        if (mSavedForegroundMapHeight != 0) {
                            ForegroundMap.getLayoutParams().height = mSavedForegroundMapHeight;
                        }
                    }
                });
        ForegroundMap.setOnClickListener(view -> {
            ValueAnimator anim;
            if (view.getHeight() == mStartForegroundMapHeight) {
                anim = ValueAnimator.ofInt(view.getHeight(),
                        mStartForegroundMapHeight - 400);
            } else {
                anim = ValueAnimator.ofInt(view.getHeight(),
                        mStartForegroundMapHeight);
            }
            anim.addUpdateListener(valueAnimator -> {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                layoutParams.height = val;
                view.setLayoutParams(layoutParams);
            });
            anim.setDuration(250);
            anim.start();
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        CustomLog.logDebug("onSaveInstanceState()");

        outState.putInt(PREF_TIMELINE_POSITION_KEY, mLastSelectedItem);
        outState.putBoolean(PREF_CITY_CHANGED_KEY, mCityChanged);
        if (!mCityChanged) {
            outState.putInt(PREF_FOREGROUND_MAP_HEIGHT_KEY, ForegroundMap.getHeight());
        }
        outState.putInt(PREF_STATUS_TEXT_VISIBILITY_KEY, StatusText.getVisibility());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        CustomLog.logDebug("onRestoreInstanceState()");

        mLastSelectedItem = savedInstanceState.getInt(PREF_TIMELINE_POSITION_KEY);
        mFirstActivityStart = false;
        mCityChanged = savedInstanceState.getBoolean(PREF_CITY_CHANGED_KEY);
        mSavedForegroundMapHeight = savedInstanceState.getInt(PREF_FOREGROUND_MAP_HEIGHT_KEY);
        StatusText = findViewById(R.id.StatusText);
        StatusText.setVisibility(savedInstanceState.getInt(PREF_STATUS_TEXT_VISIBILITY_KEY));
    }

    private void getMap(int i) {
        if (!mMaps[i].isLoaded()) {
            GetImageAsync imageAsync = new GetImageAsync();
            imageAsync.execute(i);
        } else {
            showData(i);
        }
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
            Toast.makeText(context, R.string.image_not_available, Toast.LENGTH_SHORT).show();
            NoConnectionBitmap = findViewById(R.id.NoConnectionBitmap);
            NoConnectionBitmap.setVisibility(View.VISIBLE);
        }
    }

    private void setHorizontalPicker() {
        CustomLog.logDebug("setHorizontalPicker()");

        HorizontalPicker = findViewById(R.id.HorizontalPicker);

        int padding = ScreenUtils.getScreenWidth(this) / 2
                - ScreenUtils.getPixelValueFromDp(this, 52);
        HorizontalPicker.setPadding(padding, 0, padding, 0);

        if (mLayoutManager == null) {
            mLayoutManager = new HorizontalPickerLayoutManager(context,
                    RecyclerView.HORIZONTAL,
                    false);
            mLayoutManager.setStackFromEnd(true);
        }
        HorizontalPicker.setLayoutManager(mLayoutManager);

        mAdapter = new TimeAdapter(context, v -> {
            int itemPosition = HorizontalPicker.getChildLayoutPosition(v);
            HorizontalPicker.smoothScrollToPosition(itemPosition);
        });
        HorizontalPicker.setAdapter(mAdapter);

        mSnapHelper = new LinearSnapHelper();
        if (HorizontalPicker.getOnFlingListener() == null) {
            mSnapHelper.attachToRecyclerView(HorizontalPicker);
        }

        firstVisibleInListView = mLayoutManager.findFirstVisibleItemPosition();
        HorizontalPicker.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int currentFirstVisible = mLayoutManager.findFirstVisibleItemPosition();
                if (currentFirstVisible != firstVisibleInListView) {
                    getMap(currentFirstVisible);
                    mLastSelectedItem = currentFirstVisible;
                }
                firstVisibleInListView = currentFirstVisible;
            }
        });
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
            MaterialAlertDialogBuilder builder =
                    new MaterialAlertDialogBuilder(MainActivity.this);
            builder.setTitle(R.string.choose_theme)
                    .setSingleChoiceItems(listItems, selectedTheme,
                            (dialog, item) -> {
                                SettingsUtils.saveIntSetting(context,
                                        PREF_SELECTED_THEME_KEY,
                                        item);
                                ThemeUtils.switchTheme(item);
                                dialog.dismiss();
                            })
                    .setNegativeButton(getString(R.string.cancel),
                            (dialog, id1) -> dialog.cancel());
            AlertDialog alert = builder.create();
            alert.show();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    private void initMapInfoAdapter() {
        mMapInfoList = new ArrayList<>();
        mMapInfoList.add(new MapInfo(getString(R.string.no_weather),
                getColor(R.color.color_no_weather)));
        mMapInfoList.add(new MapInfo(getString(R.string.laminated_clouds),
                getColor(R.color.color_laminated_clouds)));
        mMapInfoList.add(new MapInfo(getString(R.string.light_precipitation),
                getColor(R.color.color_light_precipitation)));
        mMapInfoList.add(new MapInfo(getString(R.string.medium_precipitation),
                getColor(R.color.color_medium_precipitation)));
        mMapInfoList.add(new MapInfo(getString(R.string.strong_precipitation),
                getColor(R.color.color_strong_precipitation)));
        mMapInfoList.add(new MapInfo(getString(R.string.convective_clouds),
                getColor(R.color.color_convective_clouds)));
        mMapInfoList.add(new MapInfo(getString(R.string.light_convective_rainfall),
                getColor(R.color.color_light_convective_rainfall)));
        mMapInfoList.add(new MapInfo(getString(R.string.medium_convective_rainfall),
                getColor(R.color.color_medium_convective_rainfall)));
        mMapInfoList.add(new MapInfo(getString(R.string.strong_convective_rainfall),
                getColor(R.color.color_strong_convective_rainfall)));
        mMapInfoList.add(new MapInfo(getString(R.string.thunderstorm_30_70),
                getColor(R.color.color_thunderstorm_30_70)));
        mMapInfoList.add(new MapInfo(getString(R.string.thunderstorm_70_90),
                getColor(R.color.color_thunderstorm_70_90)));
        mMapInfoList.add(new MapInfo(getString(R.string.thunderstorm_90_100),
                getColor(R.color.color_thunderstorm_90_100)));
        mMapInfoList.add(new MapInfo(getString(R.string.light_hail),
                getColor(R.color.color_light_hail)));
        mMapInfoList.add(new MapInfo(getString(R.string.medium_hail),
                getColor(R.color.color_medium_hail)));
        mMapInfoList.add(new MapInfo(getString(R.string.strong_hail),
                getColor(R.color.color_strong_hail)));
        mMapInfoList.add(new MapInfo(getString(R.string.light_squall),
                getColor(R.color.color_light_squall)));
        mMapInfoList.add(new MapInfo(getString(R.string.medium_squall),
                getColor(R.color.color_medium_squall)));
        mMapInfoList.add(new MapInfo(getString(R.string.strong_squall),
                getColor(R.color.color_strong_squall)));
    }

    private void checkRadarStatus() {
        Animation slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down);
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        StatusText = findViewById(R.id.StatusText);
        if (mData.getMode()) {
            if (StatusText.getVisibility() == View.INVISIBLE) {
                StatusText.setVisibility(View.VISIBLE);
                StatusText.startAnimation(slideDown);
            }
        } else {
            if (StatusText.getVisibility() == View.VISIBLE) {
                StatusText.setVisibility(View.INVISIBLE);
                StatusText.startAnimation(slideUp);
            }
        }
    }

    private class GetImageAsync extends AsyncTask<Integer, Void, Integer> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            CustomLog.logDebug("GetImageAsync(): Start");
        }

        @Override
        protected Integer doInBackground(Integer... number) {
            int imageNumber = number[0];
            int timestamp = mMaps[imageNumber].getTimestamp();

            Bitmap bitmap = StorageUtils.getBitmapFromStorage(context,
                    Integer.toString(timestamp),
                    location);
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
            CustomLog.logDebug("GetImageAsync(): End");
            showData(result);
        }
    }

    private class GetJsonAsync extends AsyncTask<Void, Void, String> {
        private final boolean forcedUpdate;
        private ProgressBar progressBar;

        GetJsonAsync(boolean forcedUpdate) {
            this.forcedUpdate = forcedUpdate;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            CustomLog.logDebug("GetJsonAsync(): Start");

            progressBar = findViewById(R.id.progressBar);
            progressBar.setVisibility(View.VISIBLE);
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

            progressBar.setVisibility(View.INVISIBLE);

            if (result == null) {
                Toast.makeText(context, R.string.no_server_connection, Toast.LENGTH_LONG).show();
                return;
            }

            RadarTime newData = new Gson().fromJson(result, RadarTime.class);
            if (newData.getTimes().length == 0) {
                NoConnectionBitmap = findViewById(R.id.NoConnectionBitmap);
                NoConnectionBitmap.setVisibility(View.VISIBLE);
                return;
            }

            if (mData == null
                    || mData.getTime(0) != newData.getTime(0)
                    || forcedUpdate) {
                mData = newData;
            }

            mMaps = new RadarBitmap[mData.getTimes().length];

            for (int i = 0; i < mData.getTimes().length; i++) {
                mMaps[i] = new RadarBitmap(location);
                mMaps[i].setTime(mData.getTime(i));
            }

            mAdapter.refreshData(mData.getTimeString());

            if (forcedUpdate || mCityChanged) {
                mLastSelectedItem = mData.getTimeString().length;
            }
            if (forcedUpdate) {
                HorizontalPicker.smoothScrollToPosition(mLastSelectedItem);
            } else {
                HorizontalPicker.scrollToPosition(mLastSelectedItem);
            }

            checkRadarStatus();

            if (mFirstActivityStart || mCityChanged || forcedUpdate) {
                mFirstActivityStart = false;
                mCityChanged = false;
            }

            if (forcedUpdate) {
                Toast.makeText(context, R.string.updated, Toast.LENGTH_SHORT).show();
            }
            CustomLog.logDebug("GetJsonAsync(): End");
        }
    }
}
