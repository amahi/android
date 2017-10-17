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

package org.amahi.anywhere.task;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;

import org.amahi.anywhere.bus.AudioMetadataRetrievedEvent;
import org.amahi.anywhere.bus.BusEvent;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.tv.presenter.MainTVPresenter;

import java.util.HashMap;

/**
 * Async wrapper for audio metadata retrieving.
 * The retrieving itself is done via {@link android.media.MediaMetadataRetriever}.
 */
public class AudioMetadataRetrievingTask extends AsyncTask<Void, Void, BusEvent> {
    private final Uri audioUri;
    private final MainTVPresenter.ViewHolder viewHolder;
    private final ServerFile serverFile;

    private AudioMetadataRetrievingTask(Uri audioUri, ServerFile serverFile) {
        this.audioUri = audioUri;
        this.viewHolder = null;
        this.serverFile = serverFile;
    }

    private AudioMetadataRetrievingTask(Uri audioUri, ServerFile serverFile, MainTVPresenter.ViewHolder viewHolder) {
        this.audioUri = audioUri;
        this.viewHolder = viewHolder;
        this.serverFile = serverFile;
    }

    public static void execute(Uri audioUri, ServerFile serverFile) {
        new AudioMetadataRetrievingTask(audioUri, serverFile).execute();
    }

    public static void execute(Uri audioUri, ServerFile serverFile, MainTVPresenter.ViewHolder viewHolder) {
        new AudioMetadataRetrievingTask(audioUri, serverFile, viewHolder).execute();
    }

    @Override
    protected BusEvent doInBackground(Void... parameters) {
        MediaMetadataRetriever audioMetadataRetriever = new MediaMetadataRetriever();

        try {
            audioMetadataRetriever.setDataSource(audioUri.toString(), new HashMap<String, String>());

            String audioTitle = audioMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            String audioArtist = audioMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            String audioAlbum = audioMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
            String duration = audioMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            Bitmap audioAlbumArt = extractAlbumArt(audioMetadataRetriever);

            return new AudioMetadataRetrievedEvent(audioTitle, audioArtist, audioAlbum,
                duration, audioAlbumArt, viewHolder, serverFile);
        } catch (RuntimeException e) {
            return new AudioMetadataRetrievedEvent(null, null, null, null, null, viewHolder, serverFile);
        } finally {
            audioMetadataRetriever.release();
        }
    }

    private Bitmap extractAlbumArt(MediaMetadataRetriever audioMetadataRetriever) {
        byte[] audioAlbumArtBytes = audioMetadataRetriever.getEmbeddedPicture();

        if (audioAlbumArtBytes == null) {
            return null;
        }

        return BitmapFactory.decodeByteArray(audioAlbumArtBytes, 0, audioAlbumArtBytes.length);
    }

    @Override
    protected void onPostExecute(BusEvent busEvent) {
        super.onPostExecute(busEvent);

        BusProvider.getBus().post(busEvent);
    }
}
