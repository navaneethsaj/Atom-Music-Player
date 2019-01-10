package com.blazingapps.asus.atom;

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;


import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;


public class FullPlayerActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener{

    private ArrayList<Song> songList;
    MusicService musicService;
    boolean isBound = false;
    ImageView albumArt;
    android.support.v7.widget.Toolbar toolbar;
    Button playButton,nextButton,prevButton;
    ImageView albumArtImageView;
    TextView titleTextview,artistTextview;
    int startTime=0;
    TextView currtime,maxtime;
    SeekBar seekBar;
    MediaPlayer mediaPlayer;
    private Handler myHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_player);

        currtime = findViewById(R.id.playtime);
        maxtime = findViewById(R.id.tot_time);
        seekBar = findViewById(R.id.seek_bar_full);
        prevButton = findViewById(R.id.previous_fullplayer);
        nextButton = findViewById(R.id.next_fullplayer);
        playButton = findViewById(R.id.play_pause_fullplayer);
        albumArt = findViewById(R.id.albumartFullplayer);
        seekBar.setOnSeekBarChangeListener(this);

        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, serviceConnection, Context.BIND_ABOVE_CLIENT);

    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MyBinder binder = (MusicService.MyBinder) service;
            musicService = binder.getService();
            isBound = true;
            mediaPlayer = musicService.getPlayerInstance();
            myHandler.postDelayed(UpdateSongTime,100);

            musicService.setFullPlayerArt(albumArt);
            musicService.setUIFullScrn(prevButton,playButton,nextButton);
            musicService.updateUIFullPlayer();
            setalbumArt();

            Log.d("TAGZ","Connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicService = null;
            isBound = false;
            Log.d("TAGZ","Disconnected");
        }
    };

    private void setalbumArt() {
        Uri sArtworkUri = Uri
                .parse("content://media/external/audio/albumart");
        Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, musicService.getalBumId());
        Picasso.get().load(albumArtUri).placeholder(R.drawable.ic_music_note_blue_24dp).error(R.drawable.ic_music_note_red_24dp).into(albumArt);

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

    public void playClicked(View v){
        musicService.play_pause();
    }
    public void prevClicked(View v){
        musicService.prev();
    }
    public void nextClicked(View v){
        musicService.next();
    }

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
