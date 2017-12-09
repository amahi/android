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

package org.amahi.anywhere.bus;

import android.graphics.Bitmap;

import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.tv.presenter.MainTVPresenter;

public class AudioMetadataRetrievedEvent implements BusEvent {
    private final String audioTitle;
    private final String audioArtist;
    private final String audioAlbum;
    private final long duration;
    private final Bitmap audioAlbumArt;
    private final MainTVPresenter.ViewHolder viewHolder;
    private final ServerFile serverFile;

    public AudioMetadataRetrievedEvent(String audioTitle, String audioArtist, String audioAlbum,
                                       String duration, Bitmap audioAlbumArt,
                                       MainTVPresenter.ViewHolder viewHolder,
                                       ServerFile serverFile) {
        this.audioTitle = audioTitle;
        this.audioArtist = audioArtist;
        this.audioAlbum = audioAlbum;
        if (duration != null) {
            this.duration = Long.valueOf(duration);
        } else {
            this.duration = 0;
        }
        this.audioAlbumArt = audioAlbumArt;
        this.viewHolder = viewHolder;
        this.serverFile = serverFile;
    }

    public AudioMetadataRetrievedEvent(MainTVPresenter.ViewHolder viewHolder, ServerFile serverFile) {
        this(null, null, null, null, null, viewHolder, serverFile);
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

    public MainTVPresenter.ViewHolder getViewHolder() {
        return viewHolder;
    }

    public ServerFile getServerFile() {
        return serverFile;
    }

    public long getDuration() {
        return duration;
    }
}
