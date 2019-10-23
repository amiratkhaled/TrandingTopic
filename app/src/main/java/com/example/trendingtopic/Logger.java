package com.example.trendingtopic;

import android.util.Log;

/**
 * Created by paristote on 1/15/16.
 */
public class Logger {

    /*
        DEBUG
     */

    public static void d(String tag, String message, Throwable t) {
        if (BuildConfig.DEBUG)
            Log.d(tag, message, t);
    }

    public static void d(String tag, String message) {
        if (BuildConfig.DEBUG)
            Log.d(tag, message);
    }


    /*
        ERROR
     */

    public static void e(String tag, String message, Throwable t) {
        if (BuildConfig.DEBUG)
            Log.e(tag, message, t);
    }

    public static void e(String tag, String message) {
        if (BuildConfig.DEBUG)
            Log.e(tag, message);
    }

}
