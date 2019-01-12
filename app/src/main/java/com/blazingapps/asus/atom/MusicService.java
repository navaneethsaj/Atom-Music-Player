package com.blazingapps.asus.atom;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Random;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {
    private static final String CHANNEL_ID = "notificationChannel";
    private static final String NOTIFICATION_CNST = "noticonst";
    private ArrayList<Song> songList;
    MediaPlayer mediaPlayer ;
    private IBinder myBinder = new MyBinder();
    int songId;
    private Button playPauseButton,nextButton,prevButton,fullplaybtn,fullprevbtn,fullnextbtn;
    private ImageView albumArt, fullPlayerArt;
    private TextView artistTextView,titleTextView;
    private boolean shuffle = false;
    private Random rand;
    private boolean isJustStarted=true;
    private int lastSongId =5;
    private int NOTIFICATION_ID =99;
    PhoneStateListener phoneStateListener;

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
        rand = new Random();
        songId= lastSongId;
        initCallStateListner();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        destroyCallStateListner();
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
        if (mediaPlayer.isPlaying()){
            playPauseButton.setText("Pause");
        }else {
            playPauseButton.setText("Play");
        }
        updateUIFullPlayer();
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if (mediaPlayer.getCurrentPosition()>0 && !isJustStarted){
            mediaPlayer.reset();
            next();
            //Log.d("TAGZ", String.valueOf(mediaPlayer.getCurrentPosition()));
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
        setFullScrnAlbumArt();
        isJustStarted = false;
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
        }finally {
            initNotification();
        }
    }

    public void play_pause(){
        if (isJustStarted==true){
            songId= lastSongId;
            playSong(songId);
        }
        if (mediaPlayer.isPlaying()){
            mediaPlayer.pause();
            playPauseButton.setText("Play");
            if (fullplaybtn!=null){
                fullplaybtn.setText("Play\n"+songList.get(songId).getTitle());
            }
        }else {
            mediaPlayer.start();
            playPauseButton.setText("Pause");
            if (fullplaybtn!=null){
                fullplaybtn.setText("Pause\n"+songList.get(songId).getTitle());
            }
        }
    }

    public void next(){
        if(shuffle){
            int newSong = songId;
            while(newSong==songId){
                newSong=rand.nextInt(songList.size());
            }
            songId=newSong;
        }
        else{
            songId++;
            if(songId>=songList.size()) songId=0;
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

    public void setShuffle(){
        shuffle=!shuffle;
    }
    public boolean getShuffle(){
        return shuffle;
    }

    public long getalBumId(){
        return songList.get(songId).getAlbumId();
    }

    public void setFullPlayerArt(ImageView fullPlayerArt) {
        this.fullPlayerArt = fullPlayerArt;
    }


    private void setFullScrnAlbumArt() {
        if (fullPlayerArt!=null) {
            Uri sArtworkUri = Uri
                    .parse("content://media/external/audio/albumart");
            Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, getalBumId());
            Picasso.get().load(albumArtUri).placeholder(R.drawable.ic_music_note_blue_24dp).error(R.drawable.ic_music_note_red_24dp).into(fullPlayerArt);
        }
    }

    public void setUIFullScrn(Button prev,Button play,Button next){
        this.fullnextbtn=next;
        this.fullplaybtn=play;
        this.fullprevbtn=prev;
    }

    public void updateUIFullPlayer(){
        if (fullprevbtn!=null&fullnextbtn!=null&&fullplaybtn!=null){
            int pre,nxt;
            pre=songId-1;
            nxt=songId+1;
            if (pre<0){
                pre=songList.size()-1;
            }
            if (nxt>=songList.size()){
                nxt=0;
            }
            fullprevbtn.setText("PREV"+"\n"+songList.get(pre).getTitle());
            fullnextbtn.setText("NEXT\n"+songList.get(nxt).getTitle());
        }
        if (fullplaybtn!=null) {
            if (mediaPlayer.isPlaying()) {
                fullplaybtn.setText("Pause\n" + songList.get(songId).getTitle());
            }else {
                fullplaybtn.setText("Play\n" + songList.get(songId).getTitle());
            }
        }
    }

    public void initLastSong(){
        titleTextView.setText(songList.get(songId).getTitle());
        artistTextView.setText(songList.get(songId).getArtist());
    }

    public void initNotification(){

        final Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent pendInt = PendingIntent.getActivity(this, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(this);

        builder.setContentIntent(pendInt)
                .setSmallIcon(R.drawable.ic_music_note_blue_24dp)
                .setOngoing(true)
                .setContentTitle(songList.get(songId).getTitle())
                .setContentText(songList.get(songId).getArtist());
        Notification not = builder.build();

        startForeground(NOTIFICATION_ID, not);
// NOTIFICATION_ID is a unique int for each notification that you must define
        //notificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    public void initCallStateListner(){
        PhoneStateListener phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                if (state == TelephonyManager.CALL_STATE_RINGING) {
                    if (mediaPlayer.isPlaying()){
                        play_pause();
                    }
                    //Incoming call: Pause music
                } else if(state == TelephonyManager.CALL_STATE_IDLE) {
                    //Not in call: Play music
                } else if(state == TelephonyManager.CALL_STATE_OFFHOOK) {
                    //A call is dialing, active or on hold
                    if (mediaPlayer.isPlaying()){
                        play_pause();
                    }
                }
                super.onCallStateChanged(state, incomingNumber);
            }
        };
        this.phoneStateListener = phoneStateListener;
        TelephonyManager mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if(mgr != null) {
            mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }
    }

    public void destroyCallStateListner(){
        TelephonyManager mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if(mgr != null) {
            mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
    }
}
