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

import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.DrawableRes;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.amahi.anywhere.R;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerShare;
import org.amahi.anywhere.util.Mimes;

import java.util.ArrayList;
import java.util.List;

/**
 * Files filtering base adapter class
 * for the {@link ServerFilesAdapter}
 * and the {@link ServerFilesMetadataAdapter}.
 */
public abstract class FilesFilterBaseAdapter extends BaseAdapter implements Filterable {


    static final ForegroundColorSpan fcs = new ForegroundColorSpan(Color.parseColor("#be5e00"));
    static String queryString;
    LayoutInflater layoutInflater;
    ServerClient serverClient;
    ServerShare serverShare;
    List<ServerFile> files;
    List<ServerFile> filteredFiles;
    private FilesFilter filesFilter;
    private onFilterListChange onFilterListChange;

    @DrawableRes
    static int getFileIcon(ServerFile file) {
        switch (Mimes.match(file.getMime())) {
            case Mimes.Type.ARCHIVE:
                return R.drawable.ic_file_archive;

            case Mimes.Type.AUDIO:
                return R.drawable.ic_file_audio;

            case Mimes.Type.CODE:
                return R.drawable.ic_file_code;

            case Mimes.Type.DOCUMENT:
                return R.drawable.ic_file_text;

            case Mimes.Type.DIRECTORY:
                return R.drawable.ic_file_directory;

            case Mimes.Type.IMAGE:
                return R.drawable.ic_file_image;

            case Mimes.Type.PRESENTATION:
                return R.drawable.ic_file_presentation;

            case Mimes.Type.SPREADSHEET:
                return R.drawable.ic_file_spreadsheet;

            case Mimes.Type.VIDEO:
                return R.drawable.ic_file_video;

            default:
                return R.drawable.ic_file_generic;
        }
    }

    abstract void bindView(ServerFile file, View view);

    abstract View newView(ViewGroup container);

    public <T extends onFilterListChange> void setFilterListChangeListener(T t) {
        this.onFilterListChange = t;
    }

    @Override
    public int getCount() {
        return filteredFiles.size();
    }

    @Override
    public ServerFile getItem(int i) {
        return filteredFiles.get(i);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup container) {
        ServerFile file = getItem(position);

        if (view == null) {
            view = newView(container);
        }

        bindView(file, view);

        return view;
    }

    public void replaceWith(ServerShare serverShare, List<ServerFile> files) {
        this.files = files;
        this.filteredFiles = files;
        this.serverShare = serverShare;

        notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        if (filesFilter == null) {
            filesFilter = new FilesFilter();
        }
        return filesFilter;
    }

    public List<ServerFile> getItems() {
        return files;
    }

    void setUpImageIcon(ServerFile file, ImageView fileIconView) {
        Picasso.with(fileIconView.getContext())
                .load(getImageUri(file))
                .centerCrop()
                .fit()
                .placeholder(getFileIcon(file))
                .into(fileIconView);
    }

    private Uri getImageUri(ServerFile file) {
        return serverClient.getFileUri(serverShare, file);
    }

    public interface onFilterListChange {
        void isListEmpty(boolean empty);
    }

    private class FilesFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            if (constraint != null && constraint.length() > 0) {
                queryString = constraint.toString();
                List<ServerFile> tempList = new ArrayList<>();
                for (ServerFile serverFile : files) {
                    if (serverFile.getName().toLowerCase().contains(constraint.toString().toLowerCase()))
                        tempList.add(serverFile);
                }

                filterResults.count = tempList.size();
                filterResults.values = tempList;
            } else {
                queryString = "";
                filterResults.count = files.size();
                filterResults.values = files;
            }

            return filterResults;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            onFilterListChange.isListEmpty(filterResults.count == 0);
            filteredFiles = (List<ServerFile>) filterResults.values;
            notifyDataSetChanged();
        }
    }

}
