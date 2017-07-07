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

package org.amahi.anywhere.tv.presenter;

import android.content.Context;
import android.net.Uri;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.Presenter;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.util.Mimes;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ServerFileTvPresenter extends Presenter {

    private Context mContext;
    private ServerClient mServerClient;

    public ServerFileTvPresenter(Context context, ServerClient serverClient) {
        mContext = context;
        mServerClient = serverClient;
    }

    @Override
    public Presenter.ViewHolder onCreateViewHolder(ViewGroup parent) {
        ImageCardView cardView = new ImageCardView(mContext);
        cardView.setFocusable(true);
        cardView.setFocusableInTouchMode(true);
        return new ViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {
        final ServerFile serverFile = (ServerFile) item;
        ViewHolder view = ((ViewHolder) viewHolder);
        view.mCardView.setTitleText(serverFile.getName());

        if (isDirectory(serverFile)) {
            Date d = serverFile.getModificationTime();
            SimpleDateFormat dt = new SimpleDateFormat("EEE LLL dd yyyy");
            view.mCardView.setContentText(dt.format(d));
        }
//        else {
//            view.mCardView.setContentText(Formatter.formatFileSize(mContext, serverFile.getSize()));
//        }

        view.mCardView.setMainImageDimensions(400, 300);

        if (isImage(serverFile)) {
            setUpImageIcon(serverFile, view.mCardView.getMainImageView());
        } else {
            view.mCardView.setMainImage(ContextCompat.getDrawable(mContext, Mimes.getTVFileIcon(serverFile)));
        }

    }

    private boolean isImage(ServerFile file) {
        return Mimes.match(file.getMime()) == Mimes.Type.IMAGE;
    }

    private boolean isDirectory(ServerFile file) {
        return Mimes.match(file.getMime()) == Mimes.Type.DIRECTORY;
    }

    private void setUpImageIcon(ServerFile file, ImageView fileIconView) {
        Glide.with(fileIconView.getContext())
                .load(getImageUri(file))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .placeholder(Mimes.getTVFileIcon(file))
                .into(fileIconView);
    }

    private Uri getImageUri(ServerFile file) {
        return mServerClient.getFileUri(file.getParentShare(), file);
    }

    @Override
    public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {

    }

    private class ViewHolder extends Presenter.ViewHolder {
        private ImageCardView mCardView;

        ViewHolder(View view) {
            super(view);
            mCardView = (ImageCardView) view;
        }
    }

}