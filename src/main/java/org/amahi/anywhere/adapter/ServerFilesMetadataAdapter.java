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
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.amahi.anywhere.R;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerFileMetadata;
import org.amahi.anywhere.task.FileMetadataRetrievingTask;
import org.amahi.anywhere.util.Mimes;

import java.util.Collections;

public class ServerFilesMetadataAdapter extends FilesFilterBaseAdapter {
    public ServerFilesMetadataAdapter(Context context, ServerClient serverClient) {
        this.layoutInflater = LayoutInflater.from(context);

        this.serverClient = serverClient;

        this.files = Collections.emptyList();
        this.filteredFiles = Collections.emptyList();
    }

    private static void bindFileView(ServerFile file, View fileView) {
        TextView fileTitle = (TextView) fileView.getTag(Tags.FILE_TITLE);
        ImageView fileIcon = (ImageView) fileView.getTag(Tags.FILE_ICON);

        SpannableStringBuilder sb = new SpannableStringBuilder(file.getName());
        if (queryString != null && !TextUtils.isEmpty(queryString)) {
            int searchMatchPosition = file.getName().toLowerCase().indexOf(queryString.toLowerCase());
            if (searchMatchPosition != -1)
                sb.setSpan(fcs, searchMatchPosition, searchMatchPosition + queryString.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        }
        fileTitle.setText(sb);
        fileTitle.setBackgroundResource(R.color.background_transparent_secondary);

        fileIcon.setImageResource(getFileIcon(file));
        fileIcon.setBackgroundResource(R.color.background_secondary);
    }

    public static void bindView(ServerFile file, ServerFileMetadata fileMetadata, View fileView) {
        if (fileMetadata == null) {
            bindFileView(file, fileView);
        } else {
            bindFileMetadataView(file, fileMetadata, fileView);
        }
    }

    private static void bindFileMetadataView(ServerFile file, ServerFileMetadata fileMetadata, View fileView) {
        TextView fileTitle = (TextView) fileView.getTag(Tags.FILE_TITLE);
        ImageView fileIcon = (ImageView) fileView.getTag(Tags.FILE_ICON);

        fileTitle.setText(null);
        fileTitle.setBackgroundResource(android.R.color.transparent);

        Picasso.with(fileView.getContext())
                .load(fileMetadata.getArtworkUrl())
                .centerCrop()
                .fit()
                .placeholder(getFileIcon(file))
                .error(getFileIcon(file))
                .into(fileIcon);
        fileTitle.setBackgroundResource(android.R.color.transparent);
    }

    protected View newView(ViewGroup container) {
        View fileView = layoutInflater.inflate(R.layout.view_server_file_metadata_item, container, false);

        fileView.setTag(Tags.FILE_TITLE, fileView.findViewById(R.id.text));
        fileView.setTag(Tags.FILE_ICON, fileView.findViewById(R.id.icon));

        return fileView;
    }

    protected void bindView(ServerFile file, View fileView) {
        unbindFileView(file, fileView);

        ImageView fileIcon = (ImageView) fileView.getTag(Tags.FILE_ICON);
        if (Mimes.match(file.getMime()) != Mimes.Type.VIDEO) {
            bindFileView(file, fileView);
        } else {
            bindFileMetadataView(file, fileView);
        }
        if (Mimes.match(file.getMime()) == Mimes.Type.IMAGE) {
            setUpImageIcon(file, fileIcon);
        }
    }

    private void unbindFileView(ServerFile file, View fileView) {
        TextView fileTitle = (TextView) fileView.getTag(Tags.FILE_TITLE);
        ImageView fileIcon = (ImageView) fileView.getTag(Tags.FILE_ICON);

        fileTitle.setText(null);
        fileTitle.setBackgroundResource(android.R.color.transparent);

        fileIcon.setImageResource(getFileIcon(file));
        fileIcon.setBackgroundResource(R.color.background_secondary);
    }

    private void bindFileMetadataView(ServerFile file, View fileView) {
        fileView.setTag(Tags.SHARE, serverShare);
        fileView.setTag(Tags.FILE, file);

        FileMetadataRetrievingTask.execute(serverClient, fileView);
    }

    public static final class Tags {
        public static final int SHARE = R.id.container_files;
        public static final int FILE = R.attr.server_share;
        public static final int FILE_TITLE = R.id.text;
        public static final int FILE_ICON = R.id.icon;
        private Tags() {
        }
    }
}
