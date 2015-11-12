package com.android.phoneadapter.utils;

import android.content.Context;
import android.util.DisplayMetrics;

public class Utils {

    public static int getDisplayWidth(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return metrics.widthPixels;
    }

    public static int getDisplayHeight(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return metrics.heightPixels + 48;
    }
}
