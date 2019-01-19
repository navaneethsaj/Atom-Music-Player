package com.blazingapps.asus.atom;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class SongAdapter extends BaseAdapter {


    private ArrayList<Song> songs;
    private ArrayList<Song> originalList = new ArrayList<>();
    private LayoutInflater songInf;
    Context context;

    public SongAdapter(Context c, ArrayList<Song> theSongs){
        songs = theSongs;
        this.context = c;
        originalList.addAll(theSongs);
        songInf=LayoutInflater.from(c);
    }


    public int getCount() {
        return songs.size();
    }

    @Override
    public Object getItem(int arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        LinearLayout songLay = (LinearLayout)songInf.inflate
                (R.layout.song, viewGroup, false);
        //get title and artist views
        TextView songView = (TextView)songLay.findViewById(R.id.song_title);
        TextView artistView = (TextView)songLay.findViewById(R.id.song_artist);
        ImageView albumart = songLay.findViewById(R.id.albumartimageview);
        //get song using position
        Song currSong = songs.get(position);
        //get title and artist strings
        songView.setText(currSong.getTitle());
        artistView.setText(currSong.getArtist()+String.valueOf(currSong.getAlbumId()));


        Uri sArtworkUri = Uri
                .parse("content://media/external/audio/albumart");
        Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, currSong.getAlbumId());

        Picasso.get().load(albumArtUri).placeholder(R.drawable.ic_music_note_blue_24dp).error(R.drawable.ic_music_note_red_24dp).into(albumart);
        //set position as tag
        songLay.setTag(position);
        return songLay;
    }

    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        songs.clear();
        if (charText.length() == 0) {
            songs.addAll(originalList);
        }
        else
        {
            for (Song sp : originalList)
            {
                if (sp.getTitle().toLowerCase(Locale.getDefault()).contains(charText))
                {
                    songs.add(sp);
                }
                else if (sp.getArtist().toLowerCase(Locale.getDefault()).contains(charText))
                {
                    songs.add(sp);
                }
                else if (sp.getAlbumName().toLowerCase(Locale.getDefault()).contains(charText))
                {
                    songs.add(sp);
                }
            }
        }
        notifyDataSetChanged();
    }
}