package com.blazingapps.asus.atom;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {

    private ArrayList<Song> songList;
    SongAdapter songAdapter;
    ListView songlistview;
    MusicService musicService;
    boolean isBound = false;
    private int startTime;
    android.support.v7.widget.Toolbar toolbar;
    DrawerLayout mDrawerLayout;
    Button playButton,nextButton,prevButton;
    TextView maxtime,currtime;
    SeekBar seekBar;
    MediaPlayer mediaPlayer;
    private Handler myHandler = new Handler();

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MyBinder binder = (MusicService.MyBinder) service;
            musicService = binder.getService();
            isBound = true;
            musicService.setSongList(songList);
            musicService.setUIElements(playButton,prevButton,nextButton);
            mediaPlayer = musicService.getPlayerInstance();
            seekBar.setProgress((int)startTime);
            myHandler.postDelayed(UpdateSongTime,100);
            Log.d("TAGZ","Connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicService = null;
            isBound = false;
            Log.d("TAGZ","Disconnected");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        songlistview = findViewById(R.id.songlistview);
        seekBar = findViewById(R.id.seekbar);
        currtime = findViewById(R.id.currTime);
        maxtime = findViewById(R.id.maxTime);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayShowTitleEnabled(false);
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        playButton = findViewById(R.id.play_pause);
        prevButton = findViewById(R.id.previous_button);
        nextButton = findViewById(R.id.next_button);

        songList = new ArrayList<Song>();
        getSongList();
        songAdapter = new SongAdapter(this, songList);
        songlistview.setAdapter(songAdapter);

        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        seekBar.setOnSeekBarChangeListener(this);
    }

    public void getSongList() {

        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);
        if(musicCursor!=null && musicCursor.moveToFirst()){
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            //add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                songList.add(new Song(thisId, thisTitle, thisArtist));
            }
            while (musicCursor.moveToNext());
        }
        Collections.sort(songList, new Comparator<Song>(){
            public int compare(Song a, Song b){
                return a.getTitle().compareTo(b.getTitle());
            }
        });
    }

    public void songPicked(View view) {

        int songId = Integer.parseInt(view.getTag().toString());
        musicService.playSong(songId);
        Log.d("TAGZ", String.valueOf(songId));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void playClicked(View v){
        musicService.play_pause();
    }
    public void prevClicked(View v){
        musicService.prev();
    }
    public void nextClicked(View v){
        musicService.next();
    }

    Runnable UpdateSongTime = new Runnable() {
        public void run() {
            startTime = mediaPlayer.getCurrentPosition();
            currtime.setText(String.format("%d : %d",
                    TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                    TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                    toMinutes((long) startTime)))
            );
            maxtime.setText(String.format("%d : %d",
                    TimeUnit.MILLISECONDS.toMinutes((long) mediaPlayer.getDuration()),
                    TimeUnit.MILLISECONDS.toSeconds((long) mediaPlayer.getDuration()) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                    toMinutes((long) mediaPlayer.getDuration())))
            );
            seekBar.setMax(mediaPlayer.getDuration());
            seekBar.setProgress(startTime);
            myHandler.postDelayed(this, 100);
        }
    };

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        musicService.seekTo(seekBar.getProgress());
    }
}
