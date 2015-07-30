package com.princecoder.nanodegree.spotifytreamer.model;

import android.media.MediaPlayer;

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

    // Set the current song in the Media Player
    private int mCurrentSongIndex = 0;

    private TrackModel mCurrentTrack = null;
    private Boolean mCurrentSongIsValid = false;

    // Shuffle mode
    private boolean mShuffle = false;

    /* Repeat mode */
    private boolean mRepeat = false;

    /** flag indicating if the mediaplayer is playing a song or not */
    private Boolean mMediaPlayerIsPlaying = false;

    /** flag indicating if the mediaplayer is paused or not */
    private Boolean mMediaPlayerIsPaused = false;


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
     *         The current song index in the Media player
     */
    public int getCurrentSongIndex() {
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

    /**
     *
     * @return true if the mediaplayer is currently playing a song
     */
    public Boolean isPlayingASong() {
        return mMediaPlayerIsPlaying;
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
}
