package com.princecoder.nanodegree.spotifytreamer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
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
    //My Tracks
    private TrackModel mTrack=new TrackModel();

    //List of tracks
    private ArrayList<TrackModel>mListTracks=new ArrayList<>();

    public static final String LIST_TRACKS="LIST_TRACKS";

    public static final String TRACK_INDEX="TRACK_INDEX";



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        // The model
        mModel=MediaModel.getInstance();

        //Make sure I have the same Media player
        mp = mModel.getMediaPlayer();

        utils = new Utilities();

        // Listeners
        songProgressBar.setOnSeekBarChangeListener(this); // Important
       // mp.setOnCompletionListener(this); // Important

        if(savedInstanceState==null){
            //Receive data from last fragment
            Bundle args=getArguments();

            if(args!=null){
                // I retreive the informations
                mListTracks=(ArrayList<TrackModel>)args.getSerializable(LIST_TRACKS);
                mModel.setTrackList(mListTracks);

                //get the selected track index and save it in the model
                mModel.setCurrentSongIndex(args.getInt(TRACK_INDEX));

                // current track
                mTrack = mListTracks.get(mModel.getCurrentSongIndex());
                mModel.setCurrentTrack(mTrack);
            }
            startPlayer();
        }
        else{
            //Update the thumbnail
            updateUI(mModel.getCurrentSongIndex());

            //Update the progress bar
            updateProgressBar();
        }

        /**
         * Play button click event
         * plays a song and changes button to pause image
         * pauses a song and changes button to play image
         * */
        btnPlayPause.setOnClickListener(btnPlayPauseListener);

        /**
         * Forward button click event
         * Forwards song specified seconds
         * */
        btnForward.setOnClickListener(btnForwardListener);

        /**
         * Backward button click event
         * Backward song to specified seconds
         * */
        btnBackward.setOnClickListener(btnBackwardListener);


        /**
         * Next button click event
         * Plays next song by taking currentSongIndex + 1
         * */
        btnNext.setOnClickListener(btnNextListener);

        /**
         * Back button click event
         * Plays previous song by currentSongIndex - 1
         * */
        btnPrevious.setOnClickListener(btnPreviousListener);

        /**
         * Button Click event for Repeat button
         * Enables repeat flag to true
         * */
        btnRepeat.setOnClickListener(btnRepeatListener);

        /**
         * Button Click event for Shuffle button
         * Enables shuffle flag to true
         * */
        btnShuffle.setOnClickListener(btnShuffleListener);

        return rootView;

    }

    //Listeners

    View.OnClickListener btnForwardListener= new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            performAction(v);
        }
    };
    View.OnClickListener btnBackwardListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            performAction(v);
        }
    };
    View.OnClickListener btnNextListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            performAction(v);
        }
    };
    View.OnClickListener btnPreviousListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            performAction(v);
        }
    };
    View.OnClickListener btnRepeatListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            performAction(v);
        }
    };
    View.OnClickListener btnShuffleListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            performAction(v);
        }
    };
    View.OnClickListener btnPlayPauseListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            performAction(v);
        }
    };


    //Reset the playPause button
    private void resetPlayPauseButton(){
        if(mp.isPlaying())
            btnPlayPause.setImageResource(R.drawable.btn_pause);
        else
            btnPlayPause.setImageResource(R.drawable.btn_play);
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
    private void startPlayer(){
        Intent intent=new Intent(getActivity(),MediaPlayerService.class);
        intent.setAction(MediaPlayerService.MEDIASERVICE_START_PLAYER);
        //I pass tracks to the intent
        intent.putExtra(MediaPlayerService.TRACKS_LIST, mListTracks);
        intent.putExtra(MediaPlayerService.CURRENT_TRACK, mTrack);
        getActivity().startService(intent);
    }

    //Sent an action to the service

    private void performAction(View v){
        Intent intent=new Intent(getActivity(),MediaPlayerService.class);
        switch (v.getId()){
            case R.id.btnPlay:
                intent.setAction(MediaPlayerService.MEDIASERVICE_PLAYPAUSE);
                break;
            case R.id.btnBackward:
                intent.setAction(MediaPlayerService.MEDIASERVICE_BACKWARD);
                break;
            case R.id.btnForward:
                intent.setAction(MediaPlayerService.MEDIASERVICE_FORWARD);
                break;
            case R.id.btnNext:
                intent.setAction(MediaPlayerService.MEDIASERVICE_NEXT);
                break;
            case R.id.btnPrevious:
                intent.setAction(MediaPlayerService.MEDIASERVICE_PREVIOUS);
                break;
            case R.id.btnRepeat:
                intent.setAction(MediaPlayerService.MEDIASERVICE_REPEAT);
                break;
            case R.id.btnShuffle:
                intent.setAction(MediaPlayerService.MEDIASERVICE_SHUFFLE);
                break;
        }
        getActivity().startService(intent);
    }



    /**
     * Update timer on seekbar
     * */
    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }

    /**
     * Background Runnable thread
     * */
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            try {
                long totalDuration = mp.getDuration();
                long currentDuration = mp.getCurrentPosition();

                // Displaying Total Duration time
                songTotalDurationLabel.setText(""+utils.milliSecondsToTimer(totalDuration));
                // Displaying time completed playing
                songCurrentDurationLabel.setText(""+utils.milliSecondsToTimer(currentDuration));

                // Updating progress bar
                int progress = utils.getProgressPercentage(currentDuration, totalDuration);
                //Log.d("Progress", ""+progress);
                songProgressBar.setProgress(progress);

                // Running this thread after 100 milliseconds
                mHandler.postDelayed(this, 100);
            } catch (IllegalStateException e) {
//                e.printStackTrace();
                L.m(LOG_TAG,e.getMessage());
            }
        }
    };


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
    }



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
        // remove message Handler from updating progress bar
        mHandler.removeCallbacks(mUpdateTimeTask);
    }

    /**
     * When user stops moving the progress hanlder
     * */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask);
        int totalDuration = mp.getDuration();
        int currentPosition = utils.progressToTimer(seekBar.getProgress(), totalDuration);

        // forward or backward to certain seconds
        mp.seekTo(currentPosition);

        // update timer progress again
        updateProgressBar();
    }


    // Set the value of the Album Thumbnail
    // I have created this Asynctask to be able to set that value on the main thread
    private class LoadThumbImage extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... params) {
            URL myurl;
            Bitmap bm=null;

            try {
                myurl = new URL(params[0]);
                URLConnection con=myurl.openConnection();
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

    private class PlaybackErrorReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG, "Playback error received - toasting message");
            String message = context.getString(R.string.msg_unknown_error);

            int error = intent.getIntExtra(MediaPlayerService.EXTRA_ERROR, -1);
            if (error == MediaPlayerService.PLAYBACK_SERVICE_ERROR.Playback.ordinal()) {
                message = context.getString(R.string.msg_playback_error);
            } else if (error == MediaPlayerService.PLAYBACK_SERVICE_ERROR.Connection.ordinal()) {
                message = context.getString(R.string.msg_playback_connection_error);
            } else if (error == MediaPlayerService.PLAYBACK_SERVICE_ERROR.InvalidPlayable.ordinal()) {
                message = context.getString(R.string.msg_playback_invalid_playable_error);

                //TODO Do something
            }
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        }
    }

}
