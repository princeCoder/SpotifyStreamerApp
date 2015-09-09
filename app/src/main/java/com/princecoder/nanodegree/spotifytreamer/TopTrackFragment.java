package com.princecoder.nanodegree.spotifytreamer;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.princecoder.nanodegree.spotifytreamer.adapter.TrackAdapter;
import com.princecoder.nanodegree.spotifytreamer.model.ArtistModel;
import com.princecoder.nanodegree.spotifytreamer.model.IElement;
import com.princecoder.nanodegree.spotifytreamer.model.MediaModel;
import com.princecoder.nanodegree.spotifytreamer.model.TrackModel;
import com.princecoder.nanodegree.spotifytreamer.utils.L;
import com.princecoder.nanodegree.spotifytreamer.utils.Utilities;

import java.util.ArrayList;
import java.util.HashMap;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.RetrofitError;

/**
 * @author prinzlyngotoum
 */
public class TopTrackFragment extends Fragment {

    //Log element
    public final String LOG_TAG =getClass().getSimpleName();

    // The ListView
    private ListView mTrackListView;

    // My adapter
    private TrackAdapter mAdapter;

    //Tag
    private String TAG=getClass().getSimpleName();

    //Listener
    OnTrackSelectedListener mListener;

    //Associated artist
    private ArtistModel mArtist=new ArtistModel();

    //Position
    private int mPosition;

    //Position Tag
    private static final String SELECTED_TRACK="SELECTED_TRACK";

    // Track tag
    private static final String TRACKS="TRACKS";

    //Artist Tag
    public static final String SELECTED_ARTIST="SELECTED_ARTIST";

    //To know if the fragment is recreated or not
    private boolean isCreated=true;

    //Used by the shareIntent
    private static final String SPOTIFY_SHARE_HASHTAG = " #SpotifyStreamer";

    private String mTrackString="";

    ShareActionProvider mShareActionProvider;

    MenuItem menuItem;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnTrackSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(TAG + activity.getString(R.string.track_selected_class_cast_exception_message));
        }
    }

    public TopTrackFragment(){
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_track, menu);

        // Retrieve the share menu item

        //This is to create a shareActionProvider
        menuItem = menu.findItem(R.id.menu_item_share);

        if(MediaModel.getInstance().getCurrentTrack()!=null){
            mShareActionProvider = new ShareActionProvider(getActivity());
            mShareActionProvider.setShareIntent(createShareSpotifyIntent());
            MenuItemCompat.setActionProvider(menuItem, mShareActionProvider);
        }
    }


    private Intent createShareSpotifyIntent(){
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        if(MediaModel.getInstance().getCurrentTrack()!=null){
            shareIntent.putExtra(Intent.EXTRA_TEXT,
                    MediaModel.getInstance().getCurrentTrack().getTrackName() + SPOTIFY_SHARE_HASHTAG);
        }
        else{
            L.toast(getActivity(), "You have to play a song");
        }
        return shareIntent;
    }

    private void shareCurrentSongIntent() {

        if(MediaModel.getInstance().getCurrentTrack()!=null){
            startActivity(createShareSpotifyIntent());
        }
        else{
            L.toast(getActivity(), "You have to play a song in order to share it");
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.action_playing){
            // Instantiate the nowPlaying fragment
            NowPlayingFragment fragment=new NowPlayingFragment();
            if(Utilities.isOnline(getActivity())){ // Make sure we start playing if we have internet
                MediaModel model=MediaModel.getInstance();
                if(model.isMediaPlayerHasStarted()){
                    model.setNowPlayingTriggeredByUser(false);
                    fragment.show(getActivity().getSupportFragmentManager(), "now playing");
                }
                else{
                    L.toast(getActivity(), getResources().getString(R.string.now_playing_message));
                }
            }
            else {
                L.toast(getActivity(),getResources().getString(R.string.no_internet));
            }
            return true;
        }
        else if(item.getItemId()==R.id.menu_item_share) {

            shareCurrentSongIntent();
        }
        return false;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView=inflater.inflate(R.layout.fragment_top_track, container, false);

        mTrackListView = (ListView) rootView.findViewById(R.id.track_listview);
        mAdapter = new TrackAdapter(getActivity(), R.layout.track_row_item, R.id.topTxt, new ArrayList<IElement>());

        mTrackListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Let the activity do the job for us
                // We have the list of tracks to play and the selected track by the user
                mListener.onTrackSelectedListener(mAdapter.getElements(), position);
                mPosition = position;
            }
        });

        // Set the adapter
        mTrackListView.setAdapter(mAdapter);

        if(savedInstanceState!=null){

            L.m(TAG, "------------- savedInstanceState !=null --------------");

            if(savedInstanceState.containsKey(SELECTED_ARTIST)) {
                mArtist = (ArtistModel) savedInstanceState.getSerializable(SELECTED_ARTIST);
                mAdapter.clear();
            }
            if(savedInstanceState.containsKey(TRACKS)) {
                mAdapter.setElements((ArrayList<IElement>) savedInstanceState.getSerializable(TRACKS));
            }
            if(savedInstanceState.containsKey(SELECTED_TRACK)) {
                mPosition=savedInstanceState.getInt(SELECTED_TRACK);
                if (mPosition != ListView.INVALID_POSITION) {
                    mTrackListView.smoothScrollToPosition(mPosition);
                }
            }

            isCreated=false;

        }
        // Inflate the layout for this fragment
        return rootView;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //I save the current artist and his tracks

        outState.putSerializable(SELECTED_ARTIST,mArtist);
        outState.putSerializable(TRACKS, mAdapter.getElements());

        //I save the current position
        if(mPosition!=ListView.INVALID_POSITION){
            outState.putInt(SELECTED_TRACK,mPosition);
        }

    }

    public TrackAdapter getAdapter(){
        return mAdapter;
    }


    @Override
    public void onResume() {
        super.onResume();
        if(MediaModel.getInstance().getCurrentTrack()!=null){
            mShareActionProvider = new ShareActionProvider(getActivity());
            mShareActionProvider.setShareIntent(createShareSpotifyIntent());
            MenuItemCompat.setActionProvider(menuItem, mShareActionProvider);
        }
        if(isCreated){
            L.m(TAG, "------------- TopTrackFragment onResume reloading datas--------------");
            displayTracK();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(MediaModel.getInstance().getCurrentTrack()!=null){
            mShareActionProvider = new ShareActionProvider(getActivity());
            mShareActionProvider.setShareIntent(createShareSpotifyIntent());
            MenuItemCompat.setActionProvider(menuItem, mShareActionProvider);
        }
        isCreated=true;
    }

    private void displayTracK(){
        Intent intent=getActivity().getIntent();
        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) { // We are in single pane mode
            ArtistModel artist = (ArtistModel)intent.getSerializableExtra(Intent.EXTRA_TEXT);
            new TopTrackAsyncTask().execute(artist.getSpotifyId());

            L.m(LOG_TAG,"------------ We are in single pane mode -----------------");
        }
        else{ // We are in dual pane mode

            L.m(LOG_TAG,"------------ We are in Dual pane mode -----------------");
            Bundle args=getArguments();
            if(args!=null){
                mArtist = (ArtistModel)args.getSerializable(SELECTED_ARTIST);
                new TopTrackAsyncTask().execute(mArtist.getSpotifyId());
            }
        }

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
            if(Utilities.isOnline(getActivity())) {
                mProgressDialog = new ProgressDialog(getActivity());
                mProgressDialog.setTitle(getResources().getString(R.string.progress_dialog_message));
                mProgressDialog.show();
            }else{
                L.m(TAG,getResources().getString(R.string.no_internet));
                L.toast(getActivity(),getResources().getString(R.string.no_internet));
                // dismiss the progress dialog
                if (mProgressDialog!=null && mProgressDialog.isShowing())
                    mProgressDialog.dismiss();
            }
        }



        //Get the Location from prefences

        private String getCountryFromPreference(){
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String location = prefs.getString(getString(R.string.pref_country_key),
                    getString(R.string.pref_country_default));
            return location;
        }

        @Override
        protected Tracks doInBackground(String... params) {
            HashMap<String,Object> queryString = new HashMap<>();
            try{
                if(Utilities.isOnline(getActivity())){
                    //queryString.put(SpotifyService.COUNTRY, Locale.getDefault().getCountry());
                    queryString.put(SpotifyService.COUNTRY,getCountryFromPreference());
                    return  mSpotifyService.getArtistTopTrack(params[0], queryString);
                }
                else{
                    L.toast(getActivity(),getResources().getString(R.string.no_internet));
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
            mAdapter.clear();
            if (tracks == null || tracks.tracks.size() == 0) {
                if(Utilities.isOnline(getActivity()))
                    L.toast(getActivity(),getResources().getString(R.string.no_track));
                else
                    L.toast(getActivity(),getResources().getString(R.string.no_internet));
            }
            else{
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
                    mAdapter.add(t);
                }
            }
            // dismiss the progress dialog
            if (mProgressDialog!=null && mProgressDialog.isShowing())
                mProgressDialog.dismiss();
        }
    }

    public interface OnTrackSelectedListener{
        void onTrackSelectedListener(ArrayList<IElement> list,int position);
    }

}
