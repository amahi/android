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

package org.amahi.anywhere.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.R;
import org.amahi.anywhere.db.entities.OfflineFile;
import org.amahi.anywhere.db.entities.RecentFile;
import org.amahi.anywhere.db.repositories.OfflineFileRepository;
import org.amahi.anywhere.db.repositories.RecentFileRepository;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerShare;
import org.amahi.anywhere.util.Downloader;
import org.amahi.anywhere.util.Fragments;
import org.amahi.anywhere.view.TouchImageView;

import java.io.File;

import javax.inject.Inject;

/**
 * Image fragment. Shows a single image.
 */
public class ServerFileImageFragment extends Fragment implements RequestListener<Uri, GlideDrawable> {
    @Inject
    ServerClient serverClient;

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        return layoutInflater.inflate(R.layout.fragment_server_file_image, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setUpInjections();

        setUpImage();
    }

    private void setUpInjections() {
        AmahiApplication.from(getActivity()).inject(this);
    }

    private void setUpImage() {
        setUpImageContent();
    }

    private void setUpImageContent() {
        ServerFile file = getFile();
        if (isFileAvailableOffline(file)) {
            Glide
                .with(getActivity())
                .load(getOfflineFilePath(getFile().getName()))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(getImageView());
            showImageContent();
        } else {
            Glide
                .with(getActivity())
                .load(getImageUri())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .listener(this)
                .into(getImageView());
        }
    }

    private boolean isFileAvailableOffline(ServerFile serverFile) {
        OfflineFileRepository repository = new OfflineFileRepository(getContext());
        OfflineFile file = repository.getOfflineFile(serverFile.getName(), serverFile.getModificationTime().getTime());
        return file != null && file.getState() == OfflineFile.DOWNLOADED;
    }

    private File getOfflineFilePath(String name) {
        return new File(getActivity().getFilesDir() + "/" + Downloader.OFFLINE_PATH + "/" + name);
    }

    private Uri getImageUri() {
        if (getShare() != null) {
            return serverClient.getFileUri(getShare(), getFile());
        } else {
            return getRecentFileUri();
        }
    }

    private Uri getRecentFileUri() {
        RecentFileRepository repository = new RecentFileRepository(getContext());
        RecentFile recentFile = repository.getRecentFile(getFile().getUniqueKey());
        return Uri.parse(recentFile.getUri());
    }

    private ServerShare getShare() {
        return getArguments().getParcelable(Fragments.Arguments.SERVER_SHARE);
    }

    private ServerFile getFile() {
        return getArguments().getParcelable(Fragments.Arguments.SERVER_FILE);
    }

    private TouchImageView getImageView() {
        return getView().findViewById(R.id.image);
    }

    private ProgressBar getProgressBar() {
        return getView().findViewById(android.R.id.progress);
    }

    @Override
    public boolean onResourceReady(GlideDrawable resource, Uri model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
        showImageContent();
        return false;
    }

    private void showImageContent() {
        getImageView().setVisibility(View.VISIBLE);
        getProgressBar().setVisibility(View.GONE);
    }

    @Override
    public boolean onException(Exception e, Uri model, Target<GlideDrawable> target, boolean isFirstResource) {
        return false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        tearDownImageContent();
    }

    private void tearDownImageContent() {
        Glide.clear(getImageView());
    }
}
