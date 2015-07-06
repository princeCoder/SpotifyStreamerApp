package com.princecoder.nanodegree.spotifytreamer.model;

import java.io.Serializable;

/**
 * Created by prinzlyngotoum on 7/1/15.
 */
public class ArtistModel  implements Serializable, IElement {

    //Name of the artist
    private String name;

    // Spotify Id
    private String SpotifyId;

    // Artist Thumbnail
    private String artThumb;


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
}
