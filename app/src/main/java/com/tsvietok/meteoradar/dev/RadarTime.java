package com.tsvietok.meteoradar.dev;

import com.google.gson.annotations.SerializedName;

import java.text.SimpleDateFormat;
import java.util.Date;

class RadarTime {
    @SerializedName("is_down")
    private Boolean mIsDown;
    @SerializedName("times")
    private int[] mTimes;

    String[] getTimeString() {
        String[] times = new String[mTimes.length];
        for (int i = 0; i < mTimes.length; i++) {
            Date time = new Date((long) mTimes[i] * 1000);
            times[i] = new SimpleDateFormat("HH:mm").format(time);
        }
        return times;
    }

    Boolean isServerDown() {
        return mIsDown;
    }

    int[] getTimes() {
        return mTimes;
    }

    int getTime(int i) {
        return mTimes[i];
    }
}
