package com.depplenny.musico;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private DatabaseReference mDatabase;
    private ArrayList<Song> mSongs = new ArrayList<>();
    private SongsAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Find the toolbar view inside the activity layout
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get the database's root reference
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Write data to the database
        // databaseWrite();

        // Read data from database and initialize mSongs with data
        databaseRead();

        // Lookup the recyclerview in activity layout
        RecyclerView rvSongs = (RecyclerView) findViewById(R.id.rvSongs);
        // Create adapter and pass in the data
        mAdapter = new SongsAdapter(mSongs);
        // Attach the adapter to the recyclerview to populate items
        rvSongs.setAdapter(mAdapter);
        // Set layout manager to position the items
        rvSongs.setLayoutManager(new LinearLayoutManager(this));

    }

    // Menu icons are inflated just as they were with actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    public void databaseWrite() {
        DatabaseReference node = mDatabase.child("test");
        // Prepare the Map<key,val> to write to the node
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("key-1", "val-1");
        childUpdates.put("key-2", "val-2");
        // Delete data by setting value to null
        childUpdates.put("key-2", null);
        // Write to the node
        node.updateChildren(childUpdates);
    }

    public void databaseRead () {
        DatabaseReference node = mDatabase.child("music");
        Query query = node.orderByKey();
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot: dataSnapshot.getChildren()) {
                    String key = childSnapshot.getKey();
                    Song song = childSnapshot.getValue(Song.class);
                    song.setKey(key);
                    mSongs.add(song);
                }
                // This is very important
                Collections.reverse(mSongs);
                mAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Failed to read value
                Log.w("xz", "loadPost:onCancelled", databaseError.toException());
            }
        };
        query.addValueEventListener(listener);
    }
}