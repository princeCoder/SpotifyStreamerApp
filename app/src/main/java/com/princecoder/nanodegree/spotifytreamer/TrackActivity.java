package com.princecoder.nanodegree.spotifytreamer;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.princecoder.nanodegree.spotifytreamer.model.IElement;

import java.util.ArrayList;


public class TrackActivity extends AppCompatActivity implements TopTrackFragment.OnTrackSelectedListener{

    private final String LOG_TAG=getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);

        // Enable the up navigation
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {

        //We create the Top Track fragment and add it to the activity
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.trackContainer, new TopTrackFragment(),getString(R.string.top_track_fragment_tag))
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_track, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onTrackSelectedListener(ArrayList<IElement>list, int position) {

        Log.d(LOG_TAG," Track selected position "+position);


        Bundle args = new Bundle();
        args.putSerializable(NowPlayingFragment.LIST_TRACKS, list);
        args.putInt(NowPlayingFragment.TRACK_INDEX, position);

        //Fragment manager
        FragmentManager manager=getSupportFragmentManager();

        // The device is smaller, so show the fragment fullscreen
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        // To make it fullscreen, use the 'content' root view as the container
        // for the fragment, which is always the root view for the activity

        // Instantiate the nowPlaying fragment
        NowPlayingFragment fragment=new NowPlayingFragment();
        fragment.setArguments(args);

        transaction.add(R.id.trackContainer, fragment)
                .addToBackStack(null).commit();
    }
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public Intent getParentActivityIntent() {
        return super.getParentActivityIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }
}
