package com.example.music11;

import android.graphics.Bitmap;

import java.io.Serializable;

public class Audio implements Serializable {

    private String data;

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    private int duration;
    private String title;
    private String album;
    private String artist;
    private Bitmap coverpic;

    public Bitmap getCoverpic() {
        return coverpic;
    }

    public void setCoverpic(Bitmap coverpic) {
        this.coverpic = coverpic;
    }



    public Audio(String data, String title, String album, String artist,Bitmap coverpic,int duration) {
        this.data = data;
        this.title = title;
        this.album = album;
        this.artist = artist;
        this.coverpic = coverpic;
        this.duration= duration;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }
}

