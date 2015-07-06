package com.princecoder.nanodegree.spotifytreamer.model;

import java.io.Serializable;

/**
 * Created by prinzlyngotoum on 7/1/15.
 */
public class TrackModel implements Serializable, IElement {

    // Album name
    private String album;

    // track name
    private String trackName;

    //Album thumbnail
    private String albThumb;

    // Preview url
    private String prevUrl;

    public String getTrackName() {
        return trackName;
    }

    public void setTrackName(String trackName) {
        this.trackName = trackName;
    }

    public String getAlbThumb() {
        return albThumb;
    }

    public void setAlbThumb(String albThumb) {
        this.albThumb = albThumb;
    }

    public String getPrevUrl() {
        return prevUrl;
    }

    public void setPrevUrl(String prevUrl) {
        this.prevUrl = prevUrl;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    @Override
    public String getBaseInfo() {
        return getAlbum();
    }

    @Override
    public String getSubInfo() {
        return getTrackName();
    }

    @Override
    public String getThumb() {
        return getAlbThumb();
    }

    @Override
    public boolean hasThumb() {
        return (getThumb()!=null && !getThumb().isEmpty());
    }
}
