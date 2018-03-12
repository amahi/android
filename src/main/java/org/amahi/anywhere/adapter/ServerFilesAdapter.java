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
import android.graphics.Typeface;
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
import org.amahi.anywhere.db.FileInfoDbHelper;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerShare;
import org.amahi.anywhere.task.AudioMetadataRetrievingTask;
import org.amahi.anywhere.util.Mimes;
import org.amahi.anywhere.util.RecyclerViewItemClickListener;
import org.amahi.anywhere.util.Preferences;

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
    private FileInfoDbHelper fileInfoDbHelper;

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
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final ServerFileViewHolder fileHolder = (ServerFileViewHolder) holder;
        final ServerFile file = getItems().get(position);

        if (Mimes.match(file.getMime()) == Mimes.Type.DIRECTORY) {
            fileHolder.moreInfo.setVisibility(View.GONE);

        } else {
            fileHolder.moreInfo.setVisibility(View.VISIBLE);

            fileHolder.fileSize.setText(Formatter.formatFileSize(context, getFileSize(file)));

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

        fileHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedPosition = fileHolder.getAdapterPosition();
                mListener.onItemClick(fileHolder.itemView, fileHolder.getAdapterPosition());
                fileHolder.itemView.setActivated(true);
            }
        });

        fileHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                selectedPosition = fileHolder.getAdapterPosition();
                boolean isHandled = mListener.onLongItemClick(fileHolder.itemView, fileHolder.getAdapterPosition());
                fileHolder.itemView.setActivated(true);
                return isHandled;
            }
        });

        fileHolder.itemView.setActivated(selectedPosition == position);

        if (Mimes.match(file.getMime()) == Mimes.Type.AUDIO ||
            Mimes.match(file.getMime()) == Mimes.Type.VIDEO) {

            setUpDbHelper();

            String file_path = Preferences.getServerName(context) + "/" + serverShare.getName() + file.getPath();

            if (fileInfoDbHelper.getFilePlayed(file_path)) {
                fileHolder.fileTextView.setTypeface(fileHolder.fileTextView.getTypeface(), Typeface.BOLD);
                fileHolder.fileTextView.setTextColor(context.getResources().getColor(R.color.played_file));
            } else {
                fileHolder.fileTextView.setTextColor(context.getResources().getColor(R.color.primary_text_material_light));
            }

            closeDb();
        }

    }

    private void setUpDbHelper() {
        fileInfoDbHelper = FileInfoDbHelper.init(context);
    }

    private void closeDb() {
        fileInfoDbHelper.closeDataBase();
    }

    private long getFileSize(ServerFile file) {
        return file.getSize();
    }

    private Date getLastModified(ServerFile file) {
        return file.getModificationTime();
    }

    private void setUpAudioArt(ServerFile serverFile, ImageView fileIconView) {
        AudioMetadataRetrievingTask
            .newInstance(context, serverClient.getFileUri(serverShare, serverFile), serverFile)
            .setImageView(fileIconView)
            .execute();
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

    public class ServerFileViewHolder extends RecyclerView.ViewHolder {
        ImageView fileIconView;
        TextView fileTextView, fileSize, fileLastModified;
        LinearLayout moreInfo;

        ServerFileViewHolder(View itemView) {
            super(itemView);
            fileIconView = (ImageView) itemView.findViewById(R.id.icon);
            fileTextView = (TextView) itemView.findViewById(R.id.text);
            fileSize = (TextView) itemView.findViewById(R.id.file_size);
            fileLastModified = (TextView) itemView.findViewById(R.id.last_modified);
            moreInfo = (LinearLayout) itemView.findViewById(R.id.more_info);
        }
    }

    public void setOnClickListener(RecyclerViewItemClickListener mListener) {
        this.mListener = mListener;
    }

}
