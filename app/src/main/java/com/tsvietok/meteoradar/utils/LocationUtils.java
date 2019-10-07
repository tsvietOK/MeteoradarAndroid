package com.tsvietok.meteoradar.utils;

import android.content.Context;

import com.tsvietok.meteoradar.Location;
import com.tsvietok.meteoradar.R;

public class LocationUtils {
    public static Location switchCity(Context context, int key) {
        CustomLog.logDebug("switchCity()");

        Location location = null;
        switch (key) {
            case 0:
                location = new Location(context.getResources().getString(R.string.kiev), "ukbb", "kiev");
                break;
            case 1:
                location = new Location(context.getResources().getString(R.string.minsk), "ummn", "minsk");
                break;
            case 2:
                location = new Location(context.getResources().getString(R.string.brest), "umbb", "brest");
                break;
            case 3:
                location = new Location(context.getResources().getString(R.string.gomel), "umgg", "gomel");
                break;
            case 4:
                location = new Location(context.getResources().getString(R.string.smolensk), "rudl", "smolensk");
                break;
            case 5:
                location = new Location(context.getResources().getString(R.string.bryansk), "rudb", "bryansk");
                break;
            case 6:
                location = new Location(context.getResources().getString(R.string.kursk), "raku", "kursk");
                break;
            case 7:
                location = new Location(context.getResources().getString(R.string.velikiye_luki), "ravl", "luki");
                break;
            default:
                break;
        }
        return location;
    }
}
