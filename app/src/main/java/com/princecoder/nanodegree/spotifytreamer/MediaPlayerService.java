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
import android.os.SystemClock;
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
import java.util.Random;


/**
 * Created by Prinzly Ngotoum on 8/1/15.
 *
 * This class helps me to handle the Media player in a service
 */
public class MediaPlayerService extends Service implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnInfoListener {

    //Log Tag
    public final String LOG_TAG =getClass().getSimpleName();

    // My Media Player
    private MediaPlayer mp;

    private boolean isPrepared = false;

    // Media Model
    private MediaModel mModel;

    //List of tracks LOG_TAG
    public static final String TRACKS_LIST="TRACKS_LIST";

    //Current track LOG_TAG
    public static final String CURRENT_TRACK="CURRENT_TRACK";


    private TrackModel mCurrentTrack;

    private Handler mHandler = new Handler();

    // Track whether we ever called start() on the media player so we don't try
    // to reset or release it.
    private boolean mediaPlayerHasStarted = false;

    private int startId;

    // Error handling
    private int errorCount;
    private int connectionErrorWaitTime;
    private int seekToPosition;

    private TelephonyManager telephonyManager;
    private PhoneStateListener listener;
    private boolean isPausedInCall = false;


    // Amount of time to rewind playback when resuming after call
    private final static int RESUME_REWIND_TIME = 3000;
    private final static int ERROR_RETRY_COUNT = 3;
    private final static int RETRY_SLEEP_TIME = 30000;

    private int seekForwardTime = 5000; // 5000 milliseconds
    private int seekBackwardTime = 5000; // 5000 milliseconds

    private Looper serviceLooper;
    private ServiceHandler serviceHandler;

    private Intent lastChangeBroadcast;
    private Intent lastUpdateBroadcast;


    public static final String MEDIASERVICE_PLAYPAUSE="MEDIASERVICE_PLAYPAUSE";
    public static final String MEDIASERVICE_START_PLAYER="MEDIASERVICE_START_PLAYER";
    public static final String MEDIASERVICE_NEXT="MEDIASERVICE_NEXT";
    public static final String MEDIASERVICE_PREVIOUS="MEDIASERVICE_PREVIOUS";
    public static final String MEDIASERVICE_BACKWARD="MEDIASERVICE_BACKWARD";
    public static final String MEDIASERVICE_FORWARD="MEDIASERVICE_FORWARD";
    public static final String MEDIASERVICE_REPEAT="MEDIASERVICE_REPEAT";
    public static final String MEDIASERVICE_SHUFFLE="MEDIASERVICE_SHUFFLE";

    public static final String SERVICE_ERROR_NAME = "SERVICE_ERROR_NAME";
    public static final String SERVICE_CLOSE_NAME = "CLOSE";
    public static final String SERVICE_CHANGE_NAME = "CHANGE";
    public static final String SERVICE_UPDATE_NAME = "UPDATE";

    public static final String EXTRA_DURATION = "EXTRA_DURATION";
    public static final String EXTRA_POSITION = "EXTRA_POSITION";
    public static final String EXTRA_IS_PLAYING = "EXTRA_IS_PLAYING";
    public static final String EXTRA_IS_PREPARED = "EXTRA_IS_PREPARED";
    public static final String EXTRA_DOWNLOADED = "EXTRA_DOWNLOADED";

    public static final String EXTRA_ERROR = "EXTRA_ERROR";

    public enum PLAYBACK_SERVICE_ERROR {Connection, Playback, InvalidPlayable}

    private String currentAction;



    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            startId = msg.arg1;
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
        // TODO initialize my Media player

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
        return START_STICKY;
    }



    protected void onHandleIntent(Intent intent) {
        if (intent == null || intent.getAction() == null) {
            Log.d(LOG_TAG, "Null intent received");
            return;
        }

        String action = intent.getAction();
        currentAction=action;
        Log.d(LOG_TAG, "Media Player service action received: " + action);
        switch (action){
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
            case MEDIASERVICE_START_PLAYER:
                playCurrent(0,1);
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

//        if (mHandler != null) {
//            mHandler.
//            updateProgressThread.interrupt();
//            try {
//                updateProgressThread.join(1000);
//            } catch (InterruptedException e) {
//                Log.e(LOG_TAG, "", e);
//            }
//        }

        synchronized (this) {
            if (mp != null) {
                if (mediaPlayerHasStarted) {
                    mp.release();
                } else {
                    mp.setOnBufferingUpdateListener(null);
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

        if (lastChangeBroadcast != null) {
            getApplicationContext().removeStickyBroadcast(lastChangeBroadcast);
        }
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
                playCurrent(0, 1);
            } else if(mModel.isShuffle()){
                // shuffle is on - play a random song
                Random rand = new Random();
                int currentSongIndex = rand.nextInt((mModel.getTrackList().size() - 1)+ 1);
                mModel.setCurrentSongIndex(currentSongIndex);
                playCurrent(0, 1);
            } else{
                // no repeat or shuffle ON - play next song
                if(mModel.getCurrentSongIndex() < mModel.getTrackList().size() - 1){
                    int currentSongIndex = mModel.getCurrentSongIndex() + 1;
                    mModel.setCurrentSongIndex(currentSongIndex);
                    playCurrent(0, 1);


                }else{
                    // play first song
                    mModel.setCurrentSongIndex(0);
                    playCurrent(0, 1);
                }
            }
    }


    //Pause the Media player
    synchronized private void pause() {
        Log.d(LOG_TAG, "pause");
        if (isPrepared) {
            if (mModel.getCurrentTrack() != null) {
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
        }
    }


    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
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

        if (seekToPosition > 0) {
            Log.d(LOG_TAG, "Seeking to starting position: " + seekToPosition);
            if(mp!=null)
            mp.seekTo(seekToPosition);
        } else {
            startPlaying();
        }
    }


    // Resume the mediaplayer
    private void resumePlaying() {
        if (mModel.getCurrentTrack() != null) {
            if (isPrepared) {
                play();
            } else {
                playCurrent(0, 1);
            }
        }
    }

    // Play current track index
    private boolean playCurrent(int startingErrorCount, int startingWaitTime) {
        errorCount = startingErrorCount;
        connectionErrorWaitTime = startingWaitTime;
        while (errorCount < ERROR_RETRY_COUNT) {
            try {
                if(mModel!=null){
                    if (mModel.getCurrentTrack() == null || mModel.getCurrentTrack().getPrevUrl() == null) {
                        Intent intent = new Intent(SERVICE_ERROR_NAME);
                        intent.putExtra(EXTRA_ERROR, PLAYBACK_SERVICE_ERROR.InvalidPlayable.ordinal());
                        getApplicationContext().sendBroadcast(intent);

                        return false;
                    }
                    TrackModel track=mModel.getTrackList().get(mModel.getCurrentSongIndex());
                    mModel.setCurrentTrack(track);
                    prepareThenPlay(mModel.getCurrentTrack().getPrevUrl());
                    return true;
                }
            } catch (UnknownHostException e) {
                Log.w(LOG_TAG, "Unknown host in playCurrent");
                handleConnectionError();
            } catch (ConnectException e) {
                Log.w(LOG_TAG, "Connect exception in playCurrent");
                handleConnectionError();
            } catch (IOException e) {
                Log.e(LOG_TAG, "IOException on playlist entry " + mModel.getCurrentTrack().getTrackName(), e);
                incrementErrorCount();
            } catch (IllegalStateException e) {
                Log.e(LOG_TAG, "Illegal state exception trying to play entry " + mModel.getCurrentTrack().getTrackName(), e);
                incrementErrorCount();
            }
        }

        return false;
    }


    private void prepareThenPlay(String url)
            throws IllegalArgumentException, IllegalStateException, IOException {
        Log.d(LOG_TAG, "prepareThenPlay " + mModel.getCurrentTrack().getTrackName());
        // First, clean up any existing audio.
        stop();
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
        //Call the handler to update UI elements
        mHandler.postDelayed(mUpdateUITask, 100);
    }


    /**
     * Background Runnable thread
     * */
    private Runnable mUpdateUITask = new Runnable() {
        public void run() {
            updateProgress();
        }
    };



    /**
     * Sends an UPDATE broadcast with the latest info.
     */
    private void updateProgress() {
        Log.d(LOG_TAG, "UpdateProgress");
    }


    //Play the current track
    synchronized private void play() {
        if (!isPrepared || mModel.getCurrentTrack() == null) {
            Log.e(LOG_TAG, "play - not prepared");
            return;
        }
        Log.d(LOG_TAG, "play " + mModel.getCurrentTrack().getTrackName());


        mp.start();
        mediaPlayerHasStarted = true;

        //presentPlayingNotification();

        // Change broadcasts are sticky, so when a new receiver connects, it will
        // have the data without polling.

        if (lastChangeBroadcast != null) {
            getApplicationContext().removeStickyBroadcast(lastChangeBroadcast);
        }
        lastChangeBroadcast = new Intent(SERVICE_CHANGE_NAME);
        lastChangeBroadcast.putExtra(CURRENT_TRACK, mModel.getCurrentTrack());
        getApplicationContext().sendStickyBroadcast(lastChangeBroadcast);

    }


    // Play previous track
    synchronized private void previous(){
        if(mModel.getCurrentSongIndex() > 0){
            int currentSongIndex = mModel.getCurrentSongIndex() - 1;
            mModel.setCurrentSongIndex(currentSongIndex);
        }else{
            mModel.setCurrentSongIndex(mModel.getTrackList().size() - 1);
        }
        playCurrent(0, 1);

        //Todo update NowPlaying Screen
    }

    //Play next song
    synchronized private void next(){
        // check if next song is there or not
        if(mModel.getCurrentSongIndex() < (mModel.getTrackList().size() - 1)){
            int currentSongIndex = mModel.getCurrentSongIndex() + 1;
            mModel.setCurrentSongIndex(currentSongIndex);

        }else{
            // play first song
            mModel.setCurrentSongIndex(0);
        }
        playCurrent(0,1);
        //Todo update NowPlaying Screen
    }

    //Play/Pause the track
    synchronized private void playPause(){
        // check for already playing
        if (mp!=null) {
            if (mp.isPlaying()) {
                Log.d(LOG_TAG,"Pausing the media player");
                mp.pause();
                //TODO reset playPause button
            }
            else{
                Log.d(LOG_TAG,"Resume the media player");
                mp.start();
                //TODO reset playPause button
            }
        }
    }


    //Backward the current track
    synchronized private void backward(){
        Log.d(LOG_TAG,"backward the media player");
        // get current song position
        int currentPosition = mp.getCurrentPosition();
        // check if seekBackward time is greater than 0 sec
        if (currentPosition - seekBackwardTime >= 0){
            // forward song
            mp.seekTo(currentPosition - seekBackwardTime);
        }else{
            // backward to starting position
            mp.seekTo(0);
        }
    }


    //Forward the current track
    synchronized private void forward(){
        Log.d(LOG_TAG,"forward the media player");
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
        if(mModel.isShuffle()){
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
        if (isPrepared) {
            seekToPosition = 0;
            mp.seekTo(pos);
        }
    }

    //get the current position of the song
    synchronized private int getPosition() {
        if (isPrepared) {
            return mp.getCurrentPosition();
        }
        return 0;
    }

    private void incrementErrorCount() {
        errorCount++;
        Log.e(LOG_TAG, "Media player increment error count:" + errorCount);
        if (errorCount >= ERROR_RETRY_COUNT) {
            Intent intent = new Intent(SERVICE_ERROR_NAME);
            intent.putExtra(EXTRA_ERROR, PLAYBACK_SERVICE_ERROR.Playback.ordinal());
            getApplicationContext().sendBroadcast(intent);
        }
    }

    private void handleConnectionError() {
        connectionErrorWaitTime *= 5;
        if (connectionErrorWaitTime > RETRY_SLEEP_TIME) {
            Log.e(LOG_TAG, "Connection failed.  Resetting mediaPlayer" +
                    " and trying again in 30 seconds.");

            Intent intent = new Intent(SERVICE_ERROR_NAME);
            intent.putExtra(EXTRA_ERROR, PLAYBACK_SERVICE_ERROR.Connection.ordinal());
            getApplicationContext().sendBroadcast(intent);

            connectionErrorWaitTime = RETRY_SLEEP_TIME;
            // Send error notification and keep waiting
            isPrepared = false;
            mp.reset();
        } else {
            Log.w(LOG_TAG, "Connection error. Waiting for " +
                    connectionErrorWaitTime + " milliseconds.");
        }
        SystemClock.sleep(connectionErrorWaitTime);
    }


}
