package com.princecoder.nanodegree.spotifytreamer;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.princecoder.nanodegree.spotifytreamer.adapter.TrackAdapter;
import com.princecoder.nanodegree.spotifytreamer.model.ArtistModel;
import com.princecoder.nanodegree.spotifytreamer.model.IElement;
import com.princecoder.nanodegree.spotifytreamer.model.TrackModel;
import com.princecoder.nanodegree.spotifytreamer.utils.L;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.RetrofitError;

/**
 * @author prinzlyngotoum
 */
public class TopTrackFragment extends Fragment {

    // The ListView
    private ListView mTrackListView;

    // List of Artists
    private ArrayList<IElement> mTraks=new ArrayList<>();

    // My adapter
    private TrackAdapter mAdapter;

    private String TAG=getClass().getSimpleName();

    public TopTrackFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mTrackListView=(ListView)getView().findViewById(R.id.track_listview);
        mAdapter=new TrackAdapter(getActivity(),R.layout.track_row_item,R.id.topTxt,mTraks);

        mTrackListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Instanciate the nowPlaying fragment
                NowPlayingFragment fragment=new NowPlayingFragment();

                //Fragment manager
                FragmentManager manager=getActivity().getSupportFragmentManager();

//                Fragment transaction
                FragmentTransaction transaction=manager.beginTransaction();
                transaction.add(R.id.trackContainer, fragment, getResources().getString(R.string.now_playing_fragment_tag));

//                add to backstack
                transaction.addToBackStack(getResources().getString(R.string.now_playing_fragment_tag));
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
                transaction.commit();
            }
        });



        // Set the adapter
        mTrackListView.setAdapter(mAdapter);

        Intent intent = getActivity().getIntent();

        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
            ArtistModel artist = (ArtistModel)intent.getSerializableExtra(Intent.EXTRA_TEXT);
            new TopTrackAsyncTask().execute(artist.getSpotifyId());
        }

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_top_track, container, false);
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
     * Top track asyncTask
     */
    private class TopTrackAsyncTask extends AsyncTask<String ,Void,Tracks> {

        private SpotifyApi mSpotifyApi = new SpotifyApi();
        private SpotifyService mSpotifyService = mSpotifyApi.getService();
        private ProgressDialog mProgressDialog;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if(isOnline()) {
                mProgressDialog = new ProgressDialog(getActivity());
                mProgressDialog.setTitle(getResources().getString(R.string.progress_dialog_message));
                mProgressDialog.show();
            }else{
                L.m(TAG,getResources().getString(R.string.no_internet));
                // I dismiss the progress dialog
                mProgressDialog.dismiss();
                L.toast(getActivity(),getResources().getString(R.string.no_internet));
            }
        }

        @Override
        protected Tracks doInBackground(String... params) {
            HashMap<String,Object> queryString = new HashMap<>();
            try{
                queryString.put(SpotifyService.COUNTRY, Locale.getDefault().getCountry());
                return  mSpotifyService.getArtistTopTrack(params[0], queryString);
            }
            catch (RetrofitError error){
                L.m(TAG,error.getMessage());
            }
            return null;
        }


        @Override
        protected void onPostExecute(Tracks tracks) {
            mTraks.clear();
            if (tracks == null || tracks.tracks.size() == 0) {
                mProgressDialog.dismiss();
                L.toast(getActivity(),getResources().getString(R.string.no_track));
            }
            else{
                int count =0;
                for (Track track : tracks.tracks) {
                    TrackModel t = new TrackModel();
                    t.setTrackName(track.name);
                    t.setPrevUrl(track.preview_url);
                    t.setAlbum(track.album.name);
                    if(track.album.images!=null && track.album.images.size()>0){
                        t.setAlbThumb(track.album.images.get(0).url);
                    }
                    mTraks.add(t);
                    count++;
                    if(count==10)break;;
                }
                // dismiss the progress dialog
                if (mProgressDialog!=null)
                    mProgressDialog.dismiss();

                //update the adapter dataset
                mAdapter.notifyDataSetChanged();
            }
        }
    }

}
