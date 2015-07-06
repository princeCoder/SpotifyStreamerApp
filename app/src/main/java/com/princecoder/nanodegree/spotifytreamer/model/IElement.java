package com.princecoder.nanodegree.spotifytreamer.model;

/**
 * Created by prinzlyngotoum on 7/4/15.
 *
 * This interface define the base elements the listviews for Artists and tracks should display
 *
 */
public interface IElement {

    // I use this for displaying the artist name or track name
    String getBaseInfo();

    // album name
    String getSubInfo();

    // Thumb name
    String getThumb();

    // Do we have a thumbnail?
    boolean hasThumb();

}
