package com.blazingapps.asus.atom;

public class Song {
    private long id;
    private String title;
    private String artist;
    private String albumArt;
    private String albumName;
    private long albumId;

    public Song(long id, String title, String artist, String album, long albumId) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.albumId = albumId;
        this.albumName = album;
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

    public String getAlbumName() {
        return albumName;
    }

    public long getAlbumId() {
        return albumId;
    }
}
