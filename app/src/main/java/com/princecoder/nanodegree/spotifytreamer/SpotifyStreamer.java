package com.princecoder.nanodegree.spotifytreamer;

import android.app.Application;

/**
 * Created by Prinzly Ngotoum on 7/27/15.
 */
public class SpotifyStreamer extends Application {

    private static volatile SpotifyStreamer mInstance;

    public SpotifyStreamer() {
        super();
        mInstance = this;
    }

    public static SpotifyStreamer getInstance() {
        if(mInstance == null) {
            mInstance=new SpotifyStreamer();
        }
        return mInstance;
    }

}
