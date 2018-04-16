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
import android.content.Intent;
import android.net.Uri;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.Presenter;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.squareup.otto.Subscribe;

import org.amahi.anywhere.R;
import org.amahi.anywhere.adapter.ServerFilesMetadataAdapter;
import org.amahi.anywhere.bus.AudioMetadataRetrievedEvent;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.FileMetadataRetrievedEvent;
import org.amahi.anywhere.bus.FileOpeningEvent;
import org.amahi.anywhere.model.AudioMetadata;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerShare;
import org.amahi.anywhere.task.AudioMetadataRetrievingTask;
import org.amahi.anywhere.task.FileMetadataRetrievingTask;
import org.amahi.anywhere.util.Intents;
import org.amahi.anywhere.util.Mimes;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainTVPresenter extends Presenter {

    private Context mContext;
    private int mSelectedBackgroundColor = -1;
    private int mDefaultBackgroundColor = -1;
    private ServerClient mServerClient;
    private List<ServerFile> mServerFileList;
    private ServerShare parentShare;

    public MainTVPresenter(Context context, ServerClient serverClient, List<ServerFile> serverFiles) {
        mContext = context;
        mServerClient = serverClient;
        mServerFileList = serverFiles;
        BusProvider.getBus().register(this);
    }

    public MainTVPresenter(Context context, ServerClient serverClient) {
        mContext = context;
        mServerClient = serverClient;
        BusProvider.getBus().register(this);
    }

    public MainTVPresenter(Context context, ServerClient serverClient, ServerShare parentShare) {
        mContext = context;
        mServerClient = serverClient;
        this.parentShare = parentShare;
    }

    @Override
    public Presenter.ViewHolder onCreateViewHolder(ViewGroup parent) {
        mDefaultBackgroundColor =
            ContextCompat.getColor(parent.getContext(), R.color.background_secondary);
        mSelectedBackgroundColor =
            ContextCompat.getColor(parent.getContext(), R.color.primary);
        ImageCardView cardView = new ImageCardView(parent.getContext()) {
            @Override
            public void setSelected(boolean selected) {
                updateCardBackgroundColor(this, selected);
                super.setSelected(selected);
            }
        };
        cardView.setFocusable(true);
        cardView.setFocusableInTouchMode(true);
        return new ViewHolder(cardView);
    }

    private void updateCardBackgroundColor(ImageCardView view, boolean selected) {
        int color = selected ? mSelectedBackgroundColor : mDefaultBackgroundColor;

        view.setInfoAreaBackgroundColor(color);
    }

    @Override
    public void onBindViewHolder(Presenter.ViewHolder viewHolderArgs, Object item) {
        final ServerFile serverFile = (ServerFile) item;
        ViewHolder viewHolder = (ViewHolder) viewHolderArgs;
        viewHolder.mCardView.setTitleText(serverFile.getName());
        viewHolder.mCardView.setInfoAreaBackgroundColor(mDefaultBackgroundColor);
        viewHolder.mCardView.setBackgroundColor(mDefaultBackgroundColor);
        if (isMetadataAvailable(serverFile)) {
            setUpMetaDimensions(viewHolder);
            if (isVideo(serverFile)) {
                View fileView = viewHolder.view;
                fileView.setTag(ServerFilesMetadataAdapter.Tags.SHARE, serverFile.getParentShare());
                fileView.setTag(ServerFilesMetadataAdapter.Tags.FILE, serverFile);
                new FileMetadataRetrievingTask(mServerClient, fileView, viewHolder).execute();
            } else if (isDirectory(serverFile))
                viewHolder.mCardView.setVisibility(View.VISIBLE);
            else
                viewHolder.mCardView.setVisibility(View.GONE);
        } else {
            setUpDimensions(viewHolder);
        }
        populateData(serverFile, viewHolder);
        viewHolder.mCardView.setOnClickListener(v -> {
            if (isDirectory(serverFile)) {
                Intent intent = Intents.Builder.with(mContext).buildServerTvFilesActivity(serverFile.getParentShare(), serverFile);
                mContext.startActivity(intent);
            } else {
                startFileOpening(serverFile);
            }
        });
    }


    private boolean isMetadataAvailable(ServerFile serverFile) {
        if (parentShare == null)
            return ServerShare.Tag.MOVIES.equals(serverFile.getParentShare().getTag());
        else
            return ServerShare.Tag.MOVIES.equals(parentShare.getTag());
    }

    private void setUpMetaDimensions(ViewHolder viewHolder) {
        viewHolder.mCardView.setMainImageDimensions(400, 500);
    }

    private void setDate(ServerFile serverFile, ViewHolder viewHolder) {
        Date d = serverFile.getModificationTime();
        SimpleDateFormat dt = new SimpleDateFormat("EEE LLL dd yyyy", Locale.US);
        viewHolder.mCardView.setContentText(dt.format(d));
    }

    private void populateData(ServerFile serverFile, ViewHolder viewHolder) {
        if (isDirectory(serverFile))
            setDate(serverFile, viewHolder);
        if (isMetadataAvailable(serverFile) && isVideo(serverFile))
            viewHolder.mCardView.setContentText("");
        if (isImage(serverFile)) {
            setUpImageIcon(serverFile, viewHolder.mCardView.getMainImageView(), getImageUri(serverFile));
        } else if (isAudio(serverFile)) {
            AudioMetadataRetrievingTask
                .newInstance(mContext, getImageUri(serverFile), serverFile)
                .setViewHolder(viewHolder)
                .execute();
        } else {
            setUpDrawable(serverFile, viewHolder);
        }
    }

    private void setUpDimensions(ViewHolder viewHolder) {
        viewHolder.mCardView.setMainImageDimensions(400, 300);
    }

    private void setUpDrawable(ServerFile serverFile, ViewHolder viewHolder) {
        viewHolder.mCardView.setMainImageScaleType(ImageView.ScaleType.CENTER_INSIDE);
        viewHolder.mCardView.setMainImage(ContextCompat.getDrawable(mContext, Mimes.getTVFileIcon(serverFile)));
        if (!isMetadataAvailable(serverFile))
            viewHolder.mCardView.getMainImageView().setPadding(50, 50, 50, 50);
    }

    @Subscribe
    public void onFileMetadataRetrieved(FileMetadataRetrievedEvent event) {
        ServerFile serverFile = event.getFile();
        ViewHolder viewHolder = event.getViewHolder();
        serverFile.setMetaDataFetched(true);
        if (event.getFileMetadata() == null) {
            populateData(serverFile, viewHolder);
        } else {
            viewHolder.mCardView.setMainImageScaleType(ImageView.ScaleType.CENTER_CROP);
            setUpImageIcon(serverFile, viewHolder.mCardView.getMainImageView(), Uri.parse(event.getFileMetadata().getArtworkUrl()));
        }
    }

    @Subscribe
    public void onAudioMetadataRetrieved(AudioMetadataRetrievedEvent event) {
        AudioMetadata metadata = event.getAudioMetadata();
        if (metadata.getAudioAlbumArt() != null) {
            ViewHolder viewHolder = event.getViewHolder();
            if (viewHolder != null) {
                viewHolder.mCardView.getMainImageView().setImageBitmap(metadata.getAudioAlbumArt());
            }
        } else
            setUpMusicLogo(event.getViewHolder());
    }

    private void setUpMusicLogo(ViewHolder viewHolder) {
        if (viewHolder != null)
            viewHolder.mCardView.setMainImage(ContextCompat.getDrawable(mContext, R.drawable.tv_ic_audio));
    }

    private boolean isImage(ServerFile file) {
        return Mimes.match(file.getMime()) == Mimes.Type.IMAGE;
    }

    private boolean isAudio(ServerFile file) {
        return Mimes.match(file.getMime()) == Mimes.Type.AUDIO;
    }

    private boolean isDirectory(ServerFile file) {
        return Mimes.match(file.getMime()) == Mimes.Type.DIRECTORY;
    }

    private boolean isVideo(ServerFile file) {
        return Mimes.match(file.getMime()) == Mimes.Type.VIDEO;
    }

    private void setUpImageIcon(ServerFile file, ImageView fileIconView, Uri url) {
        Glide.with(fileIconView.getContext())
            .load(url.toString())
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .centerCrop()
            .placeholder(Mimes.getTVFileIcon(file))
            .into(fileIconView);
    }

    private Uri getImageUri(ServerFile file) {
        if (parentShare == null)
            return mServerClient.getFileUri(file.getParentShare(), file);
        else
            return mServerClient.getFileUri(parentShare, file);
    }

    private void startFileOpening(ServerFile file) {
        BusProvider.getBus().post(new FileOpeningEvent(file.getParentShare(), mServerFileList, file));
    }

    @Override
    public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {
    }

    public class ViewHolder extends Presenter.ViewHolder {
        private ImageCardView mCardView;

        ViewHolder(View view) {
            super(view);
            mCardView = (ImageCardView) view;
            mCardView.getMainImageView().setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.tv_ic_audio));
        }
    }
}
