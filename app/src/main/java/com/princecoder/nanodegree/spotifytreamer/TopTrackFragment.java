package com.princecoder.nanodegree.spotifytreamer;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.princecoder.nanodegree.spotifytreamer.adapter.TrackAdapter;
import com.princecoder.nanodegree.spotifytreamer.model.ArtistModel;
import com.princecoder.nanodegree.spotifytreamer.model.IElement;
import com.princecoder.nanodegree.spotifytreamer.model.TrackModel;
import com.princecoder.nanodegree.spotifytreamer.utils.L;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.RetrofitError;

/**
 * @author prinzlyngotoum
 */
public class TopTrackFragment extends Fragment {

    // The ListView
    private ListView mTrackListView;

    // List of Artists
    private ArrayList<IElement> mTraks=new ArrayList<>();

    // My adapter
    private TrackAdapter mAdapter;

    //Tag
    private String TAG=getClass().getSimpleName();

    //Listener
    OnTrackSelectedListener mListener;

    //Associated artist
    private ArtistModel mArtist=new ArtistModel();

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnTrackSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(TAG + activity.getString(R.string.track_selected_class_cast_exception_message));
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView=inflater.inflate(R.layout.fragment_top_track, container, false);

        mTrackListView=(ListView)rootView.findViewById(R.id.track_listview);
        mAdapter=new TrackAdapter(getActivity(),R.layout.track_row_item,R.id.topTxt,new ArrayList<IElement>());

        mTrackListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // Let the activity do the job for us
                // We have the list of tracks to play and the selected track by the user
                mListener.onTrackSelectedListener(mTraks,position);
            }
        });

        // Set the adapter
        mTrackListView.setAdapter(mAdapter);

        Intent intent=getActivity().getIntent();
        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) { // We are in single pane mode
            ArtistModel artist = (ArtistModel)intent.getSerializableExtra(Intent.EXTRA_TEXT);
            new TopTrackAsyncTask().execute(artist.getSpotifyId());
        }
        else{ // We are in dual pane mode
            Bundle args=getArguments();
            if(args!=null){
                mArtist = (ArtistModel)args.getSerializable("Artist");
                new TopTrackAsyncTask().execute(mArtist.getSpotifyId());
            }
        }

        // Inflate the layout for this fragment
        return rootView;
    }

    public TrackAdapter getAdapter(){
        return mAdapter;
    }


    /**
     *  Check if we are online
     */
    protected boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return (netInfo != null && netInfo.isConnectedOrConnecting());
    }

    // If the user selected a new Artist
    public void onArtistChange(String spotifyId){
        new TopTrackAsyncTask().execute(spotifyId);
    }

    /**
     * Top track asyncTask
     */
    private class TopTrackAsyncTask extends AsyncTask<String ,Void,Tracks> {

        private SpotifyApi mSpotifyApi = new SpotifyApi();
        private SpotifyService mSpotifyService = mSpotifyApi.getService();
        private ProgressDialog mProgressDialog;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if(isOnline()) {
                mProgressDialog = new ProgressDialog(getActivity());
                mProgressDialog.setTitle(getResources().getString(R.string.progress_dialog_message));
                mProgressDialog.show();
            }else{
                L.m(TAG,getResources().getString(R.string.no_internet));
                // I dismiss the progress dialog
                mProgressDialog.dismiss();
                L.toast(getActivity(),getResources().getString(R.string.no_internet));
            }
        }

        @Override
        protected Tracks doInBackground(String... params) {
            HashMap<String,Object> queryString = new HashMap<>();
            try{
                queryString.put(SpotifyService.COUNTRY, Locale.getDefault().getCountry());
                return  mSpotifyService.getArtistTopTrack(params[0], queryString);
            }
            catch (RetrofitError error){
                L.m(TAG,error.getMessage());
            }
            return null;
        }


        @Override
        protected void onPostExecute(Tracks tracks) {
            mAdapter.clear();
            mTraks.clear();
            if (tracks == null || tracks.tracks.size() == 0) {
                mProgressDialog.dismiss();
                L.toast(getActivity(),getResources().getString(R.string.no_track));
            }
            else{
                int count =0;
                for (Track track : tracks.tracks) {
                    TrackModel t = new TrackModel();
                    t.setTrackName(track.name);
                    t.setPrevUrl(track.preview_url);
                    t.setAlbum(track.album.name);
                    t.setArtist((track.artists.size()>0?track.artists.get(0).name:"Unknown Artist"));
                    if(track.album.images!=null && track.album.images.size()>0){
                        t.setAlbThumb(track.album.images.get(0).url);
                    }
                    mAdapter.add(t);
                    mTraks.add(t);
                    count++;
//                    if(count==10)break;
                }
                // dismiss the progress dialog
                if (mProgressDialog!=null)
                    mProgressDialog.dismiss();
            }
        }
    }

    public interface OnTrackSelectedListener{

        void onTrackSelectedListener(ArrayList<IElement> list,int position);
    }

}
