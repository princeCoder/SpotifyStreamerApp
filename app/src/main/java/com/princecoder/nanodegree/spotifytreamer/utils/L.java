package com.princecoder.nanodegree.spotifytreamer.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by prinzlyngotoum on 7/1/15.
 */
public class L {
    /**
     * To log a specific information
     * @param message
     */
    public static void m(String tag, String message) {
        Log.d(tag, message);
    }

    /**
     * Display a Toast message
     * @param c --> The Context
     * @param message --> Message to get displayed
     */
    public static void toast(Context c, String message) {
        Toast.makeText(c, message, Toast.LENGTH_LONG).show();
    }
}
