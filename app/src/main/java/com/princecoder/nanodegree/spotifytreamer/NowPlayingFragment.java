package com.princecoder.nanodegree.spotifytreamer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
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
import android.widget.Toast;

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

/**
 *  Dialog fragment use to show the Media player
 *
 *  If it is the first time I display the fragment, I will take data from the previous screen
 *  Otherwise, I retreive data from  the model
 */
public class NowPlayingFragment extends DialogFragment implements MediaPlayer.OnCompletionListener, SeekBar.OnSeekBarChangeListener{

    public final String TAG=getClass().getSimpleName();

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

    //Media Model
    MediaModel mModel;

    // Media Player
    private MediaPlayer mp;

    // Handler to update UI timer, progress bar etc,.
    private Handler mHandler = new Handler();

    //Utilities
    private Utilities utils;

    private int seekForwardTime = 5000; // 5000 milliseconds
    private int seekBackwardTime = 5000; // 5000 milliseconds

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

        // The model
        mModel=MediaModel.getInstance();

        //Make sure I have the same Media player
        mp = mModel.getMediaPlayer();

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

                //get the selected track index and save it in the model
                mModel.setCurrentSongIndex(args.getInt(getResources().getString(R.string.track_index)));

                // current track
                mTrack = mListTracks.get(mModel.getCurrentSongIndex());
            }

            // Play the current track
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
    };

    View.OnClickListener btnBackwardListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
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
    };

    View.OnClickListener btnNextListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
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
    };

    View.OnClickListener btnPreviousListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
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
    };

    View.OnClickListener btnRepeatListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(mModel.isShuffle()){
                mModel.setRepeat(false);
                L.toast(getActivity(), getResources().getString(R.string.repeat_off));
                btnRepeat.setImageResource(R.drawable.btn_repeat);
            }else{
                // make repeat to true
                mModel.setRepeat(true);

                L.toast(getActivity(), getResources().getString(R.string.repeat_on));

                // make shuffle to false
                mModel.setShuffle(false);

                btnRepeat.setImageResource(R.drawable.btn_repeat_focused);
                btnShuffle.setImageResource(R.drawable.btn_shuffle);
            }
        }
    };

    View.OnClickListener btnShuffleListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(mModel.isShuffle()){
                mModel.setShuffle(false);

                L.toast(getActivity(), getResources().getString(R.string.shuffle_off));
                btnShuffle.setImageResource(R.drawable.btn_shuffle);
            }else{
                // make repeat to true
                mModel.setRepeat(true);
                L.toast(getActivity(), getResources().getString(R.string.shuffle_on));

                // make shuffle to false
                mModel.setRepeat(false);

                btnShuffle.setImageResource(R.drawable.btn_shuffle_focused);
                btnRepeat.setImageResource(R.drawable.btn_repeat);
            }
        }
    };


    View.OnClickListener btnPlayPauseListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // check for already playing
            if (mp.isPlaying()) {
                if (mp != null) {
                    mp.pause();
                    // Changing button image to play button
                    btnPlayPause.setImageResource(R.drawable.btn_play);
                }
            } else {
                // Resume song
                if (mp != null) {
                    mp.start();
                    // Changing button image to pause button
                    btnPlayPause.setImageResource(R.drawable.btn_pause);
                }
            }
        }
    };


    /**
     * Function to play a song
     * @param songIndex - index of song
     *
     *                  This is temporary since we need to prepare the track in a background service
     *                  to prevent the media player to block the scresn
     * */
    public void  playSong(int songIndex){
        try {
            mp.reset();
            String s=mListTracks.get(songIndex).getPrevUrl();

            mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mp.setDataSource(s);

            mp.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    Toast.makeText(getActivity(), " There was an error reading the file", Toast.LENGTH_SHORT).show();
                    return false;
                }
            });

            mp.prepare();
            mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    // Stat the media player
                    mp.start();
                }
            });

            // Update UI elements
            updateUI(songIndex);

            // set Progress bar values
            songProgressBar.setProgress(0);
            songProgressBar.setMax(100);

            // Updating progress bar
            updateProgressBar();


        } catch (IllegalArgumentException e) {
            L.m(TAG," Play Song  IllegalArgumentException "+ e.getMessage());
        }
        catch (IllegalStateException e) {
            L.m(TAG," Play Song  IllegalStateException "+e.getMessage());
        }
        catch (IOException e) {
            L.m(TAG,e.getMessage());
        }
    }



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
                L.m(TAG,e.getMessage());
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
        if(mModel.isRepeat()){
            // repeat is on play same song again
            playSong(mModel.getCurrentSongIndex());
        } else if(mModel.isShuffle()){
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
               // e.printStackTrace();
                L.m(TAG,e.getMessage());
            } catch (IOException e) {
               // e.printStackTrace();
                L.m(TAG,e.getMessage());
            }
            return bm;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            songThumb.setImageBitmap(result);
        }

    }

}
