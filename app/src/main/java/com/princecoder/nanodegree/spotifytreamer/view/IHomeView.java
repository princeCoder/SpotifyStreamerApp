package com.princecoder.nanodegree.spotifytreamer.view;

import com.princecoder.nanodegree.spotifytreamer.model.IElement;

import java.util.ArrayList;

/**
 * Created by Prinzly Ngotoum on 10/27/15.
 */
public interface IHomeView {

    //Update the empty view with the right message
    void updateEmptyView(String message);

    //Find list of track.
    //This method is called when the user decided to find tracks of the artists
    void findArtists(String artist);

    //Display tracks of the artist
    void displayTracks(int position);

    //Display Artists
    void displayArtists(ArrayList<IElement> artists);

    //When the user click on a row in the listView
    void OnItemClick(int position);

    //Clear the list of Artists
    void clearList();

    public ArrayList<IElement> getmListOfArtist();

    public void setmListOfArtist(ArrayList<IElement> mListOfArtist);

}
