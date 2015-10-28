package com.princecoder.nanodegree.spotifytreamer.presenter;

import com.princecoder.nanodegree.spotifytreamer.view.ITrackView;

/**
 * Created by Prinzly Ngotoum on 10/27/15.
 */
public class TrackPresenter implements ITrackPresenter{

    ITrackView mTrackview;


    public TrackPresenter(ITrackView iview){
        mTrackview=iview;
    }

    @Override
    public void showPlayerScreen() {

    }

    @Override
    public void loadTracks(String spotifyId) {
        mTrackview.findTracks(spotifyId);
    }
}
