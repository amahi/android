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
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.VideoFinishedEvent;
import org.amahi.anywhere.bus.VideoSizeChangedEvent;
import org.amahi.anywhere.bus.VideoStartedEvent;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerShare;
import org.videolan.libvlc.EventHandler;
import org.videolan.libvlc.IVideoPlayer;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.LibVlcException;

import javax.inject.Inject;

public class VideoService extends Service implements IVideoPlayer
{
	private static enum VideoStatus
	{
		PLAYING, PAUSED
	}

	private LibVLC videoPlayer;
	private VideoStatus videoStatus;
	private VideoEventsHandler videoEventsHandler;

	private ServerShare videoShare;
	private ServerFile videoFile;

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
		try {
			videoPlayer = LibVLC.getInstance();
			videoPlayer.init(this);

			videoStatus = VideoStatus.PAUSED;
		} catch (LibVlcException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean isVideoStarted() {
		return (videoShare != null) && (videoFile != null);
	}

	public void startVideo(ServerShare videoShare, ServerFile videoFile) {
		this.videoShare = videoShare;
		this.videoFile = videoFile;

		setUpVideoEvents();
		setUpVideoPlayback();
	}

	private void setUpVideoEvents() {
		videoEventsHandler = new VideoEventsHandler();

		EventHandler.getInstance().addHandler(videoEventsHandler);
	}

	private void setUpVideoPlayback() {
		videoPlayer.playMRL(getVideoUri().toString());

		videoStatus = VideoStatus.PLAYING;
	}

	private Uri getVideoUri() {
		return serverClient.getFileUri(videoShare, videoFile);
	}

	public LibVLC getVideoPlayer() {
		return videoPlayer;
	}

	public boolean isVideoPlaying() {
		return videoStatus == VideoStatus.PLAYING;
	}

	public void playVideo() {
		videoPlayer.play();

		videoStatus = VideoStatus.PLAYING;
	}

	public void pauseVideo() {
		videoPlayer.pause();

		videoStatus = VideoStatus.PAUSED;
	}

	@Override
	public void setSurfaceSize(int width, int height, int visibleWidth, int visibleHeight, int sarNumber, int sarDensity) {
		changeVideoSize(width, height);
	}

	private void changeVideoSize(int width, int height) {
		Message message = Message.obtain(videoEventsHandler, VideoEvent.CHANGE_VIDEO_SIZE, width, height);
		message.sendToTarget();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		tearDownVideoEvents();
		tearDownVideoPlayer();
	}

	private void tearDownVideoEvents() {
		EventHandler.getInstance().removeHandler(videoEventsHandler);
	}

	private void tearDownVideoPlayer() {
		videoPlayer.stop();
		videoPlayer.destroy();
	}

	public static final class VideoEvent
	{
		private VideoEvent() {
		}

		public static final int CHANGE_VIDEO_SIZE = 42;
	}

	private static final class VideoEventsHandler extends Handler
	{
		@Override
		public void handleMessage(Message message) {
			super.handleMessage(message);

			switch (message.what) {
				case VideoEvent.CHANGE_VIDEO_SIZE:
					BusProvider.getBus().post(new VideoSizeChangedEvent(message.arg1, message.arg2));
					break;

				default:
					break;
			}

			switch (message.getData().getInt("event")) {
				case EventHandler.MediaPlayerPlaying:
					BusProvider.getBus().post(new VideoStartedEvent());
					break;

				case EventHandler.MediaPlayerEndReached:
					BusProvider.getBus().post(new VideoFinishedEvent());
					break;

				default:
					break;
			}
		}
	}

	public static final class VideoServiceBinder extends Binder
	{
		private final VideoService videoService;

		public VideoServiceBinder(VideoService videoService) {
			this.videoService = videoService;
		}

		public VideoService getVideoService() {
			return videoService;
		}
	}
}
