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

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerShare;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import javax.inject.Inject;

/**
 * Video service. Does all the work related to the video playback.
 */
public class VideoService extends Service
{
	private ServerShare videoShare;
	private ServerFile videoFile;

	private LibVLC libvlc;
	private MediaPlayer mMediaPlayer = null;

	@Inject
	ServerClient serverClient;

	@Override
	public IBinder onBind(Intent intent) {
		return new VideoServiceBinder(this);
	}

	@Override
	public void onCreate() {
		super.onCreate();

		setUpInjections();

		setUpVideoPlayer();
	}

	private void setUpInjections() {
		AmahiApplication.from(this).inject(this);
	}

	private void setUpVideoPlayer() {
		libvlc = new LibVLC(this);
		mMediaPlayer = new MediaPlayer(libvlc);
	}

	public boolean isVideoStarted() {
		return (videoShare != null) && (videoFile != null);
	}

	public void startVideo(ServerShare videoShare, ServerFile videoFile) {
		this.videoShare = videoShare;
		this.videoFile = videoFile;

		setUpVideoPlayback();
	}

	private void setUpVideoPlayback() {
		Media media = new Media(libvlc, getVideoUri());
		mMediaPlayer.setMedia(media);
		mMediaPlayer.play();
	}

	private Uri getVideoUri() {
		return serverClient.getFileUri(videoShare, videoFile);
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
	}


	@Override
	public void onDestroy() {
		super.onDestroy();

		tearDownVideoPlayback();
	}


	private void tearDownVideoPlayback() {
		mMediaPlayer.stop();
		mMediaPlayer.release();
	}


	public static final class VideoServiceBinder extends Binder
	{
		private final VideoService videoService;

		VideoServiceBinder(VideoService videoService) {
			this.videoService = videoService;
		}

		public VideoService getVideoService() {
			return videoService;
		}
	}
}
