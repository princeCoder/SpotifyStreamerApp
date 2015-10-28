package com.princecoder.nanodegree.spotifytreamer.view;

import com.princecoder.nanodegree.spotifytreamer.model.IElement;

import java.util.ArrayList;

/**
 * Created by Prinzly Ngotoum on 10/27/15.
 */
public interface ITrackView {

    void displayTracks(ArrayList<IElement> tracks);
    void findTracks(String spotifyId);
    void onItemClick(int position);
}
