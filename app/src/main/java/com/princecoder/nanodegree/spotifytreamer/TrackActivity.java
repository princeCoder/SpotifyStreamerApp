package com.princecoder.nanodegree.spotifytreamer;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;


public class TrackActivity extends ActionBarActivity implements TopTrackFragment.OnTrackSelectedListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.trackContainer, new TopTrackFragment(),"TopTrackFragment")
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
    public void onTrackSelectedListener() {
        // Instanciate the nowPlaying fragment
        NowPlayingFragment fragment=new NowPlayingFragment();

        //Fragment manager
        FragmentManager manager=getSupportFragmentManager();

//                Fragment transaction
        FragmentTransaction transaction=manager.beginTransaction();
        transaction.add(R.id.trackContainer, fragment, getResources().getString(R.string.now_playing_fragment_tag));

//                add to backstack
        transaction.addToBackStack(getResources().getString(R.string.now_playing_fragment_tag));
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
        transaction.commit();

    }
}
