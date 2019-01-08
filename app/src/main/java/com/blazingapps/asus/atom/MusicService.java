package com.blazingapps.asus.atom;

import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Button;

import java.util.ArrayList;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener {
    private ArrayList<Song> songList;
    MediaPlayer mediaPlayer ;
    private IBinder myBinder = new MyBinder();
    int songId;
    private Button playPauseButton,nextButton,prevButton;

    public MusicService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void setUIElements(Button playButton, Button prevButton, Button nextButton) {
        this.playPauseButton = playButton;
        this.nextButton = nextButton;
        this.prevButton = prevButton;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {

        mediaPlayer.start();
    }


    public class MyBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    public void setSongList(ArrayList<Song> songList) {
        this.songList = songList;
    }

    public void playSong(int songId){
        mediaPlayer.reset();
        this.songId = songId;
        Song playSong = songList.get(songId);
        long currSong = playSong.getId();
        Uri trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currSong);
        try{
            mediaPlayer.setDataSource(getApplicationContext(), trackUri);
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.prepareAsync();
        }
        catch(Exception e){
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }

    }

    public void play_pause(){
        if (mediaPlayer.isPlaying()){
            mediaPlayer.pause();
            playPauseButton.setText("Play");
        }else {
            mediaPlayer.start();
            playPauseButton.setText("Pause");
        }
    }

    public void next(){
        songId++;
        if (songId>=songList.size()){
            songId=0;
        }
        playSong(songId);
    }

    public void prev(){
        songId--;
        if (songId<0){
            songId=songList.size()-1;
        }
        playSong(songId);
    }

    public MediaPlayer getPlayerInstance(){
        return mediaPlayer;
    }

    public void seekTo(int s){
        mediaPlayer.seekTo(s);
    }
}
