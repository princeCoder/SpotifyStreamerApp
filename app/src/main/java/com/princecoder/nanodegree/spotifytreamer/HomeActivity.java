package com.princecoder.nanodegree.spotifytreamer;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.princecoder.nanodegree.spotifytreamer.model.ArtistModel;
import com.princecoder.nanodegree.spotifytreamer.model.IElement;
import com.princecoder.nanodegree.spotifytreamer.model.MediaModel;
import com.princecoder.nanodegree.spotifytreamer.utils.L;

import java.util.ArrayList;


public class HomeActivity extends ActionBarActivity implements HomeFragment.OnArtistSelectedListener,TopTrackFragment.OnTrackSelectedListener{

    // boolean value to know if it is a tablet or not
    private boolean mTwoPane;

    //Top fragment tag
    public static final String TRACK_FRAGMENT_TAG="TRACK_FRAGMENT_TAG";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if(findViewById(R.id.trackContainer)!=null){// That means we are on two panes mode
            mTwoPane=true;
            if(savedInstanceState==null){
                // Add the fragment to the 'fragment_container' FrameLayout
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.trackContainer, new TopTrackFragment(),TRACK_FRAGMENT_TAG).commit();
            }
        }
        else{
            mTwoPane=false;
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        /** Inflating the current activity's menu with res/menu/items.xml */
        getMenuInflater().inflate(R.menu.menu_home, menu);

        return super.onCreateOptionsMenu(menu);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent=new Intent(this,SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     *  Check if we are online
     */
    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return (netInfo != null && netInfo.isConnectedOrConnecting());
    }


    @Override
    public void onArtistSelectedListener(ArtistModel artist) {
        if(isOnline()){
            if(mTwoPane){  // We display the tracks of the selected artist by adding or replacing the TopTrack fragment using a fragment transaction
                    TopTrackFragment fragment =(TopTrackFragment)getSupportFragmentManager().findFragmentByTag(TRACK_FRAGMENT_TAG);
                    if(artist==null){
                        if(fragment!=null){
                            fragment.getAdapter().clear();
                            fragment.getAdapter().notifyDataSetChanged();
                        }
                    }
                    else{
                        Bundle args = new Bundle();
                        args.putSerializable(TopTrackFragment.SELECTED_ARTIST,artist);

                        // Create fragment and pass the selected artist as argument
                        fragment = new TopTrackFragment();
                        fragment.setArguments(args);
                    }


                    // Replace whatever is in the fragment_container view with this fragment
                    getSupportFragmentManager().beginTransaction().replace(R.id.trackContainer, fragment,TRACK_FRAGMENT_TAG).commit();
            }
            else{// we are not on a tablet. we start a new activity
                    if(artist!=null){
                        Intent intent = new Intent(this, TrackActivity.class)
                                .putExtra(Intent.EXTRA_TEXT, artist);
                        startActivity(intent);
                    }
            }
        }
        else{
            L.toast(this,getResources().getString(R.string.no_internet));
        }

    }

    // We implement this method because the activity  has to display the now playing screen as a dialog
    @Override
    public void onTrackSelectedListener(ArrayList<IElement>list, int position) {
        Bundle args = new Bundle();


        if(isOnline()){
            // List of tracks
            args.putSerializable(NowPlayingFragment.LIST_TRACKS, list);
            // Selected track index
            args.putInt(NowPlayingFragment.TRACK_INDEX, position);

            // Instanciate the nowPlaying fragment and pass the arguments
            NowPlayingFragment fragment=new NowPlayingFragment();
            fragment.setArguments(args);

            fragment.show(getSupportFragmentManager(), "now playing");

            MediaModel model= MediaModel.getInstance();
            model.setNowPlayingTriggeredByUser(true);
        }
        else{
            L.toast(this,getResources().getString(R.string.no_internet));
        }

    }
}
