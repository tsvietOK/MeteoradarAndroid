package com.tsvietok.meteoradar;

import com.google.gson.annotations.SerializedName;

import java.text.SimpleDateFormat;
import java.util.Date;

class RadarTime {
    @SerializedName("timestamp")
    private int mTimestamp;
    @SerializedName("timeout")
    private int mTimeout;
    @SerializedName("locked")
    private Boolean mLocked;
    @SerializedName("is_down")
    private Boolean mIsDown;
    @SerializedName("times")
    private int[] mTimes;

    String[] getTimeString() {
        String[] times = new String[mTimes.length];
        for (int i = 0; i < mTimes.length; i++) {
            Date _time = new Date((long) mTimes[i] * 1000);
            times[i] = new SimpleDateFormat("HH:mm").format(_time);
        }
        return times;
    }

    int getTimestamp() {
        return mTimestamp;
    }

    void setTimestamp(int timestamp) {
        this.mTimestamp = timestamp;
    }

    int getTimeout() {
        return mTimeout;
    }

    void setTimeout(int timeout) {
        this.mTimeout = timeout;
    }

    Boolean getLockedState() {
        return mLocked;
    }

    void setLockedState(Boolean locked) {
        this.mLocked = locked;
    }

    Boolean getMode() {
        return mIsDown;
    }

    void setMode(Boolean isDown) {
        this.mIsDown = isDown;
    }

    int[] getTimes() {
        return mTimes;
    }

    void setTimes(int[] times) {
        this.mTimes = times;
    }

    int getTime(int i) {
        return mTimes[i];
    }
}
