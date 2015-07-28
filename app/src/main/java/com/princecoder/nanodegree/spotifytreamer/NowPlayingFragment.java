package com.princecoder.nanodegree.spotifytreamer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.princecoder.nanodegree.spotifytreamer.model.MediaModel;
import com.princecoder.nanodegree.spotifytreamer.model.TrackModel;
import com.princecoder.nanodegree.spotifytreamer.utils.L;
import com.princecoder.nanodegree.spotifytreamer.utils.Utilities;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Random;


public class NowPlayingFragment extends DialogFragment implements MediaPlayer.OnCompletionListener, SeekBar.OnSeekBarChangeListener {

    private ImageButton btnPlay;
    private ImageButton btnForward;
    private ImageButton btnBackward;
    private ImageButton btnNext;
    private ImageButton btnPrevious;
    private ImageButton btnRepeat;
    private ImageButton btnShuffle;

    private ImageView songThumb;
    private SeekBar songProgressBar;
    private TextView songAlbumLabel;
    private TextView songArtistLabel;
    private TextView songTitleLabel;
    private TextView songCurrentDurationLabel;
    private TextView songTotalDurationLabel;

    //Media Model
    MediaModel mModel;
    // Media Player
    private MediaPlayer mp;
    // Handler to update UI timer, progress bar etc,.
    private Handler mHandler = new Handler();;
    //        private SongsManager songManager;
    private Utilities utils;
    private int seekForwardTime = 5000; // 5000 milliseconds
    private int seekBackwardTime = 5000; // 5000 milliseconds
    private boolean isShuffle = false;
    private boolean isRepeat = false;


    //My Tracks
    private TrackModel mTrack=new TrackModel();

    //List of tracks
    private ArrayList<TrackModel>mListTracks=new ArrayList<>();





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
        btnPlay = (ImageButton) rootView.findViewById(R.id.btnPlay);
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

        mModel=MediaModel.getInstance();

        mp = mModel.getMediaPlayer();
        //        songManager = new SongsManager();
        utils = new Utilities();

        // Listeners
        songProgressBar.setOnSeekBarChangeListener(this); // Important
        mp.setOnCompletionListener(this); // Important

        if(savedInstanceState==null){
            //Receive data from last fragment
            Bundle args=getArguments();

            if(args!=null){
                // I retreive the informations
                mListTracks=(ArrayList<TrackModel>)args.getSerializable(getResources().getString(R.string.Liste_of_tracks));
                mModel.setCurrentSongIndex(args.getInt("position"));
                mTrack = mListTracks.get(mModel.getCurrentSongIndex());
            }
            playSong(mModel.getCurrentSongIndex());
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
        btnPlay.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // check for already playing
                if (mp.isPlaying()) {
                    if (mp != null) {
                        mp.pause();
                        // Changing button image to play button
                        btnPlay.setImageResource(R.drawable.btn_play);
                    }
                } else {
                    // Resume song
                    if (mp != null) {
                        mp.start();
                        // Changing button image to pause button
                        btnPlay.setImageResource(R.drawable.btn_pause);
                    }
                }

            }
        });


        /**
         * Forward button click event
         * Forwards song specified seconds
         * */
        btnForward.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // get current song position
                int currentPosition = mp.getCurrentPosition();
                // check if seekForward time is lesser than song duration
                if (currentPosition + seekForwardTime <= mp.getDuration()) {
                    // forward song
                    mp.seekTo(currentPosition + seekForwardTime);
                } else {
                    // forward to end position
                    mp.seekTo(mp.getDuration());
                }
            }
        });



        /**
         * Backward button click event
         * Backward song to specified seconds
         * */
        btnBackward.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // get current song position
                int currentPosition = mp.getCurrentPosition();
                // check if seekBackward time is greater than 0 sec
                if(currentPosition - seekBackwardTime >= 0){
                    // forward song
                    mp.seekTo(currentPosition - seekBackwardTime);
                }else{
                    // backward to starting position
                    mp.seekTo(0);
                }

            }
        });


        /**
         * Next button click event
         * Plays next song by taking currentSongIndex + 1
         * */
        btnNext.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // check if next song is there or not
                if(mModel.getCurrentSongIndex() < (mListTracks.size() - 1)){
                    int currentSongIndex = mModel.getCurrentSongIndex() + 1;
                    mModel.setCurrentSongIndex(currentSongIndex);
                    playSong(currentSongIndex);
                }else{
                    // play first song
                    playSong(0);
                    mModel.setCurrentSongIndex(0);
                }

            }
        });

        /**
         * Back button click event
         * Plays previous song by currentSongIndex - 1
         * */
        btnPrevious.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if(mModel.getCurrentSongIndex() > 0){
                    int currentSongIndex = mModel.getCurrentSongIndex() - 1;
                    mModel.setCurrentSongIndex(currentSongIndex);
                    playSong(mModel.getCurrentSongIndex());

                }else{
                    mModel.setCurrentSongIndex(mListTracks.size() - 1);

                    // play last song
                    playSong(mModel.getCurrentSongIndex());
                }

            }
        });


        /**
         * Button Click event for Repeat button
         * Enables repeat flag to true
         * */
        btnRepeat.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if(isRepeat){
                    isRepeat = false;
                    L.toast(getActivity(), getResources().getString(R.string.repeat_off));
                    btnRepeat.setImageResource(R.drawable.btn_repeat);
                }else{
                    // make repeat to true
                    isRepeat = true;
                    L.toast(getActivity(), getResources().getString(R.string.repeat_on));
                    // make shuffle to false
                    isShuffle = false;
                    btnRepeat.setImageResource(R.drawable.btn_repeat_focused);
                    btnShuffle.setImageResource(R.drawable.btn_shuffle);
                }
            }
        });



        /**
         * Button Click event for Shuffle button
         * Enables shuffle flag to true
         * */
        btnShuffle.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if(isShuffle){
                    isShuffle = false;
                    L.toast(getActivity(), getResources().getString(R.string.shuffle_off));
                    btnShuffle.setImageResource(R.drawable.btn_shuffle);
                }else{
                    // make repeat to true
                    isShuffle= true;
                    L.toast(getActivity(), getResources().getString(R.string.shuffle_on));
                    // make shuffle to false
                    isRepeat = false;
                    btnShuffle.setImageResource(R.drawable.btn_shuffle_focused);
                    btnRepeat.setImageResource(R.drawable.btn_repeat);
                }
            }
        });


        return rootView;

    }



    /**
     * Function to play a song
     * @param songIndex - index of song
     * */
    public void  playSong(int songIndex){
        // Play song
        try {
            mp.reset();
            mp.setDataSource(mListTracks.get(songIndex).getPrevUrl());
            mp.prepare();
            mp.start();

            // Update UI elements
            updateUI(songIndex);
            // set Progress bar values
            songProgressBar.setProgress(0);
            songProgressBar.setMax(100);

            // Updating progress bar
            updateProgressBar();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //Reset the playPause button
    private void resetPlayPauseButton(){
        if(mp.isPlaying())
            btnPlay.setImageResource(R.drawable.btn_pause);
        else
            btnPlay.setImageResource(R.drawable.btn_play);
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
                e.printStackTrace();
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

    /**
     * On Song Playing completed
     * if repeat is ON play same song again
     * if shuffle is ON play random song
     * */
    @Override
    public void onCompletion(MediaPlayer arg0) {
        // check for repeat is ON or OFF
        if(isRepeat){
            // repeat is on play same song again
            playSong(mModel.getCurrentSongIndex());
        } else if(isShuffle){
            // shuffle is on - play a random song
            Random rand = new Random();
            mModel.setCurrentSongIndex(rand.nextInt(mListTracks.size()));
            playSong(mModel.getCurrentSongIndex());
        } else{
            // no repeat or shuffle ON - play next song
            if(mModel.getCurrentSongIndex() < (mListTracks.size() - 1)){
                int currentSongIndex = mModel.getCurrentSongIndex() + 1;
                mModel.setCurrentSongIndex(currentSongIndex);
                playSong(mModel.getCurrentSongIndex());

            }else{
                // play first song
                playSong(0);
                mModel.setCurrentSongIndex(0);
            }
        }
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
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bm;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            songThumb.setImageBitmap(result);
        }

    }

}
