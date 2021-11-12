package com.tsvietok.meteoradar.dev.utils;

import android.content.Context;
import android.content.res.Resources;

import com.tsvietok.meteoradar.dev.Location;
import com.tsvietok.meteoradar.dev.R;
import com.tsvietok.meteoradar.dev.RadarMapType;

public class LocationUtils {
    public static Location switchCity(Context context, int key) {
        CustomLog.logDebug("switchCity()");

        Resources resources = context.getResources();
        Location location = null;
        switch (key) {
            case 0:
                location = new Location(resources.getString(R.string.kiev), "kiev", RadarMapType.KIEV);
                break;
            case 1:
                location = new Location(resources.getString(R.string.minsk), "minsk");
                break;
            case 2:
                location = new Location(resources.getString(R.string.brest), "brest");
                break;
            case 3:
                location = new Location(resources.getString(R.string.gomel), "gomel");
                break;
            case 4:
                location = new Location(resources.getString(R.string.smolensk), "smolensk");
                break;
            case 5:
                location = new Location(resources.getString(R.string.bryansk), "bryansk");
                break;
            case 6:
                location = new Location(resources.getString(R.string.kursk), "kursk");
                break;
            case 7:
                location = new Location(resources.getString(R.string.velikiye_luki), "vluki");
                break;
            case 8:
                location = new Location(resources.getString(R.string.zaporozhye), "zaporozhye");
                break;
            case 9:
                location = new Location(resources.getString(R.string.vitebsk), "vitebsk");
                break;
            case 10:
                location = new Location(resources.getString(R.string.grodno), "grodno");
                break;
            default:
                break;
        }
        return location;
    }
}
