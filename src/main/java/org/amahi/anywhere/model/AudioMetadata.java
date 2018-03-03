/*
 * Copyright (c) 2014 Amahi
 *
 * This file is part of Amahi.
 *
 * Amahi is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Amahi is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Amahi. If not, see <http ://www.gnu.org/licenses/>.
 */

package org.amahi.anywhere.model;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;

/**
 * Represents metadata for an audio file.
 */

public class AudioMetadata {
    private String audioTitle;
    private String audioArtist;
    private String audioAlbum;
    private long duration;
    private Bitmap audioAlbumArt;

    public @Nullable
    String getAudioTitle() {
        return audioTitle;
    }

    public void setAudioTitle(String audioTitle) {
        this.audioTitle = audioTitle;
    }

    public @Nullable
    String getAudioArtist() {
        return audioArtist;
    }

    public void setAudioArtist(String audioArtist) {
        this.audioArtist = audioArtist;
    }

    public @Nullable
    String getAudioAlbum() {
        return audioAlbum;
    }

    public void setAudioAlbum(String audioAlbum) {
        this.audioAlbum = audioAlbum;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        if (duration != null) {
            this.duration = Long.valueOf(duration);
        }
    }

    public @Nullable
    Bitmap getAudioAlbumArt() {
        return audioAlbumArt;
    }

    public void setAudioAlbumArt(Bitmap audioAlbumArt) {
        this.audioAlbumArt = audioAlbumArt;
    }
}
