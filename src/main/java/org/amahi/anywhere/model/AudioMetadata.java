package org.amahi.anywhere.model;

import android.graphics.Bitmap;

import java.io.Serializable;

/**
 * Represents metadata for an audio file.
 */

public class AudioMetadata implements Serializable {
    private String audioTitle;
    private String audioArtist;
    private String audioAlbum;
    private long duration;
    private Bitmap audioAlbumArt;

    public String getAudioTitle() {
        return audioTitle;
    }

    public void setAudioTitle(String audioTitle) {
        this.audioTitle = audioTitle;
    }

    public String getAudioArtist() {
        return audioArtist;
    }

    public void setAudioArtist(String audioArtist) {
        this.audioArtist = audioArtist;
    }

    public String getAudioAlbum() {
        return audioAlbum;
    }

    public void setAudioAlbum(String audioAlbum) {
        this.audioAlbum = audioAlbum;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public Bitmap getAudioAlbumArt() {
        return audioAlbumArt;
    }

    public void setAudioAlbumArt(Bitmap audioAlbumArt) {
        this.audioAlbumArt = audioAlbumArt;
    }
}
