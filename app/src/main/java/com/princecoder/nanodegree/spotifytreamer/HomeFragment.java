package com.princecoder.nanodegree.spotifytreamer;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.princecoder.nanodegree.spotifytreamer.adapter.ArtistAdapter;
import com.princecoder.nanodegree.spotifytreamer.model.ArtistModel;
import com.princecoder.nanodegree.spotifytreamer.model.IElement;
import com.princecoder.nanodegree.spotifytreamer.utils.L;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import retrofit.RetrofitError;

// TODO Do something

public class HomeFragment extends Fragment {

    // ListView to display artists
    private ListView mListViewArtist;

    // List of Artists
    private ArrayList<IElement> mListOfArtist=new ArrayList<>();

    // My adapter
    private ArtistAdapter mAdapter;

    // Log field
    private final String TAG=getClass().getSimpleName();

    // EditText for enter artist name
    private android.support.v7.widget.SearchView mSearchText;

    //Listener
    private OnArtistSelectedListener mListener;

    public HomeFragment() {
        // Required empty public constructor
    }

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
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View myView = updateUI(inflater, container);

        return myView;
    }

    @NonNull
    private View updateUI(LayoutInflater inflater, ViewGroup container) {
        // Inflate the layout for this fragment
        View myView= inflater.inflate(R.layout.fragment_home, container, false);

        mListViewArtist = (ListView) myView.findViewById(R.id.artist_listview);
        mSearchText =(android.support.v7.widget.SearchView)myView.findViewById(R.id.searchText);

        //Initialize the adapter
        mAdapter = new ArtistAdapter(getActivity(), R.layout.artist_row_item, R.id.topTxt, mListOfArtist);


        // Set the adapter
        mListViewArtist.setAdapter(mAdapter);

        mListViewArtist.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        mListViewArtist.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                                    long id) {
                // TODO Auto-generated method stub
                mListViewArtist.setItemChecked(position, true);
                ArtistModel artist = (ArtistModel) mAdapter.getItem(position);
                mListener.onArtistSelectedListener(artist);
            }
        });

        mSearchText = (android.support.v7.widget.SearchView) myView.findViewById(R.id.searchText);

        mSearchText.setIconifiedByDefault(false);
        mSearchText.setQueryHint(getResources().getString(R.string.editText_hint));

        mSearchText.setOnQueryTextListener(new android.support.v7.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                String searchKeyword = mSearchText.getQuery().toString();
                if (!searchKeyword.isEmpty()) {

                    // Search for artist
                    if (isOnline()) {
                        mSearchText.clearFocus();
                        new ArtistAsyncTask().execute(mSearchText.getQuery().toString());
                    } else
                        L.toast(getActivity(), getResources().getString(R.string.no_internet));

                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if(s.isEmpty()){
                    mListOfArtist.clear();
                    mAdapter.notifyDataSetChanged();
                }
                return false;
            }
        });
        return myView;
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mListViewArtist.setAdapter(mAdapter);
    }


    /**
     * Are we online?
     *
     * @return
     */
    protected boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Artist asyncTask
     */
    private  class ArtistAsyncTask extends AsyncTask<String,Void,List<Artist>> {

        private SpotifyApi api = new SpotifyApi();
        private SpotifyService spotify = api.getService();
        private ProgressDialog mProgressDialog;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(isOnline()) {
                mProgressDialog = ProgressDialog.show(getActivity(), getResources().getString(R.string.progress_dialog_title), getResources().getString(R.string.progress_dialog_message));
                mProgressDialog.show();
            }else{
                L.m(TAG,getResources().getString(R.string.no_internet));
                // I dismiss the progress dialog
                mProgressDialog.dismiss();
                L.toast(getActivity(),getResources().getString(R.string.no_internet));
            }
        }


        @Override
        protected List<Artist> doInBackground(String... params) {

            try {
                String searchString = params[0];
                return spotify.searchArtists(searchString).artists.items;
            } catch(RetrofitError ex){
                L.m(TAG, ex.getMessage());
            }
            return null;
        }


        @Override
        protected void onPostExecute(List<Artist> artists) {
            //I clear the list of artists
            mListOfArtist.clear();
            if(artists!=null && artists.size() > 0) {
                for (Artist ar : artists) {
                    ArtistModel artist = new ArtistModel();
                    artist.setName(ar.name);
                    artist.setSpotifyId(ar.id);
                    if(ar.images.size()>0){
                        artist.setArtThumb(ar.images.get(0).url);
                    }
                    mListOfArtist.add(artist);
                }
                if (mProgressDialog!=null)
                    mProgressDialog.dismiss();
            }
            else{
                // I dismiss the progress dialog
                if (mProgressDialog!=null)
                    mProgressDialog.dismiss();

                // I notify the user no data has been found
                L.toast(getActivity(),getResources().getString(R.string.no_artist));
            }
            //Update the adapter
            mAdapter.notifyDataSetChanged();
        }

    }

    public interface OnArtistSelectedListener{
        void onArtistSelectedListener(ArtistModel artist);
    }

}
