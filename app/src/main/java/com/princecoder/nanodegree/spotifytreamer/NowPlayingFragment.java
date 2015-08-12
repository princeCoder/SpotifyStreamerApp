package com.princecoder.nanodegree.spotifytreamer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.princecoder.nanodegree.spotifytreamer.model.MediaModel;
import com.princecoder.nanodegree.spotifytreamer.model.TrackModel;
import com.princecoder.nanodegree.spotifytreamer.utils.L;
import com.princecoder.nanodegree.spotifytreamer.utils.Utilities;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
/**
 *  Dialog fragment use to show the Media player
 *
 *  If it is the first time I display the fragment, I will take data from the previous screen
 *  Otherwise, I retreive data from  the model
 */
public class NowPlayingFragment extends DialogFragment implements  SeekBar.OnSeekBarChangeListener{

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

    // Loading bar indicator
    ProgressBar loadingIndicator;

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

    private int mCurrentTrackIndex;

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

    private BroadcastReceiver playPauseReceiver;

    //Intent received
    private String receivedIntent;

    // Intent
    private Intent mIntent;

    // Play/Pause Tag use to send a message to the service that we pressed the Play/Pause button
    public static String PLAY_PAUSE="RESET_PLAY_PAUSE";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    private void updateUIBroadcast(){
        Intent intent = getActivity().registerReceiver(updateUIReceiver,
                new IntentFilter(MediaPlayerService.SERVICE_UPDATE_UI));
        if (intent != null) {
            updateUIReceiver.onReceive(getActivity(), mIntent);
        }
    }

    private void progressBarStartBroadcast(){
        Intent intent = getActivity().registerReceiver(progressBarStartReceiver,
                new IntentFilter(MediaPlayerService.SERVICE_UPDATE_PROGRESS_BAR_START));
        if (intent != null) {
            progressBarStartReceiver.onReceive(getActivity(), intent);
        }
    }

    private void progressBarStopBroadcast(){
        Intent intent = getActivity().registerReceiver(progressBarStopReceiver,
                new IntentFilter(MediaPlayerService.SERVICE_UPDATE_PROGRESS_BAR_STOP));
        if (intent != null) {
            progressBarStopReceiver.onReceive(getActivity(), intent);
        }
    }

    private void playPauseBroadcast(){
        Intent intent = getActivity().registerReceiver(playPauseReceiver,
                new IntentFilter(MediaPlayerService.SERVICE_UPDATE_PLAY_PAUSE));
        if (intent != null) {
            playPauseReceiver.onReceive(getActivity(), intent);
        }
    }


    private void errorBroadcast(){
        Intent intent =getActivity().registerReceiver(errorReceiver,
                new IntentFilter(MediaPlayerService.SERVICE_ERROR_NAME));
        if (intent != null) {
            errorReceiver.onReceive(getActivity(), intent);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        //Register the broadcast receiver
        updateUIReceiver = new MediaPlayerUpdateUIReceiver();
        progressBarStartReceiver = new MediaPlayerProgressBarStartReceiver();
        progressBarStopReceiver = new MediaPlayerProgressBarStopReceiver();
        errorReceiver=new MediaPlayerErrorReceiver();
        playPauseReceiver=new MediaPlayerPlayPauseReceiver();

        errorBroadcast();
        playPauseBroadcast();
        updateUIBroadcast();
        progressBarStartBroadcast();
        progressBarStopBroadcast();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView= inflater.inflate(R.layout.fragment_now_playing, null);

        // All player buttons
        btnPlayPause = (ImageButton) rootView.findViewById(R.id.btnPlay);
        btnForward = (ImageButton) rootView.findViewById(R.id.btnForward);
        btnBackward = (ImageButton) rootView.findViewById(R.id.btnBackward);
        btnNext = (ImageButton) rootView.findViewById(R.id.btnNext);
        btnPrevious = (ImageButton)rootView.findViewById(R.id.btnPrevious);
        btnRepeat = (ImageButton) rootView.findViewById(R.id.btnRepeat);
        btnShuffle = (ImageButton) rootView.findViewById(R.id.btnShuffle);
        songProgressBar = (SeekBar) rootView.findViewById(R.id.songProgressBar);
        songTitleLabel = (TextView) rootView.findViewById(R.id.songTitle);
        songAlbumLabel = (TextView) rootView.findViewById(R.id.songAlbum);
        songArtistLabel = (TextView) rootView.findViewById(R.id.songArtist);
        songCurrentDurationLabel = (TextView) rootView.findViewById(R.id.songCurrentDurationLabel);
        songTotalDurationLabel = (TextView) rootView.findViewById(R.id.songTotalDurationLabel);
        songThumb=(ImageView)rootView.findViewById(R.id.trackThumbnail);
        loadingIndicator = (ProgressBar) rootView.findViewById(R.id.player_loading_indicator);

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

        btnShuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performAction(v);
            }
        });

        //Init Media player values
        init();


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

            //Start playing the selected track
            startSelectedTrack();

            // Update progress bar
            //updateProgressBar();
        }
        else{ // We just came back into the view

            //Update UI elements
            updateUI(mCurrentTrackIndex);

            //Update the progress bar
            updateProgressBar();
        }

        return rootView;

    }

    //Initialize The Media player values
    private void init() {
        // The model
        mModel= MediaModel.getInstance();

        //Make sure I have the same Media player
        mp = mModel.getMediaPlayer();

        utils = new Utilities();

        //Get the current track
        mCurrentTrack =mModel.getCurrentTrack();

        //get the current track index
        mCurrentTrackIndex=mModel.getCurrentTrackIndex();

        //Get the list of tracks
        mListTracks=mModel.getTrackList();

        // Listeners
        songProgressBar.setOnSeekBarChangeListener(this);
    }


    //Reset the playPause button
    private void resetPlayPauseButton(){
        if(mp!=null){
            if(mp.isPlaying())
                btnPlayPause.setImageResource(R.drawable.btn_pause);
            else
                btnPlayPause.setImageResource(R.drawable.btn_play);
        }
    }


    //Update UI elements
    private void updateUI(int songIndex) {
        //Reset the playPause button
        resetPlayPauseButton();

        // Displaying Song title
        String songTitle = mListTracks.get(songIndex).getTrackName();
        String songAlbum=mListTracks.get(songIndex).getAlbum();
        String songArtist=mListTracks.get(songIndex).getArtist();

        songTitleLabel.setText(songTitle);
        songAlbumLabel.setText(songAlbum);
        songArtistLabel.setText(songArtist);

        //Set the thumb image
        if(mListTracks.get(songIndex).getThumb()!=null)
            new LoadThumbImage().execute(mListTracks.get(songIndex).getThumb());
    }


    @Override
    public void onDetach() {
        super.onDetach();
    }

    //Start the MediaPlayer
    private void startSelectedTrack(){
        Intent intent=new Intent(getActivity(),MediaPlayerService.class);
        intent.setAction(MediaPlayerService.MEDIASERVICE_START_START_SELECTED_TRACK);
        //Send an intent we're about to play the selected track
        getActivity().startService(intent);
    }

    //Send an action to the service
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
            case R.id.btnShuffle:
                mIntent.setAction(MediaPlayerService.MEDIASERVICE_SHUFFLE);
                break;
        }

        //Send intent via then startService Method
        getActivity().startService(mIntent);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
    }



    /**
     * Update timer on seekbar
     * */
    public void updateProgressBar() {
        Log.d(LOG_TAG, "updateProgressBar()");
        mHandler.postDelayed(mUpdateTimeTask, 100);

    }

    /**
     * Background Runnable thread
     * */
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            Log.d(LOG_TAG,"mUpdateTimeTask");
            if(mp!=null){

                long totalDuration = mp.getDuration();
                long currentDuration = mp.getCurrentPosition();

                // Displaying Total Duration time
                songTotalDurationLabel.setText(""+utils.milliSecondsToTimer(totalDuration));
                // Displaying time completed playing
                songCurrentDurationLabel.setText(""+utils.milliSecondsToTimer(currentDuration));

                // Updating progress bar
                int progress = (int)(utils.getProgressPercentage(currentDuration, totalDuration));
                //Log.d("Progress", ""+progress);
                songProgressBar.setProgress(progress);

                // Running this thread after 100 milliseconds
                mHandler.postDelayed(this, 100);
            }
        }
    };


    /**
     *
     * */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {

    }

    /**
     * When user starts moving the progress handler
     * */
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask);
    }

    /**
     * When user stops moving the progress hanlder
     * */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if(mp!=null){

            mHandler.removeCallbacks(mUpdateTimeTask);
            int totalDuration = mp.getDuration();
            int currentPosition = utils.progressToTimer(seekBar.getProgress(), totalDuration);

            // forward or backward to certain seconds
            mp.seekTo(currentPosition);

            //update the progress bar
            updateProgressBar();
        }
    }


    // Set the value of the Album Thumbnail
    // I have created this Asynctask to be able to set that value on the main thread
    private class LoadThumbImage extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... params) {
            URL myUrl;
            Bitmap bm=null;

            try {
                myUrl = new URL(params[0]);
                URLConnection con=myUrl.openConnection();
                bm=BitmapFactory.decodeStream(con.getInputStream());
            }  catch (IOException e) {
                // e.printStackTrace();
                L.m(LOG_TAG,e.getMessage());
            }
            return bm;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            songThumb.setImageBitmap(result);
        }

    }


    // Receivers

    private class MediaPlayerErrorReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG, "Playback error received - toasting message");
            String message = context.getString(R.string.msg_unknown_error);
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
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

    private class MediaPlayerProgressBarStartReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent!=null){
                // set Progress bar values
                updateProgressBar();
            }
        }
    }

    private class MediaPlayerProgressBarStopReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent!=null){
                if(mHandler!=null){
                    mHandler.removeCallbacks(mUpdateTimeTask);
                }
            }
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        //Unregister the receivers
        getActivity().unregisterReceiver(updateUIReceiver);
        getActivity().unregisterReceiver(errorReceiver);
        getActivity().unregisterReceiver(playPauseReceiver);
        getActivity().unregisterReceiver(progressBarStartReceiver);
        getActivity().unregisterReceiver(progressBarStopReceiver);
    }
}
