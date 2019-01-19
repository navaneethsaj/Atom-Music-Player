package com.blazingapps.asus.atom;

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
    RelativeLayout middleLayout;
    private Handler myHandler = new Handler();
    private static final String TAG = "Gestures";
    private GestureDetectorCompat mDetector;
    SeekBar volumeSeekbar;
    private AudioManager audioManager;

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
        middleLayout = findViewById(R.id.middle_layout);
        seekBar.setOnSeekBarChangeListener(this);
        volumeSeekbar = findViewById(R.id.volumeControl);
        initVolumeControls();
        mDetector = new GestureDetectorCompat(this, new MyGestureListener());
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


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.mDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final String DEBUG_TAG = "Gestures";

        // Minimal x and y axis swipe distance.
        private int MIN_SWIPE_DISTANCE_X = 100;
        private int MIN_SWIPE_DISTANCE_Y = 100;

        // Maximal x and y axis swipe distance.
        private int MAX_SWIPE_DISTANCE_X = 1000;
        private int MAX_SWIPE_DISTANCE_Y = 1000;
        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2,
                               float velocityX, float velocityY) {
            float deltaX = event1.getX() - event2.getX();

            // Get swipe delta value in y axis.
            float deltaY = event1.getY() - event2.getY();

            // Get absolute value.
            float deltaXAbs = Math.abs(deltaX);
            float deltaYAbs = Math.abs(deltaY);

            // Only when swipe distance between minimal and maximal distance value then we treat it as effective swipe
            if((deltaXAbs >= MIN_SWIPE_DISTANCE_X) && (deltaXAbs <= MAX_SWIPE_DISTANCE_X))
            {
                if(deltaX > 0)
                {
                    musicService.next();
                }else
                {
                    musicService.prev();
                }
            }
            if((deltaYAbs >= MIN_SWIPE_DISTANCE_Y))
            {
                if(deltaY < 0)
                {
                    onBackPressed();
                }
            }
            return true;
        }

    }

    private void initVolumeControls()
    {
        try
        {
            audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            volumeSeekbar.setMax(audioManager
                    .getStreamMaxVolume(AudioManager.STREAM_MUSIC)-1);
            volumeSeekbar.setProgress(audioManager
                    .getStreamVolume(AudioManager.STREAM_MUSIC));


            volumeSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
            {
                @Override
                public void onStopTrackingTouch(SeekBar arg0)
                {
                }

                @Override
                public void onStartTrackingTouch(SeekBar arg0)
                {
                }

                @Override
                public void onProgressChanged(SeekBar arg0, int progress, boolean arg2)
                {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                            progress, 0);
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)){
            volumeSeekbar.setProgress(volumeSeekbar.getProgress()-1);
        }
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_UP)){
            volumeSeekbar.setProgress(volumeSeekbar.getProgress()+1);
        }
        return super.onKeyDown(keyCode, event);
    }
}
