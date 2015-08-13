package com.princecoder.nanodegree.spotifytreamer;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.princecoder.nanodegree.spotifytreamer.model.MediaModel;
import com.princecoder.nanodegree.spotifytreamer.model.TrackModel;
import com.princecoder.nanodegree.spotifytreamer.utils.L;

import java.io.IOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Random;


/**
 * Created by Prinzly Ngotoum on 8/1/15.
 *
 * This class helps me to handle the Media player in a service
 */
public class MediaPlayerService extends Service implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnInfoListener{

    //Log Tag
    public final String LOG_TAG =getClass().getSimpleName();

    // My Media Player
    private MediaPlayer mp;

    private boolean isPrepared = false;

    // Media Model
    private MediaModel mModel;

    //My Current Tracks
    private TrackModel mCurrentTrack =new TrackModel();

    private int mCurrentTrackIndex;

    //List of tracks
    private ArrayList<TrackModel> mListTracks=new ArrayList<>();


    private Handler mHandler = new Handler();

    // Track whether we ever called start() on the media player so we don't try
    // to reset or release it.
    private boolean mediaPlayerHasStarted = false;

    private TelephonyManager telephonyManager;
    private PhoneStateListener listener;
    private boolean isPausedInCall = false;


    // Amount of time to rewind playback when resuming after call
    private final static int RESUME_REWIND_TIME = 3000;

    private int seekForwardTime = 5000; // 5000 milliseconds
    private int seekBackwardTime = 5000; // 5000 milliseconds

    private Looper serviceLooper;
    private ServiceHandler serviceHandler;

    public static final String CURRENT_POSITION="CURRENT_POSITION";


    public static final String MEDIASERVICE_PLAYPAUSE="MEDIASERVICE_PLAYPAUSE";
    public static final String MEDIASERVICE_SEEK_TO="MEDIASERVICE_SEEK_TO";
    public static final String MEDIASERVICE_START_START_SELECTED_TRACK="MEDIASERVICE_START_START_SELECTED_TRACK";
    public static final String MEDIASERVICE_NEXT="MEDIASERVICE_NEXT";
    public static final String MEDIASERVICE_PREVIOUS="MEDIASERVICE_PREVIOUS";
    public static final String MEDIASERVICE_BACKWARD="MEDIASERVICE_BACKWARD";
    public static final String MEDIASERVICE_FORWARD="MEDIASERVICE_FORWARD";
    public static final String MEDIASERVICE_REPEAT="MEDIASERVICE_REPEAT";
    public static final String MEDIASERVICE_SHUFFLE="MEDIASERVICE_SHUFFLE";
    public static final String SERVICE_ERROR_NAME = "SERVICE_ERROR_NAME";
    public static final String SERVICE_CLOSE_NAME = "CLOSE";
    public static final String SERVICE_UPDATE_PROGRESS_BAR_START = "SERVICE_UPDATE_PROGRESS_BAR_START";
    public static final String SERVICE_UPDATE_PROGRESS_BAR_STOP = "SERVICE_UPDATE_PROGRESS_BAR_STOP";
    public static final String SERVICE_UPDATE_PLAY_PAUSE="SERVICE_UPDATE_PLAY_PAUSE";
    public static final String SERVICE_UPDATE_UI="SERVICE_UPDATE_UI";
    public static final String EXTRA_ERROR = "EXTRA_ERROR";

    public enum MEDIAPLAYER_SERVICE_ERROR {MediaPlayer, Connection, InvalidTrack}

    private String currentAction;



    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            onHandleIntent((Intent) msg.obj);
        }
    }


    //Init the media Player
    public void initMediaPlayer(){

        // Set player properties
        mp.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);

        mp.setOnPreparedListener(this);
        mp.setOnCompletionListener(this);
        mp.setOnErrorListener(this);
        mp.setOnInfoListener(this);

    }



    @Override
    public void onCreate() {

        //Get the instance of the model
        mModel=MediaModel.getInstance();

        // Get the player
        mp = mModel.getMediaPlayer();

        //Initialize the Media Player
        initMediaPlayer();

        Log.d(LOG_TAG, "MediaPlayer service created");

        telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        // Create a PhoneStateListener to watch for off-hook and idle events
        listener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                switch (state) {
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                    case TelephonyManager.CALL_STATE_RINGING:
                        // Phone going off-hook or ringing, pause the player.
                        if (mModel.isPlaying()) {

                            pause();
                            isPausedInCall = true;
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        // Phone idle. Rewind a couple of seconds and start playing.
                        if (isPausedInCall) {
                            isPausedInCall = false;
                            seekTo(Math.max(0, getPosition() - RESUME_REWIND_TIME));
                            play();
                        }
                        break;
                }
            }
        };

        // Register the listener with the telephony manager.
        telephonyManager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);

        HandlerThread thread = new HandlerThread("MediaPlayerService:WorkerThread");
        thread.start();

        serviceLooper = thread.getLooper();
        serviceHandler = new ServiceHandler(serviceLooper);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Message message = serviceHandler.obtainMessage();
        message.arg1 = startId;
        message.obj = intent;
        serviceHandler.sendMessage(message);
        mCurrentTrack=mModel.getCurrentTrack();
        mListTracks=mModel.getTrackList();
        mCurrentTrackIndex=mModel.getCurrentTrackIndex();

        return START_STICKY;
    }



    protected void onHandleIntent(Intent intent) {
        if (intent == null || intent.getAction() == null) {
            Log.d(LOG_TAG, "Null intent received");
            return;
        }

        currentAction=intent.getAction();
        Log.d(LOG_TAG, "Media Player service action received: " + currentAction);
        switch (currentAction){
            case MEDIASERVICE_PLAYPAUSE:
                playPause();
                break;
            case MEDIASERVICE_BACKWARD:
                backward();
                break;
            case MEDIASERVICE_FORWARD:
                forward();
                break;
            case MEDIASERVICE_NEXT:
                next();
                break;
            case MEDIASERVICE_PREVIOUS:
                previous();
                break;
            case MEDIASERVICE_REPEAT:
                repeat();
                break;
            case MEDIASERVICE_SHUFFLE:
                shuffle();
                break;
            case MEDIASERVICE_START_START_SELECTED_TRACK:
                playCurrent();
                break;
            case MEDIASERVICE_SEEK_TO:
                seekTo(intent.getIntExtra(CURRENT_POSITION,0));
                break;
            default:
                break;
        }
    }


    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        return false;
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.w(LOG_TAG, "Service exiting");
        stop();
        synchronized (this) {
            if (mp != null) {
                if (mediaPlayerHasStarted) {
                    mp.release();
                } else {
                    mp.setOnCompletionListener(null);
                    mp.setOnErrorListener(null);
                    mp.setOnInfoListener(null);
                    mp.setOnPreparedListener(null);
                    mp.setOnSeekCompleteListener(null);
                }
                mp = null;
            }
        }

        serviceLooper.quit();
        stopForeground( true );

        getApplicationContext().sendBroadcast(new Intent(SERVICE_CLOSE_NAME));

        telephonyManager.listen(listener, PhoneStateListener.LISTEN_NONE);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.w(LOG_TAG, "onComplete()");
            // check for repeat is ON or OFF
            if(mModel.isRepeat()){
                // repeat is on play same song again
                playCurrent();
            } else if(mModel.isShuffle()){
                // shuffle is on - play a random song
                Random rand = new Random();
                int currentSongIndex = rand.nextInt(mListTracks.size());
                mCurrentTrackIndex=currentSongIndex;
                mModel.setCurrentSongIndex(mCurrentTrackIndex);
                playCurrent();
            } else{
                // no repeat or shuffle ON - play next song
                if(mCurrentTrackIndex < mListTracks.size() - 1){
                    int currentSongIndex = mCurrentTrackIndex + 1;
                    mCurrentTrackIndex=currentSongIndex;
                    mModel.setCurrentSongIndex(mCurrentTrackIndex);

                    playCurrent();


                }else{
                    // play first song
                    mCurrentTrackIndex=0;
                    mModel.setCurrentSongIndex(mCurrentTrackIndex);
                    playCurrent();
                }
            }
    }


    //Pause the Media player
    synchronized private void pause() {
        Log.d(LOG_TAG, "pause");
        if (isPrepared) {
            if (mCurrentTrack != null) {
                isPrepared = false;
                mp.stop();
            } else {
                mp.pause();
            }
        }
        stopForeground(true);
    }

    //Stop the Media player
    synchronized private void stop() {
        Log.d(LOG_TAG, "stop");
        if (isPrepared) {
            isPrepared = false;
            mp.stop();
            // Send a broadcast to the Now Playing to stop the handler
            StopProgressBar();
        }
    }



    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.d(LOG_TAG, "Error:  " + what + " " + extra);
        handleMediaPlayerError(MEDIAPLAYER_SERVICE_ERROR.MediaPlayer);
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.d(LOG_TAG, "Prepared  --- onPrepared");
        synchronized (this) {
            if (mp != null) {
                isPrepared = true;
            }
        }
        // Start playing the track
        startPlaying();
    }


    // Resume the mediaplayer
    private void resumePlaying() {
        if (mCurrentTrack != null) {
            if (isPrepared) {
                play();
            } else {
                playCurrent();
            }
        }
    }

    // Play current track index
    private boolean playCurrent() {
            try {
                if(mModel!=null){
                    mCurrentTrack=mListTracks.get(mCurrentTrackIndex);
                    if (mCurrentTrack == null || mCurrentTrack.getPrevUrl() == null) {
                        Intent intent = new Intent(SERVICE_ERROR_NAME);
                        intent.putExtra(EXTRA_ERROR, MEDIAPLAYER_SERVICE_ERROR.InvalidTrack.ordinal());
                        getApplicationContext().sendBroadcast(intent);

                        return false;
                    }
                    //Update the model with the current song
                    mModel.setCurrentTrack(mCurrentTrack);
                    //Prepare then play the current song
                    prepareThenPlay(mCurrentTrack.getPrevUrl());
                    return true;
                }
            } catch (UnknownHostException e) {
                Log.w(LOG_TAG, "Unknown host in playCurrent");
                handleMediaPlayerError(MEDIAPLAYER_SERVICE_ERROR.MediaPlayer);
            } catch (ConnectException e) {
                Log.w(LOG_TAG, "Connect exception in playCurrent");
                handleMediaPlayerError(MEDIAPLAYER_SERVICE_ERROR.Connection);
            } catch (IOException e) {
                Log.e(LOG_TAG, "IOException on playlist entry " + mCurrentTrack.getTrackName(), e);
                handleMediaPlayerError(MEDIAPLAYER_SERVICE_ERROR.MediaPlayer);
            } catch (IllegalStateException e) {
                Log.e(LOG_TAG, "Illegal state exception trying to play entry " + mCurrentTrack.getTrackName(), e);
                handleMediaPlayerError(MEDIAPLAYER_SERVICE_ERROR.MediaPlayer);
            }
        return false;
    }

    /**
     * Handle errors
     */
    private void handleMediaPlayerError(MEDIAPLAYER_SERVICE_ERROR error) {
        Intent intent = new Intent(SERVICE_ERROR_NAME);
        switch (error){
            case MediaPlayer:
                intent.putExtra(EXTRA_ERROR, MEDIAPLAYER_SERVICE_ERROR.MediaPlayer.ordinal());
                break;
            case InvalidTrack:
                intent.putExtra(EXTRA_ERROR, MEDIAPLAYER_SERVICE_ERROR.InvalidTrack.ordinal());
                break;
            case Connection:
                intent.putExtra(EXTRA_ERROR, MEDIAPLAYER_SERVICE_ERROR.Connection.ordinal());
                break;
        }

        getApplicationContext().sendBroadcast(intent);
    }


    //Prepare then play a song using the url
    private void prepareThenPlay(String url)
            throws IllegalArgumentException, IllegalStateException, IOException {
        Log.d(LOG_TAG, "prepareThenPlay " + mCurrentTrack.getTrackName());
        // First, clean up any existing audio.
        stop();
        mediaPlayerHasStarted = false;
        Log.d(LOG_TAG, "listening to " + url);
        synchronized (this) {
            Log.d(LOG_TAG, "reset: " + url);
            mp.reset();
            mp.setDataSource(url);
            mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
            Log.d(LOG_TAG, "Preparing: " + url);
            mp.prepareAsync();
            Log.d(LOG_TAG, "Waiting for prepare");
        }
    }


    //Start playing the current track
    private void startPlaying() {
        Log.d(LOG_TAG, "StartPlaying: ");
        play();
        //Update UI Elements
        updateUI();
        StartProgressBar();
    }


    //Play the current track
    synchronized private void play() {
        if (!isPrepared || mCurrentTrack == null) {
            Log.e(LOG_TAG, "play - not prepared");
            return;
        }
        Log.d(LOG_TAG, "play " + mCurrentTrack.getTrackName());

        mp.start();
        mediaPlayerHasStarted = true;
    }



    // Play previous track
    synchronized private void previous(){
        if(mCurrentTrackIndex > 0){
            int currentSongIndex = mCurrentTrackIndex - 1;
            mCurrentTrackIndex=currentSongIndex;
            mModel.setCurrentSongIndex(currentSongIndex);
        }else{
            mModel.setCurrentSongIndex(mListTracks.size() - 1);
        }
        playCurrent();

        //Todo update NowPlaying Screen
    }

    //Play next song
    synchronized private void next(){
        // check if next song is there or not
        if(mCurrentTrackIndex < (mListTracks.size() - 1)){
            int currentSongIndex = mCurrentTrackIndex + 1;
            mCurrentTrackIndex=currentSongIndex;
            mModel.setCurrentSongIndex(mCurrentTrackIndex);

        }else{
            // play first song
            mModel.setCurrentSongIndex(0);
            mCurrentTrackIndex=0;
            mModel.setCurrentSongIndex(mCurrentTrackIndex);
        }

        //Play the current song
        playCurrent();
    }

    //Send  a broacast to the Now playing screen to update the play/Pause button
    private void updateUI(){
        Log.d(LOG_TAG, "updateUI");
        Intent uiIntent = new Intent(SERVICE_UPDATE_UI);
        getApplicationContext().sendBroadcast(uiIntent);
    }

    //Send  a broacast to the Now playing screen to update the play/Pause button
    private void updatePlayPauseButton(){
        Log.d(LOG_TAG, "updatePlayPauseButton");
        Intent playPauseIntent = new Intent(SERVICE_UPDATE_PLAY_PAUSE);
        getApplicationContext().sendBroadcast(playPauseIntent);
    }

    //Send a broadcast to the NowPlaying screen to start the seekbar
    private void StartProgressBar(){
        Log.d(LOG_TAG, "start progressBar");
        Intent progressBarIntent = new Intent(SERVICE_UPDATE_PROGRESS_BAR_START);
        getApplicationContext().sendBroadcast(progressBarIntent);
    }


    //Send a broadcast to the NowPlaying screen to stop the seekbar
    private void StopProgressBar(){
        Log.d(LOG_TAG, "Stop ProgresBar");
        Intent progressBarIntent = new Intent(SERVICE_UPDATE_PROGRESS_BAR_STOP);
        getApplicationContext().sendBroadcast(progressBarIntent);
    }


    //Play/Pause the current track
    synchronized private void playPause(){
        // check for already playing
        if (mp!=null) {
            if (mp.isPlaying()) {
                Log.d(LOG_TAG,"Pausing the media player");
                mp.pause();
            }
            else{
                Log.d(LOG_TAG,"Resume the media player");
                mp.start();
            }
            //Update play pause buttons in the now playing screen
            updatePlayPauseButton();
        }
    }


    //Backward the current track
    synchronized private void backward(){
        Log.d(LOG_TAG,"backward the media player");

        //Todo updateProgressBar
        if (mp!=null){
            // get current song position
            int currentPosition = mp.getCurrentPosition();
            // check if seekBackward time is greater than 0 sec
            if (currentPosition - seekBackwardTime >= 0){
                // forward song
                seekTo(currentPosition - seekBackwardTime);
            }else{
                // backward to starting position
                seekTo(0);
            }
        }
    }


    //Forward the current track
    synchronized private void forward(){
        Log.d(LOG_TAG,"forward the media player");

        if(mp!=null){
            // get current song position
            int currentPosition = mp.getCurrentPosition();
            // check if seekForward time is lesser than song duration
            if (currentPosition + seekForwardTime <= mp.getDuration()) {
                // forward song
                seekTo(currentPosition + seekForwardTime);
            } else {
                // forward to end position
                seekTo(mp.getDuration());
            }
        }
    }

    //Repeat the track
    synchronized private void repeat(){
        if(mModel.isRepeat()){
            mModel.setRepeat(false);
            L.toast(getApplicationContext(), getResources().getString(R.string.repeat_off));
        }else{
            // make repeat to true
            mModel.setRepeat(true);
            L.toast(getApplicationContext(), getResources().getString(R.string.repeat_on));
        }
    }


    //Shuffle the current track
    synchronized private void shuffle(){
        if(mModel.isShuffle()) {
            mModel.setShuffle(false);

            L.toast(getApplicationContext(), getResources().getString(R.string.shuffle_off));
        }else{
            // make repeat to true
            mModel.setShuffle(true);
            L.toast(getApplicationContext(), getResources().getString(R.string.shuffle_on));
            // make shuffle to false
            mModel.setRepeat(false);
        }
    }

    //Seekto the pos millisecond
    synchronized private void seekTo(int pos) {
        Log.d(LOG_TAG,"Seek to position: "+pos);
        if (isPrepared) {
            mp.seekTo(pos);
            StartProgressBar();
        }
    }

    //get the current position of the song
    synchronized private int getPosition() {
        if (isPrepared) {
            return mp.getCurrentPosition();
        }
        return 0;
    }
}
