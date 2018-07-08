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

import android.support.annotation.Nullable;
import android.widget.ImageView;

import org.amahi.anywhere.adapter.AudioFilesAdapter;
import org.amahi.anywhere.model.AudioMetadata;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.tv.presenter.MainTVPresenter;

public class AudioMetadataRetrievedEvent implements BusEvent {
    private final AudioMetadata metadata;
    private final ServerFile serverFile;

    private MainTVPresenter.ViewHolder viewHolder;
    private AudioFilesAdapter.AudioFileViewHolder audioFileHolder;
    private ImageView imageView;

    public AudioMetadataRetrievedEvent(AudioMetadata metadata,
                                       ServerFile serverFile,
                                       MainTVPresenter.ViewHolder viewHolder) {
        this.metadata = metadata;
        this.serverFile = serverFile;
        this.viewHolder = viewHolder;
    }

    public AudioMetadataRetrievedEvent(AudioMetadata metadata,
                                       ServerFile serverFile,
                                       AudioFilesAdapter.AudioFileViewHolder viewHolder) {
        this.metadata = metadata;
        this.serverFile = serverFile;
        this.audioFileHolder = viewHolder;
    }

    public AudioFilesAdapter.AudioFileViewHolder getAudioFileHolder() {
        return audioFileHolder;
    }

    @Nullable
    public MainTVPresenter.ViewHolder getViewHolder() {
        return viewHolder;
    }

    @Nullable
    public ImageView getImageView() {
        return imageView;
    }

    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
    }

    public ServerFile getServerFile() {
        return serverFile;
    }

    public AudioMetadata getAudioMetadata() {
        return this.metadata;
    }
}
