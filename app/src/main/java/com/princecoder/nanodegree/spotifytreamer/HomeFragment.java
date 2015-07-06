package com.princecoder.nanodegree.spotifytreamer;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.princecoder.nanodegree.spotifytreamer.adapter.ArtistAdapter;
import com.princecoder.nanodegree.spotifytreamer.model.ArtistModel;
import com.princecoder.nanodegree.spotifytreamer.model.IElement;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;


public class HomeFragment extends Fragment {

    // ListView to display artists
    private ListView mListViewArtist;

    // List of Artists
    private ArrayList<IElement> mListOfArtist=new ArrayList<>();

    // My adapter
    private ArtistAdapter mAdapter;

    // Log field
    private final String TAG=getClass().getSimpleName();

    private ProgressDialog mProgressDialog;

    private EditText mEditText;

    private String mEdtValue="";



    public HomeFragment() {
        // Required empty public constructor
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(savedInstanceState!=null){
            String userText =
                    savedInstanceState.getString("savedText");

            mEditText.setText(userText);
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        String userText = mEditText.getText().toString();
        outState.putString("savedText", userText);
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mListViewArtist.setAdapter(mAdapter);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myView= inflater.inflate(R.layout.fragment_home, container, false);

        mListViewArtist = (ListView) myView.findViewById(R.id.artist_listview);
        mEditText=(EditText)myView.findViewById(R.id.edtArtist);

        //Initialize the adapter
        mAdapter = new ArtistAdapter(getActivity(), R.layout.artist_row_item, R.id.topTxt, mListOfArtist);


        // Set the adapter
        mListViewArtist.setAdapter(mAdapter);
        mListViewArtist.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                                    long id) {
                // TODO Auto-generated method stub

                ArtistModel artist = (ArtistModel) mAdapter.getItem(position);

                Intent intent = new Intent(getActivity(), TrackActivity.class)
                        .putExtra(Intent.EXTRA_TEXT, artist);
                startActivity(intent);

            }
        });


        mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                String art = v.getText().toString();
                if (!art.isEmpty()) {
                    // Search for artist
                    new ArtistAsyncTask().execute(mEditText.getText().toString());
                }
                return false;
            }
        });
        mEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    ((EditText) v).setText("");
                }
            }
        });
        return myView;
    }




    private  class ArtistAsyncTask extends AsyncTask<String,Void,ArtistsPager> {

        private SpotifyApi api = new SpotifyApi();
        private SpotifyService spotify = api.getService();
        private ArtistsPager myArtistPager;
        private ProgressDialog mProgressDialog;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = ProgressDialog.show(getActivity(), "Working", "Please wait !!!");
            mProgressDialog.show();
        }


        @Override
        protected ArtistsPager doInBackground(String... params) {

            String searchString = params[0];
            return spotify.searchArtists(searchString);
        }


        @Override
        protected void onPostExecute(ArtistsPager artistPager) {

            myArtistPager = artistPager;
            List<Artist> artistSearchResult = artistPager.artists.items;
            Log.i("Artist", String.valueOf(artistSearchResult.size()));

            //I clear the list of artists
            mListOfArtist.clear();
            if(artistSearchResult.size() > 0) {
                for (Artist ar : artistSearchResult) {
                    ArtistModel artist = new ArtistModel();
                    artist.setName(ar.name);
                    artist.setSpotifyId(ar.id);
                    if(ar.images.size()>0){
                        artist.setArtThumb(ar.images.get(0).url);
                    }
                    mListOfArtist.add(artist);
                }
                mProgressDialog.dismiss();
                mAdapter.notifyDataSetChanged();
            }
            else{
                // To be done later
                mProgressDialog.dismiss();
            }
        }

    }

}
