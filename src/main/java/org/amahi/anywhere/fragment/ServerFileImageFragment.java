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

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.R;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerShare;
import org.amahi.anywhere.util.Fragments;
import org.amahi.anywhere.view.TouchImageView;

import javax.inject.Inject;

/**
 * Image fragment. Shows a single image.
 */
public class ServerFileImageFragment extends Fragment implements Callback {
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
        Picasso
                .with(getActivity())
                .load(getImageUri())
                .fit()
                .centerInside()
                .into(getImageView(), this);
    }

    private Uri getImageUri() {
        return serverClient.getFileUri(getShare(), getFile());
    }

    private ServerShare getShare() {
        return getArguments().getParcelable(Fragments.Arguments.SERVER_SHARE);
    }

    private ServerFile getFile() {
        return getArguments().getParcelable(Fragments.Arguments.SERVER_FILE);
    }

    private TouchImageView getImageView() {
        return (TouchImageView) getView().findViewById(R.id.image);
    }

    @Override
    public void onSuccess() {
        showImageContent();
    }

    private void showImageContent() {
        getImageView().setVisibility(View.VISIBLE);
    }

    @Override
    public void onError() {
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        tearDownImageContent();
    }

    private void tearDownImageContent() {
        Picasso
                .with(getActivity())
                .cancelRequest(getImageView());
    }
}
