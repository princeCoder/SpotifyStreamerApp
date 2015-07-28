package com.princecoder.nanodegree.spotifytreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.princecoder.nanodegree.spotifytreamer.model.ArtistModel;
import com.princecoder.nanodegree.spotifytreamer.model.IElement;

import java.util.ArrayList;


public class HomeActivity extends AppCompatActivity implements HomeFragment.OnArtistSelectedListener,TopTrackFragment.OnTrackSelectedListener{

    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if(findViewById(R.id.trackContainer)!=null){// That means we are on two panes mode
            mTwoPane=true;
            if(savedInstanceState==null){
                // Add the fragment to the 'fragment_container' FrameLayout
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.trackContainer, new TopTrackFragment(),getString(R.string.top_track_fragment_tag)).commit();
            }
        }
        else{
            mTwoPane=false;
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onArtistSelectedListener(ArtistModel artist) {
        if(mTwoPane){  // We display the tracks of the selected artist by adding or replacing the TopTrack fragment using a fragment transaction

            TopTrackFragment fragment =(TopTrackFragment)getSupportFragmentManager().findFragmentByTag(getResources().getString(R.string.top_track_fragment_tag));
            if(artist==null){
                if(fragment!=null){
                    fragment.getAdapter().clear();
                    fragment.getAdapter().notifyDataSetChanged();
                }
            }
            else{
                Bundle args = new Bundle();
                args.putSerializable("Artist",artist);

                // Create fragment and give it an argument for the selected article
                fragment = new TopTrackFragment();
                fragment.setArguments(args);
            }


            // Replace whatever is in the fragment_container view with this fragment
            getSupportFragmentManager().beginTransaction().replace(R.id.trackContainer, fragment,getResources().getString(R.string.top_track_fragment_tag)).commit();
        }
        else{
            if(artist!=null){
                Intent intent = new Intent(this, TrackActivity.class)
                        .putExtra(Intent.EXTRA_TEXT, artist);
                startActivity(intent);
            }
        }
    }

    // This is for the tablet. because with the tablet we stay in the same activity and we just display another fragment
    @Override
    public void onTrackSelectedListener(ArrayList<IElement>list, int position) {
        Bundle args = new Bundle();
        args.putSerializable(getResources().getString(R.string.Liste_of_tracks), list);
        args.putInt("position", position);

        // Instanciate the nowPlaying fragment
        NowPlayingFragment fragment=new NowPlayingFragment();
        fragment.setArguments(args);

        //Fragment manager
        FragmentManager manager=getSupportFragmentManager();

        fragment.show(manager, "now playing");
    }
}
