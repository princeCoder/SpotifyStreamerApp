package com.princecoder.nanodegree.spotifytreamer.service;

/**
 * Created by Prinzly Ngotoum on 10/27/15.
 */

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.princecoder.nanodegree.spotifytreamer.R;
import com.princecoder.nanodegree.spotifytreamer.model.ArtistModel;
import com.princecoder.nanodegree.spotifytreamer.model.IElement;
import com.princecoder.nanodegree.spotifytreamer.utils.L;
import com.princecoder.nanodegree.spotifytreamer.utils.Utilities;
import com.princecoder.nanodegree.spotifytreamer.view.IHomeView;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import retrofit.RetrofitError;

/**
 * Artist asyncTask
 */
public  class ArtistAsyncTask extends AsyncTask<String,Void,List<Artist>> {

    private SpotifyApi api = new SpotifyApi();
    private SpotifyService spotify = api.getService();
    private ProgressDialog mProgressDialog;
    private Context context;
    private String TAG=getClass().getSimpleName();
    private IHomeView homeView;

    public ArtistAsyncTask(Context c, IHomeView view){
        context=c;
        homeView=view;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if(Utilities.isOnline(context)) {
            mProgressDialog = ProgressDialog.show(context, context.getResources().getString(R.string.progress_dialog_title), context.getResources().getString(R.string.progress_dialog_message));
            mProgressDialog.show();
        }else{
            L.m(TAG, context.getResources().getString(R.string.no_internet));
            // I dismiss the progress dialog
            mProgressDialog.dismiss();
            //L.toast(getActivity(),getResources().getString(R.string.no_internet));
            homeView.updateEmptyView(context.getResources().getString(R.string.no_internet));
        }
    }


    @Override
    protected List<Artist> doInBackground(String... params) {

        try {
            String searchString = params[0];
            return spotify.searchArtists(searchString).artists.items;
        } catch(RetrofitError ex){
            L.m(TAG, ex.getMessage());
        }
        return null;
    }


    @Override
    protected void onPostExecute(List<Artist> artists) {
        if(artists!=null && artists.size() > 0) {
            homeView.getmListOfArtist().clear();
            ArrayList<IElement> list=new ArrayList<>();
            for (Artist ar : artists) {
                ArtistModel artist = new ArtistModel();
                artist.setName(ar.name);
                artist.setSpotifyId(ar.id);
                if(ar.images.size()>0){
                    artist.setArtThumb(ar.images.get(0).url);
                }
                list.add(artist);
                homeView.getmListOfArtist().add(artist);
            }

            homeView.displayArtists(list);
            if (mProgressDialog!=null && mProgressDialog.isShowing())
                mProgressDialog.dismiss();
        }
        else{
            // I dismiss the progress dialog
            if (mProgressDialog!=null && mProgressDialog.isShowing())
                mProgressDialog.dismiss();

            // I notify the user no data has been found
            homeView.updateEmptyView(context.getResources().getString(R.string.no_artist));
        }
    }
}