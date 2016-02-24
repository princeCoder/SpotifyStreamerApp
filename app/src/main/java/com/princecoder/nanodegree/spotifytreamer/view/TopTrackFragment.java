package com.princecoder.nanodegree.spotifytreamer.view;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
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

import com.princecoder.nanodegree.spotifytreamer.R;
import com.princecoder.nanodegree.spotifytreamer.adapter.TrackAdapter;
import com.princecoder.nanodegree.spotifytreamer.model.ArtistModel;
import com.princecoder.nanodegree.spotifytreamer.model.IElement;
import com.princecoder.nanodegree.spotifytreamer.model.MediaModel;
import com.princecoder.nanodegree.spotifytreamer.presenter.ITrackPresenter;
import com.princecoder.nanodegree.spotifytreamer.presenter.TrackPresenter;
import com.princecoder.nanodegree.spotifytreamer.service.TopTrackAsyncTask;
import com.princecoder.nanodegree.spotifytreamer.utils.L;
import com.princecoder.nanodegree.spotifytreamer.utils.Utilities;

import java.util.ArrayList;

/**
 * @author prinzlyngotoum
 */
public class TopTrackFragment extends Fragment implements ITrackView{

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

    //Position of the last selected track
    //We will need it to highlight that row element
    private int mPosition;

    //Position Tag
    private static final String SELECTED_TRACK="SELECTED_TRACK";

    // Track tag
    private static final String TRACKS="TRACKS";

    ArrayList<IElement>mListOfTracks=new ArrayList<>();

    //Artist Tag
    public static final String SELECTED_ARTIST="SELECTED_ARTIST";

    //To know if the fragment is recreated or not
    private boolean isCreated=true;

    //Used by the shareIntent
    private static final String SPOTIFY_SHARE_HASHTAG = " #SpotifyStreamer";

    private String mTrackString="";

    ShareActionProvider mShareActionProvider;

    MenuItem menuItem;

    ITrackPresenter mPresenter;

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
        mPresenter=new TrackPresenter(this);
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

        mTrackListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Let the activity do the job for us
                // We have the list of tracks to play and the selected track by the user
                mListener.onTrackSelectedListener(mAdapter.getElements(), position);

                mPosition = position;
            }
        });


        if(savedInstanceState!=null){

            L.m(TAG, "------------- savedInstanceState !=null --------------");

            if(savedInstanceState.containsKey(SELECTED_ARTIST)) {
                mArtist = (ArtistModel) savedInstanceState.getSerializable(SELECTED_ARTIST);
            }
            if(savedInstanceState.containsKey(TRACKS)) {

                mListOfTracks=(ArrayList<IElement>) savedInstanceState.getSerializable(TRACKS);
                displayTracks(mListOfTracks);
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
        if(mAdapter!=null)
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
            mPresenter.loadTracks(artist.getSpotifyId());

            L.m(LOG_TAG,"------------ We are in single pane mode -----------------");
        }
        else{ // We are in dual pane mode

            L.m(LOG_TAG,"------------ We are in Dual pane mode -----------------");
            Bundle args=getArguments();
            if(args!=null){
                mArtist = (ArtistModel)args.getSerializable(SELECTED_ARTIST);
                mPresenter.loadTracks(mArtist.getSpotifyId());
            }
        }

    }



    @Override
    public void displayTracks(ArrayList<IElement> tracks) {
        mAdapter = new TrackAdapter(getActivity(), R.layout.track_row_item, R.id.topTxt, tracks);
        mTrackListView.setAdapter(mAdapter);

    }

    @Override
    public void findTracks(String spotifyId) {
        new TopTrackAsyncTask(getActivity(),this).execute(spotifyId);
    }

    @Override
    public void onItemClick(int position) {

    }

    public interface OnTrackSelectedListener{
        void onTrackSelectedListener(ArrayList<IElement> list,int position);
    }

}
