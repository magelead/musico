package com.depplenny.musico;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class PlayerActivity extends AppCompatActivity {
    private DatabaseReference mDatabase;
    // Debug tag.
    private static final String TAG = "PlayerActivity";
    // Fields about media player.
    private PlayerControlView mPlayerControlView;
    private PlaybackStateListener playbackStateListener;
    private SimpleExoPlayer mPlayer;
    private Long mLike;
    private Long mDislike;

    /**
     * Callback triggered when the activity is first created.
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        // Retrieve intent
        Intent intent = getIntent();
        String key = intent.getStringExtra("key");
        String name = intent.getStringExtra("name");
        name = name.replaceAll("\\(.*\\) | wav","");
        Uri url = Uri.parse(intent.getStringExtra("url"));
        // Find the toolbar view inside the activity layout
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        // Remove default title text
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        // Get access to the custom title view
        TextView title = findViewById(R.id.toolbar_title);
        title.setText(name);

        // Set up player
        mPlayerControlView = findViewById(R.id.playerControlView);
        playbackStateListener = new PlaybackStateListener();
        initializePlayer(url);

        // Get the database's root reference
        mDatabase = FirebaseDatabase.getInstance().getReference();
        databaseRead(key);
        initializeButtons(key);
    }

    /**
     * Callback triggered when the activity is destroyed.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();
    }

    // Menu icons are inflated just as they were with actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }


    /**
     * Helper method that initializes SimpleExoPlayer.
     *
     * @param uri The URI of the sample to play.
     */
    private void initializePlayer(Uri uri) {
        if (mPlayer == null) {
            // Create a meadia source from Uri
            MediaSource mediaSource = buildMediaSource(uri);
            // Create a SimpleExoPlayer
            mPlayer = new SimpleExoPlayer.Builder(this).build();
            // Add PlaybackStateListener to the player
            mPlayer.addListener(playbackStateListener);
            // Prepare the SimpleExoPlayer
            mPlayer.prepare(mediaSource);
            // Play media source
            mPlayer.setPlayWhenReady(true);
            // Bind it to PlayerControlView
            mPlayerControlView.setPlayer(mPlayer);
        }
    }

    /**
     * Helper method to build MediaSource.
     *
     * @param uri The URI of the sample to play.
     */
    private MediaSource buildMediaSource(Uri uri) {
        // Create a media source factory
        DataSource.Factory dataSourceFactory =
                new DefaultDataSourceFactory(this, Util.getUserAgent(this, "musico"));
        ProgressiveMediaSource.Factory progressiveMediaSourceFactory =
                new ProgressiveMediaSource.Factory(dataSourceFactory);
        // Create a media source using the supplied URI
        MediaSource mediaSource = progressiveMediaSourceFactory.createMediaSource(uri);
        return mediaSource;
    }

    /**
     * Helper method to release SimpleExoPlayer.
     */
    private void releasePlayer() {
        if (mPlayer != null) {
            mPlayer.removeListener(playbackStateListener);
            mPlayer.release();
            mPlayer = null;
        }
    }

    /**
     * Add listeners to node like and dislike of the song
     * @param key
     */
    public void databaseRead (String key) {
        DatabaseReference likeRef = mDatabase.child("music").child(key).child("like");
        DatabaseReference dislikeRef = mDatabase.child("music").child(key).child("dislike");

        likeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                mLike = (Long) snapshot.getValue();
                Log.d("xz", mLike.toString());
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Failed to read value
                Log.w("xz", "loadPost:onCancelled", databaseError.toException());
            }
        });

        dislikeRef.addValueEventListener(new ValueEventListener(){
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mDislike = (Long) snapshot.getValue();
                Log.d("xz", mDislike.toString());
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Failed to read value
                Log.w("xz", "loadPost:onCancelled", databaseError.toException());
            }

        });
    }


    /**
     * Helper method to initialize buttons
     */
    private void initializeButtons(String key) {

        Button buttonLike = findViewById(R.id.buttonLike);

        buttonLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatabase.child("music").child(key).child("like").setValue(mLike+1);
                buttonLike.setText("Like "+(mLike+1));
            }
        });

        Button buttonDislike = findViewById(R.id.buttonDislike);
       
        buttonDislike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatabase.child("music").child(key).child("dislike").setValue(mDislike+1);
                buttonDislike.setText("Dislike "+(mDislike+1));
            }
        });

    }









    /**
     * Inner class that implements Player.EventListener.
     */
    class PlaybackStateListener implements Player.EventListener {
        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            String stateString;
            switch (playbackState) {
                case ExoPlayer.STATE_IDLE:
                    stateString = "ExoPlayer.STATE_IDLE      -";
                    break;
                case ExoPlayer.STATE_BUFFERING:
                    stateString = "ExoPlayer.STATE_BUFFERING -";
                    break;
                case ExoPlayer.STATE_READY:
                    stateString = "ExoPlayer.STATE_READY     -";
                    break;
                case ExoPlayer.STATE_ENDED:
                    stateString = "ExoPlayer.STATE_ENDED     -";
                    break;
                default:
                    stateString = "UNKNOWN_STATE             -";
                    break;
            }
            Log.d(TAG, "changed state to " + stateString + " playWhenReady: " + playWhenReady);
        }
    }






}
