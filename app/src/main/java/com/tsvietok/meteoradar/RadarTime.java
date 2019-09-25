package com.tsvietok.meteoradar;

import java.text.SimpleDateFormat;
import java.util.Date;

class RadarTime {
    private static final String domain = "http://radar.veg.by/";
    private int timestamp;
    private int timeout;
    private Boolean locked;
    private Boolean is_down;
    private int[] times;

    String[] getTimeString() {
        String[] _times = new String[times.length];
        for (int i = 0; i < times.length; i++) {
            Date _time = new Date((long) times[i] * 1000);
            _times[i] = new SimpleDateFormat("HH:mm").format(_time);
        }
        return _times;
    }

    int getTimestamp() {
        return timestamp;
    }

    void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    int getTimeout() {
        return timeout;
    }

    void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    Boolean getLockedState() {
        return locked;
    }

    void setLockedState(Boolean locked) {
        this.locked = locked;
    }

    Boolean getMode() {
        return is_down;
    }

    void setMode(Boolean is_down) {
        this.is_down = is_down;
    }

    int[] getTimes() {
        return times;
    }

    int getTime(int i) {
        return times[i];
    }

    void setTimes(int[] times) {
        this.times = times;
    }

    String getImageLink(int i) {
        return domain + "data/ukbb/images/" + times[i] + ".png";
    }
}
