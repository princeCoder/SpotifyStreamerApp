package com.princecoder.nanodegree.spotifytreamer.view;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.princecoder.nanodegree.spotifytreamer.R;
import com.princecoder.nanodegree.spotifytreamer.adapter.RecyclerViewArtistAdapter;
import com.princecoder.nanodegree.spotifytreamer.model.ArtistModel;
import com.princecoder.nanodegree.spotifytreamer.model.IElement;
import com.princecoder.nanodegree.spotifytreamer.presenter.ArtistPresenter;
import com.princecoder.nanodegree.spotifytreamer.presenter.IArtistPresenter;
import com.princecoder.nanodegree.spotifytreamer.service.ArtistAsyncTask;
import com.princecoder.nanodegree.spotifytreamer.utils.Utilities;

import java.util.ArrayList;

/**
 * Todo do something
 *
 * -
 *
 */



/**
 *
 */
public class HomeFragment extends Fragment implements IHomeView {

    private TextView emptyView;

    // List of Artists
    private ArrayList<IElement> mListOfArtist=new ArrayList<>();

    // My adapter
    private RecyclerViewArtistAdapter mAdapter;

    // My recyclerView
    private RecyclerView mRecyclerView;

    // Log field
    private final String TAG=getClass().getSimpleName();

    // EditText for enter artist name
    private android.support.v7.widget.SearchView mSearchText;

    //Listener
    private OnArtistSelectedListener mListener;

    //Position
    private int mPosition;

    //Selected item
    private final String SELECTED_KEY="Selected_key";

    //List_TAG
    private final String LIST_TAG="List";

    //Presenter
    IArtistPresenter mPresenter;

    //Asynctask
    ArtistAsyncTask mTask;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnArtistSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(TAG + activity.getString(R.string.artist_selected_class_cast_exception_message));
        }
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPresenter=new ArtistPresenter(this,getActivity());
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        View myView= inflater.inflate(R.layout.fragment_home, container, false);

        mSearchText =(SearchView)myView.findViewById(R.id.searchText);
        emptyView=(TextView)myView.findViewById(R.id.listview_spotify_empty);

        mRecyclerView=(RecyclerView)myView.findViewById(R.id.artist_recyclerview);
        // Set the layout manager
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mAdapter=new RecyclerViewArtistAdapter(getContext(), new RecyclerViewArtistAdapter.ViewHolderOnClickHandler() {
            @Override
            public void onClick(int id, RecyclerViewArtistAdapter.ViewHolder vh) {
                OnItemClick(id);
            }
        });

        if(savedInstanceState!=null){
            if(savedInstanceState.containsKey(SELECTED_KEY)){
                mPosition = savedInstanceState.getInt(SELECTED_KEY);
            }
        }


        if (mPosition != RecyclerView.NO_POSITION) {
            // If we don't need to restart the loader, and there's a desired position to restore
            // to, do so now.
            mRecyclerView.smoothScrollToPosition(mPosition);
            mAdapter.setSelectedItem(mPosition);
        }

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);

        mSearchText = (SearchView) myView.findViewById(R.id.searchText);

        mSearchText.setIconifiedByDefault(false);
        mSearchText.setQueryHint(getResources().getString(R.string.editText_hint));

        mSearchText.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                String searchKeyword = mSearchText.getQuery().toString();
                if (!searchKeyword.isEmpty()) {

                    // Search for artist
                    if (Utilities.isOnline(getActivity())) {
                        mSearchText.clearFocus();
                        //Find artists
                        mPresenter.loadArtist(mSearchText.getQuery().toString());
                    } else{
                        updateEmptyView(getString(R.string.no_internet));
                    }


                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (s.isEmpty()) {
                    clearList();
                }
                return false;
            }
        });


        return myView;
    }

    @Override
    public ArrayList<IElement> getmListOfArtist() {
        return mListOfArtist;
    }

    @Override
    public void setmListOfArtist(ArrayList<IElement> mListOfArtist) {
        this.mListOfArtist = mListOfArtist;
    }

    @Override
    public void displayTracks(int position){
        // Get the selected artist
        ArtistModel artist = (ArtistModel) mAdapter.getItem(position);

        //Notify the activity to handle the clic event
        mListener.onArtistSelectedListener(artist);

        mPosition = position;
    }

    @Override
    public void displayArtists(ArrayList<IElement> artists) {
        if((artists!=null)&&artists.size()>0){
            mRecyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
        else{
            updateEmptyView(getString(R.string.no_artist));
        }
        mAdapter.swapElements(artists);
    }

    @Override
    public void OnItemClick(int position) {
        mPosition = position;
        mPresenter.onClickItem(position);
    }

    @Override
    public void clearList() {
        if(mAdapter!=null){
            mAdapter.swapElements(new ArrayList<IElement>());
            mListener.onArtistSelectedListener(null);
        }
        updateEmptyView(getString(R.string.empty_spotify_list));


    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState!=null) {
            if (savedInstanceState.containsKey(LIST_TAG)) {
                mListOfArtist= (ArrayList<IElement>) savedInstanceState.getSerializable(LIST_TAG);
                displayArtists(mListOfArtist);
            }
        }

        updateEmptyView(getString(R.string.empty_spotify_list));

        setRetainInstance(true);
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        if(mPosition!=ListView.INVALID_POSITION){
            outState.putInt(SELECTED_KEY,mPosition);
        }
        outState.putSerializable(LIST_TAG,mListOfArtist);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void updateEmptyView(String message){
        emptyView.setText(message);

        if(!Utilities.isOnline(getActivity())){
            emptyView.setText(getString(R.string.no_internet));
        }

        mRecyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);
    }

    @Override
    public void findArtists(String artist) {
        mTask=new ArtistAsyncTask(getActivity(),this);
        mTask.execute(artist);
    }

    public interface OnArtistSelectedListener{
        void onArtistSelectedListener(ArtistModel artist);
    }

}
