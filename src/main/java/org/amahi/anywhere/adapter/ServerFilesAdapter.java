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

package org.amahi.anywhere.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import org.amahi.anywhere.R;
import org.amahi.anywhere.bus.AudioMetadataRetrievedEvent;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.task.AudioMetadataRetrievingTask;
import org.amahi.anywhere.util.Downloader;
import org.amahi.anywhere.util.Mimes;
import org.amahi.anywhere.util.ServerFileClickListener;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

/**
 * Files adapter. Visualizes files
 * for the {@link org.amahi.anywhere.fragment.ServerFilesFragment}.
 */
public class ServerFilesAdapter extends FilesFilterAdapter {
    private Context context;

    public ServerFilesAdapter(Context context, ServerClient serverClient) {
        this.serverClient = serverClient;
        this.layoutInflater = LayoutInflater.from(context);
        this.context = context;
        this.files = Collections.emptyList();
        this.filteredFiles = Collections.emptyList();
        BusProvider.getBus().register(this);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ServerFileViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_server_file_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final ServerFileViewHolder fileHolder = (ServerFileViewHolder) holder;

        final ServerFile file = filteredFiles.get(position);

        if (Mimes.match(file.getMime()) == Mimes.Type.DIRECTORY) {
            fileHolder.moreInfo.setVisibility(View.GONE);
            fileHolder.moreOptions.setVisibility(View.GONE);

        } else {
            fileHolder.moreInfo.setVisibility(View.VISIBLE);
            fileHolder.moreInfo.setVisibility(View.VISIBLE);

            if (!isOfflineMode()) {
                fileHolder.fileSize.setText(Formatter.formatFileSize(context, getFileSize(file)));
            } else {
                File localFile = new File(context.getFilesDir(), Downloader.OFFLINE_PATH + "/" + file.getName());
                fileHolder.fileSize.setText(Formatter.formatFileSize(context, localFile.length()));
            }

            Date d = getLastModified(file);
            SimpleDateFormat dt = new SimpleDateFormat("EEE LLL dd yyyy", Locale.getDefault());
            fileHolder.fileLastModified.setText(dt.format(d));
        }

        SpannableStringBuilder sb = new SpannableStringBuilder(file.getName());
        if (queryString != null && !TextUtils.isEmpty(queryString)) {
            int searchMatchPosition = file.getName().toLowerCase().indexOf(queryString.toLowerCase());
            if (searchMatchPosition != -1)
                sb.setSpan(fcs, searchMatchPosition, searchMatchPosition + queryString.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        }
        fileHolder.fileTextView.setText(sb);

        if (Mimes.match(file.getMime()) == Mimes.Type.IMAGE) {
            setUpImageIcon(file, fileHolder.fileIconView);
        } else if (Mimes.match(file.getMime()) == Mimes.Type.AUDIO) {
            setUpAudioArt(file, fileHolder.fileIconView);
        } else {
            fileHolder.fileIconView.setImageResource(Mimes.getFileIcon(file));
        }

        fileHolder.itemView.setOnClickListener(view -> {
            mListener.onItemClick(fileHolder.itemView, fileHolder.getAdapterPosition());
        });

        fileHolder.moreOptions.setOnClickListener(view -> {
            selectedPosition = fileHolder.getAdapterPosition();
            mListener.onMoreOptionClick(fileHolder.itemView, fileHolder.getAdapterPosition());
        });
    }

    private boolean isOfflineMode() {
        return getAdapterMode() == AdapterMode.OFFLINE;
    }

    private long getFileSize(ServerFile file) {
        return file.getSize();
    }

    private Date getLastModified(ServerFile file) {
        return file.getModificationTime();
    }

    private void setUpAudioArt(ServerFile serverFile, ImageView fileIconView) {
        if (getAdapterMode() != AdapterMode.OFFLINE) {
            AudioMetadataRetrievingTask
                .newInstance(context, serverClient.getFileUri(serverShare, serverFile), serverFile)
                .setImageView(fileIconView)
                .execute();
        } else {
            new AudioMetadataRetrievingTask(context, getAudioPath(serverFile), serverFile)
                .setImageView(fileIconView)
                .execute();
        }
    }

    private String getAudioPath(ServerFile serverFile) {
        return context.getFilesDir() + "/" + Downloader.OFFLINE_PATH + "/" + serverFile.getName();
    }

    @Subscribe
    public void onAudioMetadataRetrieved(AudioMetadataRetrievedEvent event) {
        ImageView imageView = event.getImageView();
        Bitmap bitmap = event.getAudioMetadata().getAudioAlbumArt();
        if (bitmap != null && imageView != null) {
            imageView.setImageBitmap(bitmap);
        }
    }


    public void tearDownCallbacks() {
        BusProvider.getBus().unregister(this);
    }

    public void setOnClickListener(ServerFileClickListener mListener) {
        this.mListener = mListener;
    }

    public class ServerFileViewHolder extends RecyclerView.ViewHolder {
        ImageView fileIconView, moreOptions;
        TextView fileTextView, fileSize, fileLastModified;
        LinearLayout moreInfo;

        ServerFileViewHolder(View itemView) {
            super(itemView);
            fileIconView = itemView.findViewById(R.id.icon);
            fileTextView = itemView.findViewById(R.id.text);
            fileSize = itemView.findViewById(R.id.file_size);
            fileLastModified = itemView.findViewById(R.id.last_modified);
            moreInfo = itemView.findViewById(R.id.more_info);
            moreOptions = itemView.findViewById(R.id.more_options);
        }
    }

}
