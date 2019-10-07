package com.tsvietok.meteoradar.dev.utils;

import android.content.Context;
import android.graphics.Bitmap;

import com.tsvietok.meteoradar.dev.Location;
import com.tsvietok.meteoradar.dev.R;

public class LocationUtils {
    private final static String DEFAULT_PART_FILE_NAME = "_background.png";

    public static Location switchCity(Context context, int key) {
        CustomLog.logDebug("switchCity()");

        Bitmap backgroundMap;
        Location location = null;
        switch (key) {
            case 0:
                backgroundMap = BitmapUtils.getBitmapFromAsset(context, "kiev" + DEFAULT_PART_FILE_NAME);
                location = new Location(context.getResources().getString(R.string.kiev), "ukbb", "kiev", backgroundMap);
                break;
            case 1:
                backgroundMap = BitmapUtils.getBitmapFromAsset(context, "minsk" + DEFAULT_PART_FILE_NAME);
                location = new Location(context.getResources().getString(R.string.minsk), "ummn", "minsk", backgroundMap);
                break;
            case 2:
                backgroundMap = BitmapUtils.getBitmapFromAsset(context, "brest" + DEFAULT_PART_FILE_NAME);
                location = new Location(context.getResources().getString(R.string.brest), "umbb", "brest", backgroundMap);
                break;
            case 3:
                backgroundMap = BitmapUtils.getBitmapFromAsset(context, "gomel" + DEFAULT_PART_FILE_NAME);
                location = new Location(context.getResources().getString(R.string.gomel), "umgg", "gomel", backgroundMap);
                break;
            case 4:
                backgroundMap = BitmapUtils.getBitmapFromAsset(context, "smolensk" + DEFAULT_PART_FILE_NAME);
                location = new Location(context.getResources().getString(R.string.smolensk), "rudl", "smolensk", backgroundMap);
                break;
            case 5:
                backgroundMap = BitmapUtils.getBitmapFromAsset(context, "bryansk" + DEFAULT_PART_FILE_NAME);
                location = new Location(context.getResources().getString(R.string.bryansk), "rudb", "bryansk", backgroundMap);
                break;
            case 6:
                backgroundMap = BitmapUtils.getBitmapFromAsset(context, "kursk" + DEFAULT_PART_FILE_NAME);
                location = new Location(context.getResources().getString(R.string.kursk), "raku", "kursk", backgroundMap);
                break;
            case 7:
                backgroundMap = BitmapUtils.getBitmapFromAsset(context, "luki" + DEFAULT_PART_FILE_NAME);
                location = new Location(context.getResources().getString(R.string.velikiye_luki), "ravl", "luki", backgroundMap);
                break;
            default:
                break;
        }
        return location;
    }
}
