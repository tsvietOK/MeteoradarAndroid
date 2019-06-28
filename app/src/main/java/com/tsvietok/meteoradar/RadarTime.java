package com.tsvietok.meteoradar;

import java.text.SimpleDateFormat;
import java.util.Date;

public class RadarTime {
    public int timestamp;
    public int timeout;
    public Boolean locked;
    public Boolean is_down;
    public int[] times;

    String[] getTime() {
        String[] _times = new String[times.length];
        for (int i = 0; i < times.length; i++) {
            Date _time = new Date((long) times[i] * 1000);
            _times[i] = new SimpleDateFormat("HH:mm").format(_time);
        }
        return _times;
    }
}
