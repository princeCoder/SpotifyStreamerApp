package com.princecoder.nanodegree.spotifytreamer.service;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.princecoder.nanodegree.spotifytreamer.R;
import com.princecoder.nanodegree.spotifytreamer.model.IElement;
import com.princecoder.nanodegree.spotifytreamer.model.TrackModel;
import com.princecoder.nanodegree.spotifytreamer.utils.L;
import com.princecoder.nanodegree.spotifytreamer.utils.Utilities;
import com.princecoder.nanodegree.spotifytreamer.view.ITrackView;

import java.util.ArrayList;
import java.util.HashMap;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.RetrofitError;

/**
 * Created by Prinzly Ngotoum on 10/27/15.
 */
public class TopTrackAsyncTask extends AsyncTask<String ,Void,Tracks> {

    private SpotifyApi mSpotifyApi = new SpotifyApi();
    private SpotifyService mSpotifyService = mSpotifyApi.getService();
    private ProgressDialog mProgressDialog;
    private Context mContext;
    private ITrackView mView;
    private String TAG=getClass().getSimpleName();

    public TopTrackAsyncTask(Context context, ITrackView view){
        this.mContext=context;
        mView=view;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if(Utilities.isOnline(mContext)) {
            mProgressDialog = new ProgressDialog(mContext);
            mProgressDialog.setTitle(mContext.getResources().getString(R.string.progress_dialog_message));
            mProgressDialog.show();
        }else{
            L.m(TAG, mContext.getResources().getString(R.string.no_internet));
            L.toast(mContext,mContext.getResources().getString(R.string.no_internet));
            // dismiss the progress dialog
            if (mProgressDialog!=null && mProgressDialog.isShowing())
                mProgressDialog.dismiss();
        }
    }



    //Get the Location from prefences

    private String getCountryFromPreference(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        String location = prefs.getString(mContext.getString(R.string.pref_country_key),
                mContext.getString(R.string.pref_country_default));
        return location;
    }

    @Override
    protected Tracks doInBackground(String... params) {
        HashMap<String,Object> queryString = new HashMap<>();
        try{
            if(Utilities.isOnline(mContext)){
                //queryString.put(SpotifyService.COUNTRY, Locale.getDefault().getCountry());
                queryString.put(SpotifyService.COUNTRY,getCountryFromPreference());
                return  mSpotifyService.getArtistTopTrack(params[0], queryString);
            }
            else{
                L.toast(mContext,mContext.getString(R.string.no_internet));
                return null;
            }
        }
        catch (RetrofitError error){
            L.m(TAG,error.getMessage());
        }
        return null;
    }


    @Override
    protected void onPostExecute(Tracks tracks) {
        if (tracks == null || tracks.tracks.size() == 0) {
            if(Utilities.isOnline(mContext))
                L.toast(mContext,mContext.getResources().getString(R.string.no_track));
            else
                L.toast(mContext,mContext.getResources().getString(R.string.no_internet));
        }
        else{
            ArrayList<IElement>list=new ArrayList<>();
            for (Track track : tracks.tracks) {
                TrackModel t = new TrackModel();
                t.setTrackName(track.name);
                t.setPrevUrl(track.preview_url);
                t.setAlbum(track.album.name);
                t.setArtist((track.artists.size() > 0 ? track.artists.get(0).name : "Unknown Artist"));
                t.setExternalUrl(track.external_urls.get("spotify"));
                if(track.album.images!=null && track.album.images.size()>0){
                    t.setAlbThumb(track.album.images.get(0).url);
                }
                list.add(t);
//                mAdapter.add(t);
            }
            mView.displayTracks(list);
        }
        // dismiss the progress dialog
        if (mProgressDialog!=null && mProgressDialog.isShowing())
            mProgressDialog.dismiss();
    }
}