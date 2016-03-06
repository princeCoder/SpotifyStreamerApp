package com.princecoder.nanodegree.spotifytreamer.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by prinzlyngotoum on 7/1/15.
 */
public class ArtistModel  implements Parcelable, IElement {

    //Name of the artist
    private String name;

    // Spotify Id
    private String SpotifyId;

    // Artist Thumbnail
    private String artThumb;


    protected ArtistModel(Parcel in) {
        name = in.readString();
        SpotifyId = in.readString();
        artThumb = in.readString();
    }

    public ArtistModel(){
        name=null;
        SpotifyId=null;
        artThumb=null;
    }

    public static final Creator<ArtistModel> CREATOR = new Creator<ArtistModel>() {
        @Override
        public ArtistModel createFromParcel(Parcel in) {
            return new ArtistModel(in);
        }

        @Override
        public ArtistModel[] newArray(int size) {
            return new ArtistModel[size];
        }
    };

    /*

        GETTER AND SETTER
         */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpotifyId() {
        return SpotifyId;
    }

    public void setSpotifyId(String spotifyId) {
        SpotifyId = spotifyId;
    }

    public String getArtThumb() {
        return artThumb;
    }

    public void setArtThumb(String artThumb) {
        this.artThumb = artThumb;
    }

    @Override
    public String getBaseInfo() {
        return getName();
    }

    @Override
    public String getSubInfo() {
        return null;
    }

    @Override
    public String getThumb() {
        return getArtThumb();
    }

    @Override
    public boolean hasThumb() {
        return  (artThumb!=null && !artThumb.isEmpty());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

        parcel.writeString(name);
        parcel.writeString(SpotifyId);
        parcel.writeString(artThumb);
    }
}
