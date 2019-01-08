package com.blazingapps.asus.atom;

public class Song {
    private long id;
    private String title;
    private String artist;
    private String albumArt;

    public Song(long id, String title, String artist, String thisArt) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.albumArt = thisArt;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbumArt() {
        return albumArt;
    }
}
