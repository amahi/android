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
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerShare;
import org.amahi.anywhere.util.Downloader;
import org.amahi.anywhere.util.Mimes;
import org.amahi.anywhere.util.ServerFileClickListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Files filtering base adapter class
 * for the {@link ServerFilesAdapter}
 * and the {@link ServerFilesMetadataAdapter}.
 */
public abstract class FilesFilterAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {

    static final ForegroundColorSpan fcs = new ForegroundColorSpan(Color.parseColor("#be5e00"));
    static String queryString;
    protected ServerFileClickListener mListener;
    protected int selectedPosition = RecyclerView.NO_POSITION;
    LayoutInflater layoutInflater;
    ServerClient serverClient;
    ServerShare serverShare;
    List<ServerFile> files;
    List<ServerFile> filteredFiles;
    private FilesFilter filesFilter;
    private onFilterListChange onFilterListChange;
    private AdapterMode adapterMode = AdapterMode.SERVER;

    public <T extends onFilterListChange> void setFilterListChangeListener(T t) {
        this.onFilterListChange = t;
    }

    public void setOnClickListener(ServerFileClickListener mListener) {
        this.mListener = mListener;
    }

    @Override
    public int getItemCount() {
        return filteredFiles.size();
    }

    public ServerFile getItem(int i) {
        return filteredFiles.get(i);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void replaceWith(ServerShare serverShare, List<ServerFile> files) {
        this.files = files;
        this.filteredFiles = files;
        this.serverShare = serverShare;

        notifyDataSetChanged();
    }

    public void removeFile(int position) {
        ServerFile serverFile = filteredFiles.get(position);
        this.filteredFiles.remove(serverFile);
        this.files.remove(serverFile);
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
        if (adapterMode == AdapterMode.OFFLINE) {
            Glide
                .with(fileIconView.getContext())
                .load(getOfflineFilePath(file.getName(), fileIconView.getContext()))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .placeholder(Mimes.getFileIcon(file))
                .into(fileIconView);
        } else {
            Glide.with(fileIconView.getContext())
                .load(getImageUri(file))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .placeholder(Mimes.getFileIcon(file))
                .into(fileIconView);
        }
    }

    private File getOfflineFilePath(String name, Context context) {
        return new File(context.getFilesDir() + "/" + Downloader.OFFLINE_PATH + "/" + name);
    }

    private Uri getImageUri(ServerFile file) {
        return serverClient.getFileUri(serverShare, file);
    }

    public boolean isEmpty() {
        return (filteredFiles.isEmpty());
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public void setSelectedPosition(int position) {
        this.selectedPosition = position;
    }

    public AdapterMode getAdapterMode() {
        return adapterMode;
    }

    public void setAdapterMode(AdapterMode adapterMode) {
        this.adapterMode = adapterMode;
    }

    public static enum AdapterMode {
        SERVER, OFFLINE
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
