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

package org.amahi.anywhere.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;

import com.squareup.otto.Subscribe;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.R;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.VideoFinishedEvent;
import org.amahi.anywhere.bus.VideoSizeChangedEvent;
import org.amahi.anywhere.bus.VideoStartedEvent;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerShare;
import org.amahi.anywhere.service.VideoService;
import org.amahi.anywhere.util.Android;
import org.amahi.anywhere.util.Intents;
import org.amahi.anywhere.util.ViewDirector;
import org.amahi.anywhere.view.MediaControls;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

public class ServerFileVideoActivity extends Activity implements ServiceConnection,
	SurfaceHolder.Callback,
	Runnable,
	MediaController.MediaPlayerControl,
	View.OnSystemUiVisibilityChangeListener
{
	public static final Set<String> SUPPORTED_FORMATS;

	static {
		SUPPORTED_FORMATS = new HashSet<String>(Arrays.asList(
			"video/avi",
			"video/divx",
			"video/mp4",
			"video/x-matroska",
			"video/x-m4v"
		));
	}

	@Inject
	ServerClient serverClient;

	private VideoService videoService;
	private MediaControls videoControls;
	private Handler videoControlsHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_server_file_video);

		setUpInjections();

		setUpHomeNavigation();

		setUpVideo();

		setUpSystemControls();
	}

	private void setUpInjections() {
		AmahiApplication.from(this).inject(this);
	}

	private void setUpHomeNavigation() {
		getActionBar().setHomeButtonEnabled(true);
	}

	private void setUpVideo() {
		setUpVideoTitle();
	}

	private void setUpVideoTitle() {
		getActionBar().setTitle(getVideoFile().getName());
	}

	private ServerFile getVideoFile() {
		return getIntent().getParcelableExtra(Intents.Extras.SERVER_FILE);
	}

	private void setUpSystemControls() {
		setUpSystemControlsListener();

		showSystemControls();
	}

	private void setUpSystemControlsListener() {
		getActivityView().setOnSystemUiVisibilityChangeListener(this);
	}

	private View getActivityView() {
		return getWindow().getDecorView();
	}

	@Override
	public void onSystemUiVisibilityChange(int visibility) {
		if (areSystemControlsVisible(visibility)) {
			showControls();
		}
	}

	private boolean areSystemControlsVisible(int visibility) {
		return (visibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0;
	}

	private void showControls() {
		showSystemControls();
		showVideoControls();

		hideControlsDelayed();
	}

	private void showSystemControls() {
		getActivityView().setSystemUiVisibility(
			View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
			View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
			View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
	}

	private void showVideoControls() {
		if (areVideoControlsAvailable() && !videoControls.isShowing()) {
			videoControls.showAnimated();
		}
	}

	private boolean areVideoControlsAvailable() {
		return videoControls != null;
	}

	private void hideControlsDelayed() {
		if (isVideoControlsHandlerAvailable()) {
			videoControlsHandler.postDelayed(this, TimeUnit.SECONDS.toMillis(3));
		}
	}

	private boolean isVideoControlsHandlerAvailable() {
		return videoControlsHandler != null;
	}

	@Override
	public void run() {
		hideControls();
	}

	private void hideControls() {
		hideSystemControls();
		hideVideoControls();
	}

	private void hideSystemControls() {
		getActivityView().setSystemUiVisibility(
			View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
			View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
			View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
			View.SYSTEM_UI_FLAG_FULLSCREEN |
			View.SYSTEM_UI_FLAG_LOW_PROFILE |
			View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
	}

	private void hideVideoControls() {
		videoControls.hideAnimated();
	}

	@Override
	protected void onStart() {
		super.onStart();

		setUpVideoService();
		setUpVideoServiceBind();
	}

	private void setUpVideoService() {
		Intent intent = new Intent(this, VideoService.class);
		startService(intent);
	}

	private void setUpVideoServiceBind() {
		Intent intent = new Intent(this, VideoService.class);
		bindService(intent, this, Context.BIND_AUTO_CREATE);
	}

	@Override
	public void onServiceDisconnected(ComponentName serviceName) {
	}

	@Override
	public void onServiceConnected(ComponentName serviceName, IBinder serviceBinder) {
		setUpVideoServiceBind(serviceBinder);

		setUpVideoView();
		setUpVideoControls();
		setUpVideoControlsHandler();
		setUpVideoPlayback();
	}

	private void setUpVideoServiceBind(IBinder serviceBinder) {
		VideoService.VideoServiceBinder videoServiceBinder = (VideoService.VideoServiceBinder) serviceBinder;
		videoService = videoServiceBinder.getVideoService();
	}

	private void setUpVideoView() {
		SurfaceHolder surfaceHolder = getSurface().getHolder();

		surfaceHolder.setFormat(PixelFormat.RGBX_8888);
		surfaceHolder.setKeepScreenOn(true);

		surfaceHolder.addCallback(this);
	}

	private SurfaceView getSurface() {
		return (SurfaceView) findViewById(R.id.surface);
	}

	@Override
	public void surfaceCreated(SurfaceHolder surfaceHolder) {
	}

	@Override
	public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
		videoService.getVideoPlayer().attachSurface(surfaceHolder.getSurface(), videoService);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
		videoService.getVideoPlayer().detachSurface();
	}

	private void setUpVideoControls() {
		if (!areVideoControlsAvailable()) {
			videoControls = new MediaControls(this);

			videoControls.setMediaPlayer(this);
			videoControls.setAnchorView(findViewById(R.id.container_controls));
		}
	}

	private void setUpVideoControlsHandler() {
		if (!isVideoControlsHandlerAvailable()) {
			videoControlsHandler = new Handler();
		}
	}

	private void setUpVideoPlayback() {
		if (videoService.isVideoStarted()) {
			showVideo();
			showControls();
		} else {
			videoService.startVideo(getVideoShare(), getVideoFile());
		}
	}

	private void showVideo() {
		ViewDirector.of(this, R.id.animator).show(R.id.layout_content);
	}

	private ServerShare getVideoShare() {
		return getIntent().getParcelableExtra(Intents.Extras.SERVER_SHARE);
	}

	@Subscribe
	public void onVideoStarted(VideoStartedEvent event) {
		start();

		showVideo();
		showControls();
	}

	@Override
	public void start() {
		videoService.playVideo();
	}

	@Override
	public boolean canPause() {
		return true;
	}

	@Override
	public void pause() {
		videoService.pauseVideo();
	}

	@Override
	public boolean canSeekBackward() {
		return true;
	}

	@Override
	public boolean canSeekForward() {
		return true;
	}

	@Override
	public void seekTo(int time) {
		videoService.getVideoPlayer().setTime(time);
	}

	@Override
	public int getDuration() {
		return (int) videoService.getVideoPlayer().getLength();
	}

	@Override
	public int getCurrentPosition() {
		return (int) videoService.getVideoPlayer().getTime();
	}

	@Override
	public boolean isPlaying() {
		return videoService.isVideoPlaying();
	}

	@Override
	public int getBufferPercentage() {
		return 0;
	}

	@Override
	public int getAudioSessionId() {
		return 0;
	}

	@Subscribe
	public void onVideoFinished(VideoFinishedEvent event) {
		finish();
	}

	@Subscribe
	public void onVideoSizeChanged(VideoSizeChangedEvent event) {
		changeSurfaceSize(event.getVideoWidth(), event.getVideoHeight());
	}

	private void changeSurfaceSize(int videoWidth, int videoHeight) {
		if ((videoWidth == 0) && (videoHeight == 0)) {
			return;
		}

		SurfaceView surface = getSurface();

		surface.getHolder().setFixedSize(videoWidth, videoHeight);

		surface.setLayoutParams(getSurfaceLayoutParams(videoWidth, videoHeight));
		surface.invalidate();
	}

	private ViewGroup.LayoutParams getSurfaceLayoutParams(int videoWidth, int videoHeight) {
		int screenWidth = Android.getDeviceScreenWidth(this);
		int screenHeight = Android.getDeviceScreenHeight(this);

		float screenAspectRatio = (float) screenWidth / (float) screenHeight;
		float videoAspectRatio = (float) videoWidth / (float) videoHeight;

		if (screenAspectRatio < videoAspectRatio) {
			screenHeight = (int) (screenWidth / videoAspectRatio);
		} else {
			screenWidth = (int) (screenHeight * videoAspectRatio);
		}

		ViewGroup.LayoutParams surfaceLayoutParams = getSurface().getLayoutParams();

		surfaceLayoutParams.width = screenWidth;
		surfaceLayoutParams.height = screenHeight;

		return surfaceLayoutParams;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem) {
		switch (menuItem.getItemId()) {
			case android.R.id.home:
				finish();
				return true;

			default:
				return super.onOptionsItemSelected(menuItem);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		showControlsForced();

		BusProvider.getBus().register(this);
	}

	private void showControlsForced() {
		showSystemControls();
		showVideoControlsForced();

		hideControlsDelayed();
	}

	private void showVideoControlsForced() {
		if (areVideoControlsAvailable()) {
			videoControls.show();
		}
	}

	@Override
	public void onPause() {
		super.onPause();

		showControls();

		tearDownVideoControlsHandler();

		if (!isChangingConfigurations()) {
			pause();
		}

		if (isFinishing()) {
			tearDownVideoPlayback();
		}

		BusProvider.getBus().unregister(this);
	}

	private void tearDownVideoControlsHandler() {
		if (isVideoControlsHandlerAvailable()) {
			videoControlsHandler.removeCallbacks(this);
		}
	}

	private void tearDownVideoPlayback() {
		videoService.getVideoPlayer().stop();
	}

	@Override
	protected void onStop() {
		super.onStop();

		tearDownVideoServiceBind();
	}

	private void tearDownVideoServiceBind() {
		unbindService(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (isFinishing()) {
			tearDownVideoService();
		}
	}

	private void tearDownVideoService() {
		Intent intent = new Intent(this, VideoService.class);
		stopService(intent);
	}
}
