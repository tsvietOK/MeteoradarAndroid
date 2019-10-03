package com.tsvietok.meteoradar.dev.utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;

public class DeviceUtils {
    public static int getPixelValue(Context context, float dimenId) {
        Resources resources = context.getResources();
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dimenId,
                resources.getDisplayMetrics()
        );
    }
}
