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

package org.amahi.anywhere.amahitv.bus;

import android.graphics.Bitmap;

public class AudioMetadataRetrievedEvent implements BusEvent {
    private final String audioTitle;
    private final String audioArtist;
    private final String audioAlbum;
    private final Bitmap audioAlbumArt;

    public AudioMetadataRetrievedEvent(String audioTitle, String audioArtist, String audioAlbum, Bitmap audioAlbumArt) {
        this.audioTitle = audioTitle;
        this.audioArtist = audioArtist;
        this.audioAlbum = audioAlbum;
        this.audioAlbumArt = audioAlbumArt;
    }

    public String getAudioTitle() {
        return audioTitle;
    }

    public String getAudioArtist() {
        return audioArtist;
    }

    public String getAudioAlbum() {
        return audioAlbum;
    }

    public Bitmap getAudioAlbumArt() {
        return audioAlbumArt;
    }
}
