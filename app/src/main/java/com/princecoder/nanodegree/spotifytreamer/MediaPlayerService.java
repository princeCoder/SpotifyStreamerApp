package com.princecoder.nanodegree.spotifytreamer;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.RemoteViews;

import com.princecoder.nanodegree.spotifytreamer.model.MediaModel;
import com.princecoder.nanodegree.spotifytreamer.model.TrackModel;
import com.princecoder.nanodegree.spotifytreamer.utils.L;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
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

    private TelephonyManager telephonyManager;
    private PhoneStateListener listener;
    private boolean isPausedInCall = false;

    private int seekForwardTime = 3000; // 3000 milliseconds
    private int seekBackwardTime = 3000; // 3000 milliseconds

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
    public static final String SERVICE_UPDATE_REPEAT="SERVICE_UPDATE_REPEAT";
    public static final String SERVICE_UPDATE_SHUFFLE="SERVICE_UPDATE_SHUFFLE";
    public static final String SERVICE_UPDATE_UI="SERVICE_UPDATE_UI";
    public static final String EXTRA_ERROR = "EXTRA_ERROR";
    public static final String MEDIASERVICE_RESUME_PLAYING="MEDIASERVICE_RESUME_PLAYING";

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

        L.m(LOG_TAG, "MediaPlayer service created");

        telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);

        // Create a PhoneStateListener to watch for off-hook and idle events
        listener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                switch (state) {
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                    case TelephonyManager.CALL_STATE_RINGING:
                        // Phone going off-hook or ringing, pause the player.
                        if (isPlaying()) {
                            isPausedInCall = true;
                            pause();
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        // Phone idle. Rewind a couple of seconds and start playing.
                        if (isPausedInCall) {
                            isPausedInCall = false;
                            seekTo(getPosition());
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
            L.m(LOG_TAG, "Null intent received");
            return;
        }

        currentAction=intent.getAction();
        L.m(LOG_TAG, "Media Player service action received: " + currentAction);
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
                seekTo(intent.getIntExtra(CURRENT_POSITION, 0));
                break;
            case MEDIASERVICE_RESUME_PLAYING:
                resumePlaying();
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
                if (mModel.isMediaPlayerHasStarted()) {
                    //Release the Media player
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

        //Stop the service looper
        serviceLooper.quit();
        stopForeground(true);

        //Remove TelephonyManager listener
        telephonyManager.listen(listener, PhoneStateListener.LISTEN_NONE);

        //Cancel notifications
        NotificationManager manager=(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancelAll();

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.w(LOG_TAG, "onComplete()");
        if(isOnline()){
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
        else {
            handleMediaPlayerError(MEDIAPLAYER_SERVICE_ERROR.Connection);
//            StopProgressBar();
        }
    }

    //Is the Media player playing?
    synchronized private boolean isPlaying() {
        return isPrepared && mp.isPlaying();
    }


    //Pause the Media player
    synchronized private void pause() {
        L.m(LOG_TAG, "pause");

        if (isPrepared && mp.isPlaying()) {
            L.m(LOG_TAG, "Pausing the media player");

            //Stop the progress bar
            StopProgressBar();
            mp.pause();

            mModel.setPause(true);

            displayNotification();

            //Update UI
            updateUI();
        }
    }

    //Stop the Media player
    synchronized private void stop() {
        L.m(LOG_TAG, "stop");
        if (isPrepared) {
            isPrepared = false;
            // Send a broadcast to the Now Playing to stop the handler
            StopProgressBar();
            mp.stop();

        }
    }


    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        L.m(LOG_TAG, "Error:  " + what + " " + extra);
        //Reset the media Player
        if(!isOnline()){
            handleMediaPlayerError(MEDIAPLAYER_SERVICE_ERROR.Connection);
        }
        else{
            handleMediaPlayerError(MEDIAPLAYER_SERVICE_ERROR.MediaPlayer);
        }
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        L.m(LOG_TAG, "Prepared  --- onPrepared");
        synchronized (this) {
            if (mp != null) {
                isPrepared = true;
            }
        }
        // Start playing the track
        startPlaying();
    }

    // Play current track index
    private boolean playCurrent() {
        try {
            if(mModel!=null){
                if(mCurrentTrackIndex<mListTracks.size()){
                    mCurrentTrack=mListTracks.get(mCurrentTrackIndex);
                    if (mCurrentTrack == null || mCurrentTrack.getPrevUrl() == null) {
                        mp.reset();
                        Intent intent = new Intent(SERVICE_ERROR_NAME);
                        intent.putExtra(EXTRA_ERROR, MEDIAPLAYER_SERVICE_ERROR.InvalidTrack.ordinal());
                        getApplicationContext().sendBroadcast(intent);

                        //move to the next track
                        next();

                        return false;
                    }
                    //Update the model with the current song
                    mModel.setCurrentTrack(mCurrentTrack);
                    //Prepare then play the current song
                    prepareThenPlay(mCurrentTrack.getPrevUrl());
                    return true;
                }
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
                mp.reset();
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


    synchronized private int getPosition() {
        if (isPrepared) {
            return mp.getCurrentPosition();
        }
        return 0;
    }


    synchronized private void resumePlaying() {
        L.m(LOG_TAG, "----------------------- Media Service  resumePlaying");
        if (mp.isPlaying())
            //I resume the progress bar
            StartProgressBar();
        // I resume the UI
        updateUI();
    }



    //Prepare then play a song using the url
    synchronized private void prepareThenPlay(String url)
            throws IllegalArgumentException, IllegalStateException, IOException {
        L.m(LOG_TAG, "prepareThenPlay " + mCurrentTrack.getTrackName());
        // First, clean up any existing audio.
        stop();
        mModel.setMediaPlayerHasStarted(false);
        L.m(LOG_TAG, "listening to " + url);
        synchronized (this) {
            L.m(LOG_TAG, "reset: " + url);
            mp.reset();
            mp.setDataSource(url);
            mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
            L.m(LOG_TAG, "Preparing: " + url);
            mp.prepareAsync();
            L.m(LOG_TAG, "Waiting for prepare");
        }
    }


    /**
     *  Check if we are online
     */
    protected boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return (netInfo != null && netInfo.isConnectedOrConnecting());
    }


    //Start playing the current track
    private void startPlaying() {
        L.m(LOG_TAG, "StartPlaying: ");
        play();

    }


    //Play the current track
    synchronized private void play() {
        if (!isPrepared || mCurrentTrack == null) {
            Log.e(LOG_TAG, "play - not prepared");
            return;
        }
        L.m(LOG_TAG, "play " + mCurrentTrack.getTrackName());
        mp.start();

        mModel.setPause(false);

        mModel.setMediaPlayerHasStarted(true);

        // Display the notification
        displayNotification();

        // Start the progress bar
        StartProgressBar();

        //Update UI Elements
        updateUI();
    }

    // Play previous track
    synchronized private void previous(){
        if(isOnline()){
            if(mCurrentTrackIndex > 0){
                int currentSongIndex = mCurrentTrackIndex - 1;
                mCurrentTrackIndex=currentSongIndex;
                mModel.setCurrentSongIndex(currentSongIndex);
            }else{
                mModel.setCurrentSongIndex(mListTracks.size() - 1);
            }
            playCurrent();
        }
        else{
            handleMediaPlayerError(MEDIAPLAYER_SERVICE_ERROR.Connection);
        }
    }

    //Play next song
    synchronized private void next(){
        if(isOnline()){
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
        else{
            handleMediaPlayerError(MEDIAPLAYER_SERVICE_ERROR.Connection);
        }
    }

    //Send  a broacast to the Now playing screen to update the play/Pause button
    synchronized private void updateUI(){
        L.m(LOG_TAG, "updateUI");
        Intent uiIntent = new Intent(SERVICE_UPDATE_UI);
        getApplicationContext().sendBroadcast(uiIntent);
    }

    //Send  a broacast to the Now playing screen to update the play/Pause button
    synchronized private void updatePlayPauseButton(){
        L.m(LOG_TAG, "updatePlayPauseButton");
        Intent playPauseIntent = new Intent(SERVICE_UPDATE_PLAY_PAUSE);
        getApplicationContext().sendBroadcast(playPauseIntent);
    }

    //Send  a broacast to the Now playing screen to update the repeat button
    synchronized private void updateRepeatButton(){
        L.m(LOG_TAG, "updateRepeatButton");
        Intent repeatIntent = new Intent(SERVICE_UPDATE_REPEAT);
        getApplicationContext().sendBroadcast(repeatIntent);
    }

    //Send  a broacast to the Now playing screen to update the shuffle button
    synchronized private void updateShuffleButton(){
        L.m(LOG_TAG, "updateShuffleButton");
        Intent shuffleIntent = new Intent(SERVICE_UPDATE_SHUFFLE);
        getApplicationContext().sendBroadcast(shuffleIntent);
    }

    //Send a broadcast to the NowPlaying screen to start the seekbar
    synchronized private void StartProgressBar(){
        L.m(LOG_TAG, "start progressBar");
        Intent progressBarIntent = new Intent(SERVICE_UPDATE_PROGRESS_BAR_START);
        getApplicationContext().sendBroadcast(progressBarIntent);
    }


    //Send a broadcast to the NowPlaying screen to stop the seekbar
    synchronized private void StopProgressBar(){
        L.m(LOG_TAG, "Stop ProgresBar");
        Intent progressBarIntent = new Intent(SERVICE_UPDATE_PROGRESS_BAR_STOP);
        getApplicationContext().sendBroadcast(progressBarIntent);
    }


    //Play/Pause the current track
    synchronized private void playPause(){
        // check for already playing
        if (mp!=null) {
            if (mp.isPlaying()) {
                pause();
                mModel.setPause(true);
            }
            else{
                if(isOnline()){
                    play();
                }
                else {
                    handleMediaPlayerError(MEDIAPLAYER_SERVICE_ERROR.Connection);
                }
            }

            //Update UI
            updateUI();
        }
    }


    //Backward the current track
    synchronized private void backward(){
        L.m(LOG_TAG, "backward the media player");

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
        L.m(LOG_TAG, "forward the media player");

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
        updateRepeatButton();
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
        updateShuffleButton();
    }

    //Seekto the pos millisecond
    synchronized private void seekTo(int pos) {
        L.m(LOG_TAG, "Seek to position: " + pos);
        if (isPrepared) {
            mp.seekTo(pos);
            StartProgressBar();
        }
    }


    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            // Log exception
            return null;
        }
    }

    public static Bitmap scaleDown(Bitmap realImage, float maxImageSize,
                                   boolean filter) {
        float ratio = Math.min(
                (float) maxImageSize / realImage.getWidth(),
                (float) maxImageSize / realImage.getHeight());
        int width = Math.round((float) ratio * realImage.getWidth());
        int height = Math.round((float) ratio * realImage.getHeight());

        Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width,
                height, filter);
        return newBitmap;
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private synchronized void displayNotification(){

        // Creates an explicit intent for an Activity in your app
//        Intent resultIntent = new Intent(this, MediaPlayerService.class);
//        PendingIntent resultPendingIntent=PendingIntent.getService(this,0,resultIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        RemoveControlWidget remoteView = new RemoveControlWidget(getApplicationContext(),getApplicationContext().getPackageName(), R.layout.notification_content);
//        remoteView.setOnClickPendingIntent(R.id.normal, resultPendingIntent);

        // L.m(LOG_TAG, "-------------------------  "+mModel.getCurrentTrack().getThumb()+" -------------------------");

        Bitmap image = getBitmapFromURL(mModel.getCurrentTrack().getThumb());
        remoteView.setImageViewBitmap(R.id.AppThumb,image);
        //removeWidget.setImageViewResource(R.id.AppThumb,R.mipmap.ic_launcher);
        remoteView.setTextViewText(R.id.songTitle, mModel.getCurrentTrack().getTrackName());
        if(mModel.isPaused()){
            remoteView.setImageViewResource(R.id.btnPlay, R.mipmap.img_btn_play);
        }
        else {
            remoteView.setImageViewResource(R.id.btnPlay, R.mipmap.img_btn_pause);
        }

        NotificationCompat.Builder mBuilder =
                (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                        // Show controls on lock screen even when user hides sensitive content.
                        .setVisibility(Notification.VISIBILITY_PUBLIC)
                        .setContentTitle(getResources().getString(R.string.app_name))
                        .setContentText(mModel.getCurrentTrack().getTrackName())
                        .setSmallIcon(R.mipmap.ic_launcher).setAutoCancel(true)
//                        .setContentIntent(resultPendingIntent)
                        .setContent(remoteView);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        notificationManager.notify(0,mBuilder.build());
    }



    public class RemoveControlWidget extends RemoteViews
    {
        private final Context mContext;

        public static final String ACTION_PLAY_PAUSE = MEDIASERVICE_PLAYPAUSE;

        public static final String ACTION_PREVIOUS = MEDIASERVICE_PREVIOUS;

        public static final String ACTION_NEXT = MEDIASERVICE_NEXT;

        public static final String ACTION_BACKWARD = MEDIASERVICE_BACKWARD;

        public static final String ACTION_FORWARD = MEDIASERVICE_FORWARD;


        public RemoveControlWidget(Context context , String packageName, int layoutId)
        {
            super(packageName, layoutId);
            mContext = context;
            Intent intent = new Intent(getApplicationContext(), MediaPlayerService.class);

            intent.setAction(ACTION_PLAY_PAUSE);
            PendingIntent pendingIntent = PendingIntent.getService(mContext.getApplicationContext(),100,
                    intent,PendingIntent.FLAG_UPDATE_CURRENT);
            setOnClickPendingIntent(R.id.btnPlay, pendingIntent);


            intent.setAction(ACTION_PREVIOUS);
            pendingIntent = PendingIntent.getService(mContext.getApplicationContext(),101,
                    intent,PendingIntent.FLAG_UPDATE_CURRENT);
            setOnClickPendingIntent(R.id.btnPrevious, pendingIntent);

            intent.setAction(ACTION_NEXT);
            pendingIntent = PendingIntent.getService(mContext.getApplicationContext(),102,
                    intent,PendingIntent.FLAG_UPDATE_CURRENT);
            setOnClickPendingIntent(R.id.btnNext, pendingIntent);

            intent.setAction(ACTION_BACKWARD);
            pendingIntent = PendingIntent.getService(mContext.getApplicationContext(),103,
                    intent,PendingIntent.FLAG_UPDATE_CURRENT);
            setOnClickPendingIntent(R.id.btnBackward, pendingIntent);

            intent.setAction(ACTION_FORWARD);
            pendingIntent = PendingIntent.getService(mContext.getApplicationContext(),104,
                    intent,PendingIntent.FLAG_UPDATE_CURRENT);
            setOnClickPendingIntent(R.id.btnForward,pendingIntent);


        }
    }
}
