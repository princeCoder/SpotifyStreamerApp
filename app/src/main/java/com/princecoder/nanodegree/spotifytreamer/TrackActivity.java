package com.princecoder.nanodegree.spotifytreamer;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.princecoder.nanodegree.spotifytreamer.model.ArtistModel;
import com.princecoder.nanodegree.spotifytreamer.model.IElement;
import com.princecoder.nanodegree.spotifytreamer.model.MediaModel;
import com.princecoder.nanodegree.spotifytreamer.utils.L;

import java.util.ArrayList;


public class TrackActivity extends AppCompatActivity implements TopTrackFragment.OnTrackSelectedListener{

    private final String LOG_TAG=getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);

        if (savedInstanceState == null) {

        //We create the Top Track fragment and add it to the activity
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.trackContainer, new TopTrackFragment(),HomeActivity.TRACK_FRAGMENT_TAG)
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        
        if (id == R.id.action_settings) {
            Intent intent=new Intent(this,SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ArtistModel artist=(ArtistModel)getIntent().getSerializableExtra(Intent.EXTRA_TEXT);


        //Set the title of the actionBar
        getSupportActionBar().setTitle("Top tracks");
        if(artist!=null){
            getSupportActionBar().setSubtitle(artist.getName());
        }
    }

    @Override
    public void onTrackSelectedListener(ArrayList<IElement>list, int position) {
        Bundle args = new Bundle();
        args.putSerializable(NowPlayingFragment.LIST_TRACKS, list);
        args.putInt(NowPlayingFragment.TRACK_INDEX, position);

        // Instantiate the nowPlaying fragment
        NowPlayingFragment fragment=new NowPlayingFragment();
        if(isOnline()){ // Make sure we start playing if we have internet
            fragment.setArguments(args);
            fragment.show(getSupportFragmentManager(), "now playing");

            MediaModel model= MediaModel.getInstance();
            model.setNowPlayingTriggeredByUser(true);
        }
        else {
            L.toast(this,getResources().getString(R.string.no_internet));
        }
    }
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public Intent getParentActivityIntent() {
        return super.getParentActivityIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }

    /**
     *  Check if we are online
     */
    protected boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return (netInfo != null && netInfo.isConnectedOrConnecting());
    }
}
