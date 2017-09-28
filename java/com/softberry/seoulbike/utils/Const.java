package com.softberry.seoulbike.utils;

import android.os.Environment;

/**
 * Created by parkjs on 2016-02-19.
 */
public class Const {

    // 발급받은 appKey
    public static final String TMAP_APP_KEY = "e7887535-1f8c-3fb4-a748-224bbe78452d";
    public static final String OLLEH_NAVI_ACTION_NAME = "kt.navi.OLLEH_NAVIGATION";

    private static boolean mDebugMode = false;

    public static void toogleDebugMode() {
        mDebugMode = !mDebugMode;
        if (mDebugMode == true) {
        } else {
        }
    }

    public static boolean getDebugMode() {
        return mDebugMode;
    }
}
