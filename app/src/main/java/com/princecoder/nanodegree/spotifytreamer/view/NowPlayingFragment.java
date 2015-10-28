package com.princecoder.nanodegree.spotifytreamer.view;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.princecoder.nanodegree.spotifytreamer.service.MediaPlayerService;
import com.princecoder.nanodegree.spotifytreamer.R;
import com.princecoder.nanodegree.spotifytreamer.model.MediaModel;
import com.princecoder.nanodegree.spotifytreamer.model.TrackModel;
import com.princecoder.nanodegree.spotifytreamer.utils.L;
import com.princecoder.nanodegree.spotifytreamer.utils.Utilities;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
/**
 *  Dialog fragment use to show the Media player
 *
 *  If it is the first time I display the fragment, I will take data from the previous screen
 *  Otherwise, I retreive data from  the model
 */
public class NowPlayingFragment extends DialogFragment implements  SeekBar.OnSeekBarChangeListener{

    //Log element
    public final String LOG_TAG =getClass().getSimpleName();

    // Play Button
    private ImageButton btnPlayPause;

    // Button Forward
    private ImageButton btnForward;

    // Button Backward
    private ImageButton btnBackward;

    // Button Next
    private ImageButton btnNext;

    // Button Previous
    private ImageButton btnPrevious;

    // Button Repeat
    private ImageButton btnRepeat;

    // Button Repeat
    private ImageButton btnSpotify;

    // Button Shuffle
    private ImageButton btnShuffle;

    // Thumbnail
    private ImageView songThumb;

    //Progress bar
    private SeekBar songProgressBar;

    // Use to display the Album
    private TextView songAlbumLabel;

    //Use to display the artist
    private TextView songArtistLabel;

    //Use to display the track title
    private TextView songTitleLabel;

    // To display the song duration
    private TextView songCurrentDurationLabel;

    //To display the Total song duration
    private TextView songTotalDurationLabel;

    //Media Model
    MediaModel mModel;

    // Media Player
    private MediaPlayer mp;

    // Handler to update UI timer, progress bar etc,.
    private Handler mHandler = new Handler();

    //Utilities
    private Utilities utils;

    //My Current Tracks
    private TrackModel mCurrentTrack =new TrackModel();

    //List of tracks
    private ArrayList<TrackModel>mListTracks=new ArrayList<>();

    //List of tracks tag
    public static final String LIST_TRACKS="LIST_TRACKS";

    //Track index tag
    public static final String TRACK_INDEX="TRACK_INDEX";

    //Broadcast receiver
    private BroadcastReceiver updateUIReceiver;

    private BroadcastReceiver progressBarStartReceiver;

    private BroadcastReceiver progressBarStopReceiver;

    private BroadcastReceiver errorReceiver;

    private BroadcastReceiver repeatReceiver;

    private BroadcastReceiver shuffleReceiver;

    private BroadcastReceiver playPauseReceiver;

    // Intent
    private Intent mIntent;

    //use to handle the starting
    private boolean isFirstTime=true;

    private boolean isProgressbarStoped=true;

    // Play/Pause Tag use to send a message to the service that we pressed the Play/Pause button
    public static String PLAY_PAUSE="RESET_PLAY_PAUSE";


    /**
     *  // Register a broadcast receiver to update the UI
     */
    private void registerUpdateUIBroadcast(){
        Intent intent = getActivity().registerReceiver(updateUIReceiver,
                new IntentFilter(MediaPlayerService.SERVICE_UPDATE_UI));
        if (intent != null) {
            updateUIReceiver.onReceive(getActivity(), intent);
        }
    }

    /***
     *  // Register a broadcast receiver to start the progressBar
     */
    private void registerProgressBarStartBroadcast(){
        Intent intent = getActivity().registerReceiver(progressBarStartReceiver,
                new IntentFilter(MediaPlayerService.SERVICE_UPDATE_PROGRESS_BAR_START));
        if (intent != null) {
            progressBarStartReceiver.onReceive(getActivity(), intent);
        }
    }


    /***
     * // Register a broadcast receiver to stop the progressBar
     */
    private void registerProgressBarStopBroadcast(){
        Intent intent = getActivity().registerReceiver(progressBarStopReceiver,
                new IntentFilter(MediaPlayerService.SERVICE_UPDATE_PROGRESS_BAR_STOP));
        if (intent != null) {
            progressBarStopReceiver.onReceive(getActivity(), intent);
        }
    }

    /***
     * // Register a broadcast receiver to update the play/Pause button
     */
    private void registerPlayPauseBroadcast(){
        Intent intent = getActivity().registerReceiver(playPauseReceiver,
                new IntentFilter(MediaPlayerService.SERVICE_UPDATE_PLAY_PAUSE));
        if (intent != null) {
            playPauseReceiver.onReceive(getActivity(), intent);
        }
    }

    /***
     * // Register a broadcast receiver to update the repeat button
     */
    private void registerRepeatBroadcast(){
        Intent intent = getActivity().registerReceiver(repeatReceiver,
                new IntentFilter(MediaPlayerService.SERVICE_UPDATE_REPEAT));
        if (intent != null) {
            repeatReceiver.onReceive(getActivity(), intent);
        }
    }


    /***
     * // Register a broadcast receiver to update the shuffle button
     */
    private void registerShuffleBroadcast(){
        Intent intent = getActivity().registerReceiver(shuffleReceiver,
                new IntentFilter(MediaPlayerService.SERVICE_UPDATE_SHUFFLE));
        if (intent != null) {
            shuffleReceiver.onReceive(getActivity(), intent);
        }
    }

    /***
     * // Register a broadcast receiver for error handling
     */
    private void registerErrorBroadcast(){
        Intent intent =getActivity().registerReceiver(errorReceiver,
                new IntentFilter(MediaPlayerService.SERVICE_ERROR_NAME));
        if (intent != null) {
            errorReceiver.onReceive(getActivity(), intent);
        }
    }


    /***
     * //Initialize The Media player values
     */
    private void initMediaPlayer() {
        // The model
        mModel= MediaModel.getInstance();

        //Make sure I have the same Media player
        mp = mModel.getMediaPlayer();

        utils = new Utilities();

        //Get the current track
        mCurrentTrack =mModel.getCurrentTrack();

        //Get the list of tracks
        mListTracks=mModel.getTrackList();

        // Listeners
        songProgressBar.setOnSeekBarChangeListener(this);
    }

    /**
     * //Reset the playPause button
     */
    private void resetPlayPauseButton(){
        if(mp!=null){
            if(mp.isPlaying())
                btnPlayPause.setImageResource(R.drawable.btn_pause);
            else
                btnPlayPause.setImageResource(R.drawable.btn_play);
        }
    }

    /**
     * //Reset the Repeat button
     */
    private void resetRepeatButton(){
        if(mp!=null){
            if(mModel.isRepeat())
                btnRepeat.setImageResource(R.mipmap.img_btn_repeat_pressed);
            else
                btnRepeat.setImageResource(R.mipmap.img_btn_repeat);
        }
    }

    /**
     * //Reset the Shuffle button
     */
    private void resetShuffleButton(){
        if(mp!=null){
            if(mModel.isShuffle())
                btnShuffle.setImageResource(R.mipmap.img_btn_shuffle_pressed);
            else
                btnShuffle.setImageResource(R.mipmap.img_btn_shuffle);
        }
    }


    /**
     * //Update UI elements
     * @param songIndex the current song index
     */
    private void updateUI(int songIndex) {
        //Reset the playPause button
        resetPlayPauseButton();
        resetShuffleButton();
        resetRepeatButton();

        if(songIndex<mListTracks.size()){
            // Displaying Song title
            String songTitle = mListTracks.get(songIndex).getTrackName();
            String songAlbum=mListTracks.get(songIndex).getAlbum();
            String songArtist=mListTracks.get(songIndex).getArtist();

            songTitleLabel.setText(songTitle);
            songAlbumLabel.setText(songAlbum);
            songArtistLabel.setText(songArtist);

            //Set the thumb image
            if(mListTracks.get(songIndex).getThumb()!=null)
                Picasso.with(getActivity())
                        .load(mListTracks.get(songIndex).getThumb())
                        .into(songThumb);
        }
    }

    /**
     * //Start the MediaPlayer
     */
    private void startSelectedTrack(){
        Intent intent=new Intent(getActivity(),MediaPlayerService.class);
        intent.setAction(MediaPlayerService.MEDIASERVICE_START_START_SELECTED_TRACK);
        //Send an intent we're about to play the selected track
        getActivity().startService(intent);
    }



    /**
     *  Perform an action depending on which button the user has clicked
     * @param v the button
     */
    private void performAction(View v){
        mIntent=new Intent(getActivity(),MediaPlayerService.class);
        switch (v.getId()){
            case R.id.btnPlay:
                mIntent.setAction(MediaPlayerService.MEDIASERVICE_PLAYPAUSE);
                break;
            case R.id.btnBackward:
                mIntent.setAction(MediaPlayerService.MEDIASERVICE_BACKWARD);
                break;
            case R.id.btnForward:
                mIntent.setAction(MediaPlayerService.MEDIASERVICE_FORWARD);
                break;
            case R.id.btnNext:
                mIntent.setAction(MediaPlayerService.MEDIASERVICE_NEXT);
                break;
            case R.id.btnPrevious:
                mIntent.setAction(MediaPlayerService.MEDIASERVICE_PREVIOUS);
                break;
            case R.id.btnRepeat:
                mIntent.setAction(MediaPlayerService.MEDIASERVICE_REPEAT);
                break;
            case R.id.btnSpotify:
                //open Spotify
                openSpotify();
                break;
            case R.id.btnShuffle:
                mIntent.setAction(MediaPlayerService.MEDIASERVICE_SHUFFLE);
                break;
        }
        //Send intent via then startService Method
        getActivity().startService(mIntent);
    }



    private void openSpotify(){
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                mModel.getCurrentTrack().getExternalUrl());
        startActivity(shareIntent);
    }



    /**
     * Update timer on seekbar
     * */
    public void updateProgressBar() {
        if(isProgressbarStoped){
            mHandler.postDelayed(mUpdateTimeTask, 100);
        }
    }

    /**
     * Background Runnable thread
     * */
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            if(mp!=null){
                long totalDuration = mp.getDuration();
                long currentDuration = mp.getCurrentPosition();

                // Displaying Total Duration time
                songTotalDurationLabel.setText(""+utils.milliSecondsToTimer(totalDuration));
                // Displaying time completed playing
                songCurrentDurationLabel.setText(""+utils.milliSecondsToTimer(currentDuration));

                // Updating progress bar
                int progress = (int)(utils.getProgressPercentage(currentDuration, totalDuration));

                songProgressBar.setProgress(progress);

                // Running this thread after 100 milliseconds
                mHandler.postDelayed(this, 100);
                isProgressbarStoped=false;
            }
        }
    };


    @Override
    public void onStart() {
        super.onStart();

        updateUIReceiver = new MediaPlayerUpdateUIReceiver();
        progressBarStartReceiver = new MediaPlayerProgressBarStartReceiver();
        progressBarStopReceiver = new MediaPlayerProgressBarStopReceiver();
        errorReceiver=new MediaPlayerErrorReceiver();
        playPauseReceiver=new MediaPlayerPlayPauseReceiver();
        repeatReceiver=new MediaPlayerRepeatReceiver();
        shuffleReceiver=new MediaPlayerShuffleReceiver();

        //Register the broadcast receiver
        registerErrorBroadcast();
        registerPlayPauseBroadcast();
        registerProgressBarStartBroadcast();
        registerProgressBarStopBroadcast();
        registerUpdateUIBroadcast();
        registerRepeatBroadcast();
        registerShuffleBroadcast();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {




        // Inflate the layout for this fragment
        View rootView= inflater.inflate(R.layout.fragment_now_playing, null);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        // All player buttons
        btnPlayPause = (ImageButton) rootView.findViewById(R.id.btnPlay);
        btnForward = (ImageButton) rootView.findViewById(R.id.btnForward);
        btnBackward = (ImageButton) rootView.findViewById(R.id.btnBackward);
        btnNext = (ImageButton) rootView.findViewById(R.id.btnNext);
        btnPrevious = (ImageButton)rootView.findViewById(R.id.btnPrevious);
        btnRepeat = (ImageButton) rootView.findViewById(R.id.btnRepeat);
        btnSpotify = (ImageButton) rootView.findViewById(R.id.btnSpotify);
        btnShuffle = (ImageButton) rootView.findViewById(R.id.btnShuffle);
        songProgressBar = (SeekBar) rootView.findViewById(R.id.songProgressBar);
        songTitleLabel = (TextView) rootView.findViewById(R.id.songTitle);
        songAlbumLabel = (TextView) rootView.findViewById(R.id.songAlbum);
        songArtistLabel = (TextView) rootView.findViewById(R.id.songArtist);
        songArtistLabel.setSelected(true);
        songAlbumLabel.setSelected(true);
        songTitleLabel.setSelected(true);
        songCurrentDurationLabel = (TextView) rootView.findViewById(R.id.songCurrentDurationLabel);
        songTotalDurationLabel = (TextView) rootView.findViewById(R.id.songTotalDurationLabel);
        songThumb=(ImageView)rootView.findViewById(R.id.trackThumbnail);

        //OnclickListener
        btnPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performAction(v);
            }
        });

        btnForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performAction(v);
            }
        });

        btnBackward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performAction(v);
            }
        });

        btnPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performAction(v);
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performAction(v);
            }
        });

        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performAction(v);
            }
        });

        btnRepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performAction(v);
            }
        });

        btnSpotify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performAction(v);
            }
        });

        btnShuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performAction(v);
            }
        });

        //Init Media player values
        initMediaPlayer();

        if(savedInstanceState==null){ // First time we open the view
            //Receive data from last fragment

            Bundle args=getArguments();

            if(args!=null){
                // I retreive the informations
                mListTracks=(ArrayList<TrackModel>)args.getSerializable(LIST_TRACKS);

                mModel.setTrackList(mListTracks);

                //get the selected track index and save it in the model
                mModel.setCurrentSongIndex(args.getInt(TRACK_INDEX));

                // current track
                mCurrentTrack = mListTracks.get(mModel.getCurrentTrackIndex());

                mModel.setCurrentTrack(mCurrentTrack);
            }

            isFirstTime=true;
        }

        return rootView;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // TODO Auto-generated method stub

        Dialog dialog= super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        return dialog;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // The disalog should not be destroy
        setRetainInstance(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        L.m(LOG_TAG, "Now Playing onResume ");
        if(!isFirstTime){
            updateProgressBar();
            updateUI(mModel.getCurrentTrackIndex());
        }
        else {
            //Start playing the selected track
            isFirstTime=false;
            if(mModel.isNowPlayingTriggeredByUser()){
                startSelectedTrack();
            }
            else{
                //We update the progress bar
                updateProgressBar();
                updateUI(mModel.getCurrentTrackIndex());
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        L.m(LOG_TAG, "---------------Now Playing onStop-------------");
        mHandler.removeCallbacks(mUpdateTimeTask);
        isProgressbarStoped=true;

        //Unregister the receivers
        getActivity().unregisterReceiver(updateUIReceiver);
        getActivity().unregisterReceiver(errorReceiver);
        getActivity().unregisterReceiver(playPauseReceiver);
        getActivity().unregisterReceiver(repeatReceiver);
        getActivity().unregisterReceiver(shuffleReceiver);
        getActivity().unregisterReceiver(progressBarStartReceiver);
        getActivity().unregisterReceiver(progressBarStopReceiver);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {

    }

    /**
     * When user starts moving the progress handler
     * */
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask);
        isProgressbarStoped=true;
    }

    /**
     * When user stops moving the progress hanlder
     * */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if(mp!=null){

            mHandler.removeCallbacks(mUpdateTimeTask);
            isProgressbarStoped=true;
            int totalDuration = mp.getDuration();
            int currentPosition = utils.progressToTimer(seekBar.getProgress(), totalDuration);

            // forward or backward to certain seconds
            mp.seekTo(currentPosition);

            //update the progress bar
            updateProgressBar();
        }
    }



    // Receivers

    private class MediaPlayerErrorReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            String message = context.getString(R.string.msg_unknown_error);
            if(!Utilities.isOnline(getActivity())){
                message = context.getString(R.string.msg_playback_connection_error);
                L.m(LOG_TAG, "Not connected to internet");
                L.toast(context, message);
            }
            else{
                int error = intent.getIntExtra(MediaPlayerService.EXTRA_ERROR, -1);
                if (error == MediaPlayerService.MEDIAPLAYER_SERVICE_ERROR.MediaPlayer.ordinal()) {
                    message = context.getString(R.string.msg_playback_error);
                } else if (error == MediaPlayerService.MEDIAPLAYER_SERVICE_ERROR.InvalidTrack.ordinal()) {
                    message = context.getString(R.string.msg_playback_invalid_track_error);
                }
                L.m(LOG_TAG, message);
            }

            if(!isProgressbarStoped){
//                L.m(LOG_TAG, "--------------------- ProgressBarStopValue=  "+isProgressbarStoped);
                mHandler.removeCallbacks(mUpdateTimeTask);
                isProgressbarStoped=true;
            }
            updateUI(mModel.getCurrentTrackIndex());
        }
    }


    private class MediaPlayerUpdateUIReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent!=null){
                updateUI(mModel.getCurrentTrackIndex());
            }
        }
    }



    private class MediaPlayerPlayPauseReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent!=null){
                resetPlayPauseButton();
            }
        }
    }


    private class MediaPlayerRepeatReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent!=null){
                resetRepeatButton();
            }
        }
    }

    private class MediaPlayerShuffleReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent!=null){
                resetShuffleButton();
            }
        }
    }

    private class MediaPlayerProgressBarStartReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent!=null){
                // set Progress bar values
                updateProgressBar();
            }
        }
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance()) {
            getDialog().setDismissMessage(null);
        }
        super.onDestroyView();
    }

    private class MediaPlayerProgressBarStopReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent!=null){
                if(mHandler!=null){
                    if(!isProgressbarStoped){
                        mHandler.removeCallbacks(mUpdateTimeTask);
                        isProgressbarStoped=true;
                    }
                }
            }
        }
    }

}
