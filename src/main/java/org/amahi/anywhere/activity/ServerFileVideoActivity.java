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
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.MediaController;
import android.widget.ViewAnimator;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.R;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerShare;
import org.amahi.anywhere.util.Android;
import org.amahi.anywhere.util.Intents;
import org.videolan.libvlc.EventHandler;
import org.videolan.libvlc.IVideoPlayer;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.LibVlcException;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

public class ServerFileVideoActivity extends Activity implements IVideoPlayer,
	SurfaceHolder.Callback,
	MediaController.MediaPlayerControl,
	Runnable,
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

	private static final class SavedState
	{
		private SavedState() {
		}

		public static final String VLC_TIME = "vlc_time";
	}

	private static enum VlcStatus
	{
		PLAYING, PAUSED
	}

	@Inject
	ServerClient serverClient;

	private LibVLC vlc;

	private VlcEvents vlcEvents;

	private MediaController vlcControls;

	private VlcStatus vlcStatus;

	private Handler vlcControlsHandler;

	private long vlcTime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_server_file_video);

		setUpSavedState(savedInstanceState);

		setUpInjections();

		setUpVideo();

		setUpSystemControls();
	}

	private void setUpSavedState(Bundle savedState) {
		if (savedState == null) {
			return;
		}

		vlcTime = savedState.getLong(SavedState.VLC_TIME);
	}

	private void setUpInjections() {
		AmahiApplication.from(this).inject(this);
	}

	private void setUpVideo() {
		setUpVideoTitle();
		setUpVideoView();
	}

	private void setUpVideoTitle() {
		getActionBar().setTitle(getFile().getName());
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
		vlc.attachSurface(surfaceHolder.getSurface(), this);
	}

	@Override
	public void setSurfaceSize(int width, int height, int visibleWidth, int visibleHeight, int sarNumber, int sarDensity) {
		Message message = Message.obtain(vlcEvents, 42, width, height);
		message.sendToTarget();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
		vlc.detachSurface();
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
	public void onResume() {
		super.onResume();

		createVlc();
		createVlcControls();

		startVlc(getVideoUri());
	}

	private void createVlc() {
		try {
			vlc = LibVLC.getInstance();
			vlc.init(this);

			vlcStatus = VlcStatus.PAUSED;
		} catch (LibVlcException e) {
			throw new RuntimeException(e);
		}
	}

	private void createVlcControls() {
		vlcControls = new MediaController(this);

		vlcControls.setMediaPlayer(this);
		vlcControls.setAnchorView(findViewById(R.id.container_controls));
	}

	@Override
	public void start() {
		vlc.play();

		vlcStatus = VlcStatus.PLAYING;
	}

	@Override
	public boolean canPause() {
		return true;
	}

	@Override
	public void pause() {
		vlc.pause();

		vlcStatus = VlcStatus.PAUSED;
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
		vlc.setTime(time);
	}

	@Override
	public int getDuration() {
		return (int) vlc.getLength();
	}

	@Override
	public int getCurrentPosition() {
		return (int) vlc.getTime();
	}

	@Override
	public boolean isPlaying() {
		return vlcStatus == VlcStatus.PLAYING;
	}

	@Override
	public int getBufferPercentage() {
		return 0;
	}

	@Override
	public int getAudioSessionId() {
		return 0;
	}

	private void startVlc(Uri uri) {
		vlcEvents = new VlcEvents(this);
		EventHandler.getInstance().addHandler(vlcEvents);

		vlc.playMRL(uri.toString());

		vlcStatus = VlcStatus.PLAYING;
	}

	private Uri getVideoUri() {
		return serverClient.getFileUri(getShare(), getFile());
	}

	private ServerShare getShare() {
		return getIntent().getParcelableExtra(Intents.Extras.SERVER_SHARE);
	}

	private ServerFile getFile() {
		return getIntent().getParcelableExtra(Intents.Extras.SERVER_FILE);
	}

	private static final class VlcEvents extends Handler
	{
		private final WeakReference<ServerFileVideoActivity> activityKeeper;

		private VlcEvents(ServerFileVideoActivity fragment) {
			this.activityKeeper = new WeakReference<ServerFileVideoActivity>(fragment);
		}

		@Override
		public void handleMessage(Message message) {
			super.handleMessage(message);

			if (message.what == 42) {
				activityKeeper.get().changeSurfaceSize(message.arg1, message.arg2);
			}

			switch (message.getData().getInt("event")) {
				case EventHandler.MediaPlayerPlaying:
					activityKeeper.get().setUpVlcTime();
					activityKeeper.get().showFileContent();
					activityKeeper.get().showControls();
					break;

				default:
					break;
			}
		}
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

	private void setUpVlcTime() {
		if (vlc.getTime() == 0) {
			vlc.setTime(vlcTime);
		}
	}

	private void showFileContent() {
		ViewAnimator animator = (ViewAnimator) findViewById(R.id.animator);

		View contentLayout = findViewById(R.id.layout_content);

		if (animator.getDisplayedChild() != animator.indexOfChild(contentLayout)) {
			animator.setDisplayedChild(animator.indexOfChild(contentLayout));
		}
	}

	private void showControls() {
		showSystemControls();
		showVlcControls();

		hideControlsDelayed();
	}

	private void showSystemControls() {
		getActivityView().setSystemUiVisibility(
			View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
			View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
			View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
	}

	private void showVlcControls() {
		Animation showAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_up);
		vlcControls.startAnimation(showAnimation);

		vlcControls.show(0);
	}

	private void hideControlsDelayed() {
		vlcControlsHandler = new Handler();
		vlcControlsHandler.postDelayed(this, TimeUnit.SECONDS.toMillis(3));
	}

	@Override
	public void run() {
		hideControls();
	}

	private void hideControls() {
		hideVlcControls();
		hideSystemControls();
	}

	private void hideVlcControls() {
		vlcControls.hide();

		Animation hideAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_down);
		vlcControls.startAnimation(hideAnimation);
	}

	private void hideSystemControls() {
		getActivityView().setSystemUiVisibility(
			View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
			View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
			View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
			View.SYSTEM_UI_FLAG_FULLSCREEN |
			View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
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

	@Override
	public void onPause() {
		super.onPause();

		stopVlc();
	}

	private void stopVlc() {
		EventHandler.getInstance().removeHandler(vlcEvents);

		vlcTime = vlc.getTime();

		vlc.stop();
		vlc.detachSurface();

		vlcControlsHandler.removeCallbacks(this);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		tearDownSavedState(outState);
	}

	private void tearDownSavedState(Bundle savedState) {
		savedState.putLong(SavedState.VLC_TIME, vlcTime);
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
}
