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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.ImageView;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.adapter.AudioFilesAdapter;
import org.amahi.anywhere.bus.AudioMetadataRetrievedEvent;
import org.amahi.anywhere.bus.BusEvent;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.cache.CacheManager;
import org.amahi.anywhere.db.entities.OfflineFile;
import org.amahi.anywhere.db.repositories.OfflineFileRepository;
import org.amahi.anywhere.model.AudioMetadata;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.tv.presenter.MainTVPresenter;
import org.amahi.anywhere.util.FileManager;

import java.lang.ref.WeakReference;
import java.util.HashMap;

import javax.inject.Inject;

/**
 * Async wrapper for audio metadata retrieving.
 * The retrieving itself is done via {@link android.media.MediaMetadataRetriever}.
 */
public class AudioMetadataRetrievingTask extends AsyncTask<Void, Void, BusEvent> {
    private final Uri audioUri;
    private final ServerFile serverFile;

    @Inject
    CacheManager cacheManager;
    private String audioPath;
    private String uniqueKey;
    private boolean isOfflineFile;
    private MainTVPresenter.ViewHolder viewHolder;
    private WeakReference<ImageView> imageViewWeakReference;
    private AudioFilesAdapter.AudioFileViewHolder audioFileHolder;

    private AudioMetadataRetrievingTask(Context context, Uri audioUri, ServerFile serverFile) {
        this.audioUri = audioUri;
        this.serverFile = serverFile;
        this.uniqueKey = serverFile.getUniqueKey();
        setUpInjections(context);

        setUpOfflineTask(context, serverFile);
    }

    public AudioMetadataRetrievingTask(Context context, String path, ServerFile serverFile) {
        this.audioPath = path;
        this.isOfflineFile = true;
        audioUri = null;
        this.serverFile = serverFile;
        setUpInjections(context);
    }

    public AudioMetadataRetrievingTask(Context context, Uri audioUri, String uniqueKey) {
        this.audioUri = audioUri;
        this.serverFile = null;
        this.uniqueKey = uniqueKey;
        setUpInjections(context);
    }

    private void setUpOfflineTask(Context context, ServerFile serverFile) {
        OfflineFileRepository repository = new OfflineFileRepository(context);
        OfflineFile offlineFile = repository.getOfflineFile(serverFile.getName(), serverFile.getModificationTime().getTime());

        if (offlineFile != null && offlineFile.getState() == OfflineFile.DOWNLOADED) {
            isOfflineFile = true;
            audioPath = FileManager.newInstance(context).getContentUriForOfflineFile(serverFile.getName()).toString();
        }
    }

    public static AudioMetadataRetrievingTask newInstance(Context context, Uri audioUri, ServerFile serverFile) {
        return new AudioMetadataRetrievingTask(context, audioUri, serverFile);
    }

    private void setUpInjections(Context context) {
        AmahiApplication.from(context).inject(this);
    }

    public AudioMetadataRetrievingTask setViewHolder(MainTVPresenter.ViewHolder viewHolder) {
        this.viewHolder = viewHolder;
        return this;
    }

    public AudioMetadataRetrievingTask setImageView(ImageView imageView) {
        this.imageViewWeakReference = new WeakReference<>(imageView);
        return this;
    }

    public AudioMetadataRetrievingTask setAudioFileHolder(AudioFilesAdapter.AudioFileViewHolder audioFileHolder) {
        this.audioFileHolder = audioFileHolder;

        return this;
    }

    @Override
    protected BusEvent doInBackground(Void... parameters) {
        AudioMetadata metadata;
        if (serverFile != null) {
            uniqueKey = serverFile.getUniqueKey();
        }
        metadata = cacheManager.getMetadataFromCache(uniqueKey);
        if (metadata == null) {
            metadata = new AudioMetadata();
            MediaMetadataRetriever audioMetadataRetriever = new MediaMetadataRetriever();

            try {
                if (!isOfflineFile) {
                    audioMetadataRetriever.setDataSource(audioUri.toString(), new HashMap<>());
                } else {
                    audioMetadataRetriever.setDataSource(audioPath);
                }

                metadata.setAudioTitle(audioMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
                metadata.setAudioArtist(audioMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
                metadata.setAudioAlbum(audioMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));
                metadata.setDuration(audioMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
                metadata.setAudioAlbumArt(extractAlbumArt(audioMetadataRetriever));

                if (uniqueKey != null)
                    cacheManager.addMetadataToCache(uniqueKey, metadata);
            } catch (RuntimeException ignored) {
            } finally {
                audioMetadataRetriever.release();
            }
        }
        AudioMetadataRetrievedEvent event;
        if (audioFileHolder != null) {
            event = new AudioMetadataRetrievedEvent(metadata, serverFile, audioFileHolder);
        } else {
            event = new AudioMetadataRetrievedEvent(metadata, serverFile, viewHolder);
        }
        if (imageViewWeakReference != null && imageViewWeakReference.get() != null) {
            event.setImageView(imageViewWeakReference.get());
        }
        return event;
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
