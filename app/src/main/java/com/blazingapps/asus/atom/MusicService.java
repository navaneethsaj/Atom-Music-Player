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
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {
    private ArrayList<Song> songList;
    MediaPlayer mediaPlayer ;
    private IBinder myBinder = new MyBinder();
    int songId;
    private Button playPauseButton,nextButton,prevButton;
    private ImageView albumArt;
    private TextView artistTextView,titleTextView;

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

        mediaPlayer.setOnCompletionListener(this);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void setUIElements(Button playButton, Button prevButton, Button nextButton, ImageView albumArt, TextView titleTextView, TextView artistTextView) {
        this.playPauseButton = playButton;
        this.nextButton = nextButton;
        this.prevButton = prevButton;
        this.albumArt = albumArt;
        this.titleTextView = titleTextView;
        this.artistTextView = artistTextView;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        //Log.d("TAGZ","Z");
        mediaPlayer.start();
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if (mediaPlayer.getCurrentPosition()>0){
            Log.d("TAGZ", String.valueOf(mediaPlayer.getCurrentPosition()));
        }
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
        //Log.d("TAGZ","P");
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
            Uri sArtworkUri = Uri
                    .parse("content://media/external/audio/albumart");
            Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, playSong.getAlbumId());
            Picasso.get().load(albumArtUri).placeholder(R.drawable.ic_music_note_blue_24dp).error(R.drawable.ic_music_note_red_24dp).into(albumArt);
            titleTextView.setText(playSong.getTitle());
            artistTextView.setText(playSong.getArtist());
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
