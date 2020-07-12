package com.tsvietok.meteoradar.dev.test;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.tsvietok.meteoradar.R;
import com.tsvietok.meteoradar.utils.BitmapUtils;

import java.util.ArrayList;

public class ColorTestActivity extends AppCompatActivity {
    private ArrayList<ColorTest> mColorTestList;
    private int[] originalValues = new int[]{
            -3158065,
            -2171170,
            -6493556,
            -10682812,
            -12270980,
            -6495492,
            -12283140,
            -15967492,
            -10202428,
            -224604,
            -246220,
            -3930604,
            -202228,
            -222700,
            -5485052,
            -2315524,
            -1287428,
            -4448516
    };

    /*private int[] darkValues = new int[]{
            -9079435,
            -14606047,
            -12883197,
            -10960125,
            -12727736,
            -16558517,
            -16541502,
            -16534273,
            -13020231,
            -10353878,
            -2751683,
            -48225,
            -27901,
            -118269,
            -37280,
            -16579772,
            -11336793,
            -11533327
    };*/
    private int[] darkValues;
    private Bitmap[] invertedBitmaps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_test);

        darkValues = new int[]{
                getColor(R.color.color_no_weather),
                getColor(R.color.color_laminated_clouds),
                getColor(R.color.color_light_precipitation),
                getColor(R.color.color_medium_precipitation),
                getColor(R.color.color_strong_precipitation),
                getColor(R.color.color_convective_clouds),
                getColor(R.color.color_light_convective_rainfall),
                getColor(R.color.color_medium_convective_rainfall),
                getColor(R.color.color_strong_convective_rainfall),
                getColor(R.color.color_thunderstorm_30_70),
                getColor(R.color.color_thunderstorm_70_90),
                getColor(R.color.color_thunderstorm_90_100),
                getColor(R.color.color_light_hail),
                getColor(R.color.color_medium_hail),
                getColor(R.color.color_strong_hail),
                getColor(R.color.color_light_squall),
                getColor(R.color.color_medium_squall),
                getColor(R.color.color_strong_squall)
        };
        invertedBitmaps = new Bitmap[darkValues.length];

        for (int i = 0; i < originalValues.length; i++) {
            invertedBitmaps[i] = BitmapUtils.CreateBitmap(90, 40, originalValues[i]);
            invertedBitmaps[i] = BitmapUtils.InvertBitmap(invertedBitmaps[i], this);
        }


        initMapInfoAdapter();
        ColorTestListAdapter mColorTestListAdapter = new ColorTestListAdapter(this, mColorTestList);
        ListView testColorList = findViewById(R.id.testColorList);
        testColorList.setAdapter(mColorTestListAdapter);
    }

    private void initMapInfoAdapter() {
        mColorTestList = new ArrayList<>();

        for (int i = 0; i < originalValues.length; i++) {
            ColorTest bitmapsSet = new ColorTest(
                    BitmapUtils.CreateBitmap(90, 40, originalValues[i]),
                    BitmapUtils.CreateBitmap(90, 40, darkValues[i]),
                    invertedBitmaps[i]);
            mColorTestList.add(bitmapsSet);
            System.out.println(bitmapsSet.getInvertedBitmapColorHexValue());
        }
    }
}