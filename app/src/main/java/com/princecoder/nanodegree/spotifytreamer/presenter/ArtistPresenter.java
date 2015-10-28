package com.princecoder.nanodegree.spotifytreamer.presenter;

import android.content.Context;

import com.princecoder.nanodegree.spotifytreamer.view.IHomeView;

/**
 * Created by Prinzly Ngotoum on 10/27/15.
 */
public class ArtistPresenter implements IArtistPresenter {

    IHomeView homeView;
    Context mContext;

    public ArtistPresenter(IHomeView iView, Context context){
        homeView=iView;
        mContext=context;
    }

    @Override
    public void loadArtist(String name) {
        homeView.findArtists(name);
    }

    @Override
    public void onClickItem(int position) {
        homeView.displayTracks(position);
    }
}
