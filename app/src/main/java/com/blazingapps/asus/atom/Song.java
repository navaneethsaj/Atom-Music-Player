package com.blazingapps.asus.atom;

public class Song {
    private long id;
    private String title;
    private String artist;
    private String albumArt;
    private long albumId;

    public Song(long id, String title, String artist, long albumId) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.albumId = albumId;
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

    public long getAlbumId() {
        return albumId;
    }
}
