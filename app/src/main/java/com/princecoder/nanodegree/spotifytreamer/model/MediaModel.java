package com.princecoder.nanodegree.spotifytreamer.model;

import android.media.MediaPlayer;

import java.util.ArrayList;

/**
 * Created by Prinzly Ngotoum on 7/27/15.
 */

//This class is a singleton because I want to make sure we always have one instance to control the media player
public class MediaModel {

    //Log Tag
    private  final String TAG=getClass().getSimpleName();
    // --------------------------------------------------------------------------------
    // Singleton
    // --------------------------------------------------------------------------------
    static private volatile MediaModel mInstance = null;

    static public MediaModel getInstance() {
        if (mInstance == null) {
            synchronized (MediaModel.class) {
                if (mInstance == null) {
                    mInstance = new MediaModel();
                }
            }
        }
        return mInstance;
    }



    //to know if the Media is paused
    private boolean mPaused = false;

    // flag indicating if the media is stopped
    private boolean mStopped = true;

    // mediaplayer
    private MediaPlayer mMediaPlayer;

    // Current track index in the Media Player
    private int mCurrentSongIndex = 0;

    //Current track
    private TrackModel mCurrentTrack = null;

    private ArrayList<TrackModel> mTrackList=new ArrayList<>();

    //Is the song valid?
    private Boolean mCurrentSongIsValid = false;

    // Shuffle mode
    private boolean mShuffle = false;

    /* Repeat mode */
    private boolean mRepeat = false;

    /** flag indicating if the mediaplayer is playing a song or not */
    private Boolean mMediaPlayerIsPlaying = false;

    /** flag indicating if the mediaplayer is paused or not */
    private Boolean mMediaPlayerIsPaused = false;

    //Does the user triggered the now playing screen?
    private boolean mNowPlayingTriggeredByUser=false;

    // Track whether we ever called start() on the media player so we don't try
    // to reset or release it.
    private boolean mediaPlayerHasStarted = false;


    /**
     *
     * @return
     *         True if the Media is shuffle or false if not
     */
    public boolean isShuffle() {
        return mShuffle;
    }

    /**
     *
     * @return
     *         True if the repeat mode is set or false if not
     */
    public boolean isRepeat() {
        return mRepeat;
    }

    /**
     * Set the current song index
     *
     * @param index
     */
    public void setCurrentSongIndex(int index) {
        mCurrentSongIndex = index;
        mCurrentSongIsValid = false;
    }

    /**
     *
     * @return
     *         The current Track index in the Media player
     */
    public int getCurrentTrackIndex() {
        return mCurrentSongIndex;
    }

// --------------------------------------------------------------------------------
    //	Media Player
    // --------------------------------------------------------------------------------
    /**
     *
     * @return
     *         The Media Player
     */
    public MediaPlayer getMediaPlayer() {
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
        }
        return mMediaPlayer;
    }

    /**
     *
     * @return
     *         True if the Media Player is Paused or False if not
     */
    public Boolean getMediaPlayerIsPaused() {
        return mMediaPlayerIsPaused;
    }

    /**
     *
     * @return
     *         True if the Media Player is Paused or False if not
     */
    public boolean isPaused() {
        return mPaused;
    }

    public boolean isStopped() {
        return mStopped;
    }

    public boolean isMediaPlayerHasStarted() {
        return mediaPlayerHasStarted;
    }

    public void setMediaPlayerHasStarted(boolean mediaPlayerHasStarted) {
        this.mediaPlayerHasStarted = mediaPlayerHasStarted;
    }

    /**
     *
     * @return true if the mediaplayer is currently playing a song
     */
    public Boolean isPlaying() {
        return mMediaPlayerIsPlaying;
    }


    public boolean isNowPlayingTriggeredByUser() {
        return mNowPlayingTriggeredByUser;
    }

    public void setNowPlayingTriggeredByUser(boolean mNowPlayingTriggeredByUser) {
        this.mNowPlayingTriggeredByUser = mNowPlayingTriggeredByUser;
    }

    /**
     *
     * @param mPaused
     */
    public void setPause(boolean mPaused) {
        this.mPaused = mPaused;
    }

    /**
     *
     * @param stop
     */
    public void setStop(boolean stop) {
        this.mStopped = stop;
    }

    /**
     * set the media player
     *
     * @param mMediaPlayer
     */
    public void setMediaPlayer(MediaPlayer mMediaPlayer) {
        this.mMediaPlayer = mMediaPlayer;
    }

    /**
     *
     * @param mMediaPlayerIsPaused
     */
    public void setMediaPlayerIsPaused(Boolean mMediaPlayerIsPaused) {
        this.mMediaPlayerIsPaused = mMediaPlayerIsPaused;
    }

    /**
     * Set the repeat mode
     *
     * @param mRepeat
     */
    public void setRepeat(boolean mRepeat) {
        this.mRepeat = mRepeat;
    }

    /**
     * Set the shuffle mode
     * @param mShuffle
     */
    public void setShuffle(boolean mShuffle) {
        this.mShuffle = mShuffle;
    }

    public TrackModel getCurrentTrack() {
        return mCurrentTrack;
    }

    public void setCurrentTrack(TrackModel mCurrentTrack) {
        this.mCurrentTrack = mCurrentTrack;
    }

    public Boolean getCurrentSongIsValid() {
        return mCurrentSongIsValid;
    }

    public void setCurrentSongIsValid(Boolean mCurrentSongIsValid) {
        this.mCurrentSongIsValid = mCurrentSongIsValid;
    }

    public ArrayList<TrackModel> getTrackList() {
        return mTrackList;
    }

    public void setTrackList(ArrayList<TrackModel> mTrackList) {
        this.mTrackList = mTrackList;
    }
}
