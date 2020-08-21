package com.depplenny.musico;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

// Create the basic adapter extending from RecyclerView.Adapter
// Note that we specify the custom ViewHolder which gives us access to our views
public class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.ViewHolder> {

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public TextView nameTextView;
        public TextView timestampTextView;
        public Button urlButton;

        // We also create a constructor that accepts one item in the list (itemView)
        // and does the view lookups to find each subview
        public ViewHolder(final Context context, View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            nameTextView = itemView.findViewById(R.id.song_name);
            timestampTextView = itemView.findViewById(R.id.timestamp);
            urlButton =  itemView.findViewById(R.id.url_button);

            /*
            // Attach a click listener to the entire row view
            itemView.setOnClickListener(new View.OnClickListener() {
                // Handles the subview being being clicked
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition(); // gets item position
                    if (position != RecyclerView.NO_POSITION) { // Check if an item was deleted, but the user clicked it before the UI removed it
                        Song song = mSongs.get(position);
                        // We can access the data within the views
                        Toast.makeText(context, nameTextView.getText(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
            */

            // Attach a click listener to the subview urlButton
            urlButton.setOnClickListener(new View.OnClickListener() {
                // Handles the subview being being clicked
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition(); // gets item position
                    if (position != RecyclerView.NO_POSITION) { // Check if an item was deleted, but the user clicked it before the UI removed it
                        Song song = mSongs.get(position);
                        // We can access the data within the views
                        // Toast.makeText(context, urlButton.getText(), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(context, PlayerActivity.class);
                        intent.putExtra("key",song.getKey());
                        intent.putExtra("name", song.getName());
                        intent.putExtra("url",song.getUrl());
                        intent.putExtra("like", song.getLike());
                        intent.putExtra("dislike", song.getDislike());
                        context.startActivity(intent);
                        //context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(song.getVal())));
                    }
                }
            });

        }

    }

    // Store a member variable for the songs
    private List<Song> mSongs;


    // Pass in the songs array into the {constructor}
    public SongsAdapter(List<Song> songs) {
        mSongs = songs;
    }

    // Usually involves inflating a layout from XML and returning the holder
    @NonNull
    @Override
    public SongsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom item layout
        View songView = inflater.inflate(R.layout.item_song, parent, false);

        // Return a new holder instance
        return new ViewHolder(context, songView);
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(SongsAdapter.ViewHolder viewHolder, int position) {
        // Get the data model based on position
        Song song = mSongs.get(position);

        // Set item views based on your views and data model
        viewHolder.nameTextView.setText(song.getName());
        viewHolder.timestampTextView.setText(song.getTimestamp());
        viewHolder.urlButton.setText("Play");

    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return mSongs.size();
    }

}

