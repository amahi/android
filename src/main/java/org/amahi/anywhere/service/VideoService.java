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

package org.amahi.anywhere.service;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;

import com.squareup.otto.Subscribe;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.ServerFilesLoadedEvent;
import org.amahi.anywhere.db.FileInfoDbHelper;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerShare;
import org.amahi.anywhere.util.Mimes;
import org.amahi.anywhere.util.Preferences;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Video service. Does all the work related to the video playback.
 */
public class VideoService extends Service {
    @Inject
    ServerClient serverClient;
    private ServerShare videoShare;
    private ServerFile videoFile;

    private FileInfoDbHelper fileInfoDbHelper;

    private long pauseTime;

    private LibVLC mLibVLC;
    private MediaPlayer mMediaPlayer = null;

    @Override
    public IBinder onBind(Intent intent) {
        return new VideoServiceBinder(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        setUpInjections();

        setUpVideoPlayer();

        BusProvider.getBus().register(this);
    }

    private void setUpInjections() {
        AmahiApplication.from(this).inject(this);
    }

    private void setUpVideoPlayer() {
        final ArrayList<String> args = new ArrayList<>();
        args.add("-vvv");
        mLibVLC = new LibVLC(this, args);
        mMediaPlayer = new MediaPlayer(mLibVLC);
    }

    public boolean isVideoStarted() {
        return (videoShare != null) && (videoFile != null);
    }

    public void startVideo(ServerShare videoShare, ServerFile videoFile, boolean isSubtitleEnabled) {
        this.videoShare = videoShare;
        this.videoFile = videoFile;

        setUpVideoPlayback(isSubtitleEnabled);
    }

    private void setUpVideoPlayback(boolean isSubtitleEnabled) {
        Media media = new Media(mLibVLC, getVideoUri());
        mMediaPlayer.setMedia(media);
        media.release();
        if (isSubtitleEnabled) {
            searchSubtitleFile();
        }
        mMediaPlayer.play();
    }

    private Uri getVideoUri() {
        return serverClient.getFileUri(videoShare, videoFile);
    }

    private void searchSubtitleFile() {
        if (serverClient.isConnected()) {
            if (!isDirectoryAvailable()) {
                serverClient.getFiles(videoShare);
            } else {
                serverClient.getFiles(videoShare, getDirectory());
            }
        }
    }

    @Subscribe
    public void onFilesLoaded(ServerFilesLoadedEvent event) {
        List<ServerFile> files = event.getServerFiles();
        for (ServerFile file : files) {
            if (videoFile.getNameOnly().equals(file.getNameOnly())) {
                if (Mimes.match(file.getMime()) == Mimes.Type.SUBTITLE) {
                    mMediaPlayer.getMedia().addSlave(
                        new Media.Slave(
                            Media.Slave.Type.Subtitle, 4, getSubtitleUri(file)));
                    break;
                }
            }
        }
    }

    private String getSubtitleUri(ServerFile file) {
        return serverClient.getFileUri(videoShare, file).toString();
    }

    private boolean isDirectoryAvailable() {
        return getDirectory() != null;
    }

    private ServerFile getDirectory() {
        return videoFile.getParentFile();
    }

    public MediaPlayer getMediaPlayer() {
        return mMediaPlayer;
    }

    public boolean isVideoPlaying() {
        return mMediaPlayer.isPlaying();
    }

    public void playVideo() {
        mMediaPlayer.play();
    }

    public void pauseVideo() {
        mMediaPlayer.pause();
        updatePauseTime();
    }

    private void updatePauseTime() {
        pauseTime = getMediaPlayer().getTime();
    }

    public ServerFile getVideoFile() {
        return videoFile;
    }

    public ServerShare getVideoShare() {
        return videoShare;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        checkVideoFilePlayed();

        tearDownVideoPlayback();
        BusProvider.getBus().unregister(this);
    }


    private void checkVideoFilePlayed() {
        setUpDbHelper();

        String file_path = Preferences.getServerName(this) + "/" + getVideoShare().getName() + getVideoFile().getPath();

        if (pauseTime > 10000 && !fileInfoDbHelper.getFilePlayed(file_path)) {
            fileInfoDbHelper.setFilePlayed(file_path, true);
        }

        closeDb();
    }

    private void setUpDbHelper() {
        fileInfoDbHelper = FileInfoDbHelper.init(this);
    }

    private void closeDb() {
        fileInfoDbHelper.closeDataBase();
    }

    private void tearDownVideoPlayback() {
        mMediaPlayer.stop();
        mMediaPlayer.release();
        mLibVLC.release();
    }


    public static final class VideoServiceBinder extends Binder {
        private final VideoService videoService;

        VideoServiceBinder(VideoService videoService) {
            this.videoService = videoService;
        }

        public VideoService getVideoService() {
            return videoService;
        }
    }
}
