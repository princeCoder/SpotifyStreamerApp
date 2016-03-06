package com.princecoder.nanodegree.spotifytreamer.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by prinzlyngotoum on 7/1/15.
 */
public class TrackModel implements Parcelable, IElement {

    // Album name
    private String album;

    // track name
    private String trackName;

    //Album thumbnail
    private String albThumb;

    // Artist name
    private String artist;

    // Preview url
    private String prevUrl;


    // external spotify url
    private String externalUrl;

    public TrackModel(){
        trackName=null;
        albThumb=null;
        artist=null;
        prevUrl=null;
        album=null;
    }

    protected TrackModel(Parcel in) {
        album = in.readString();
        trackName = in.readString();
        albThumb = in.readString();
        artist = in.readString();
        prevUrl = in.readString();
        externalUrl = in.readString();
    }

    public static final Creator<TrackModel> CREATOR = new Creator<TrackModel>() {
        @Override
        public TrackModel createFromParcel(Parcel in) {
            return new TrackModel(in);
        }

        @Override
        public TrackModel[] newArray(int size) {
            return new TrackModel[size];
        }
    };

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

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getExternalUrl() {
        return externalUrl;
    }

    public void setExternalUrl(String externalUrl) {
        this.externalUrl = externalUrl;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

        parcel.writeString(album);
        parcel.writeString(trackName);
        parcel.writeString(albThumb);
        parcel.writeString(artist);
        parcel.writeString(prevUrl);
        parcel.writeString(externalUrl);
    }
}
