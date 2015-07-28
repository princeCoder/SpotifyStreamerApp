package com.princecoder.nanodegree.spotifytreamer.utils;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Prinzly Ngotoum on 7/27/15.
 */
public class SongsManager {

    private ArrayList<HashMap<String, String>> songsList = new ArrayList<>();


    /**
     * function that return the list of tracks
     * @return ArrayList
     */
    public ArrayList<HashMap<String, String>> getTracks(){
        // return songs list array
        return songsList;
    }
}
