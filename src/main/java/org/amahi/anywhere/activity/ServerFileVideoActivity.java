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

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.R;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerShare;
import org.amahi.anywhere.service.VideoService;
import org.amahi.anywhere.util.FullScreenHelper;
import org.amahi.anywhere.util.Intents;
import org.amahi.anywhere.view.MediaControls;
import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

/**
 * Video activity. Shows videos, supports basic operations such as pausing, resuming, scrolling.
 * The playback itself is done via {@link org.amahi.anywhere.service.VideoService}.
 * Backed up by {@link android.view.SurfaceView} and {@link org.videolan.libvlc.LibVLC}.
 */
public class ServerFileVideoActivity extends AppCompatActivity implements
	ServiceConnection,
	MediaController.MediaPlayerControl,
	IVLCVout.OnNewVideoLayoutListener,
	MediaPlayer.EventListener,
	View.OnLayoutChangeListener {

	private static final boolean ENABLE_SUBTITLES = true;

	private VideoService videoService;
	private MediaControls videoControls;
	private FullScreenHelper fullScreen;
	private Handler layoutChangeHandler;

	private int mVideoHeight = 0;
	private int mVideoWidth = 0;
	private int mVideoVisibleHeight = 0;
	private int mVideoVisibleWidth = 0;
	private int mVideoSarNum = 0;
	private int mVideoSarDen = 0;

	private SurfaceView mSubtitlesSurface = null;
	private float bufferPercent = 0.0f;

	private enum SurfaceSizes {
		SURFACE_BEST_FIT,
		SURFACE_FIT_SCREEN,
		SURFACE_FILL,
		SURFACE_16_9,
		SURFACE_4_3,
		SURFACE_ORIGINAL;
	}

	private static SurfaceSizes CURRENT_SIZE = SurfaceSizes.SURFACE_BEST_FIT;

	//TODO Add feature for changing the screen size

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_server_file_video);

		setUpInjections();

		setUpHomeNavigation();

		setUpViews();

		setUpVideo();

		setUpFullScreen();

		setUpVideoService();
	}

	private void setUpInjections() {
		AmahiApplication.from(this).inject(this);
	}

	private void setUpHomeNavigation() {
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setIcon(R.drawable.ic_launcher);
	}

	private void setUpViews() {
		if (ENABLE_SUBTITLES) {
			final ViewStub stub = (ViewStub) findViewById(R.id.subtitles_stub);
			mSubtitlesSurface = (SurfaceView) stub.inflate();
			mSubtitlesSurface.setZOrderMediaOverlay(true);
			mSubtitlesSurface.getHolder().setFormat(PixelFormat.TRANSLUCENT);
		}
	}

	private void setUpVideo() {
		setUpVideoTitle();
	}

	private void setUpVideoTitle() {
		getSupportActionBar().setTitle(getVideoFile().getName());
	}

	private ServerFile getVideoFile() {
		return getIntent().getParcelableExtra(Intents.Extras.SERVER_FILE);
	}

	private void setUpFullScreen() {
		fullScreen = new FullScreenHelper(getSupportActionBar(), getVideoMainFrame());
		fullScreen.enableOnClickToggle(false);
		getVideoMainFrame().setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				fullScreen.toggle();
				videoControls.toggle();
			}
		});
		fullScreen.init();
	}

	private MediaPlayer getMediaPlayer() {
		assert videoService != null;
		return videoService.getMediaPlayer();
	}

	@Override
	protected void onStart() {
		super.onStart();
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
		setUpHandlers();
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
		final IVLCVout vlcVout = getMediaPlayer().getVLCVout();
		vlcVout.setVideoView(getSurface());
		if (mSubtitlesSurface != null)
			vlcVout.setSubtitlesView(mSubtitlesSurface);
		vlcVout.attachViews(this);
		getMediaPlayer().setEventListener(this);
	}

	private SurfaceView getSurface() {
		return (SurfaceView) findViewById(R.id.surface);
	}

	private FrameLayout getSurfaceFrame() {
		return (FrameLayout) findViewById(R.id.video_surface_frame);
	}

	private FrameLayout getVideoMainFrame() {
		return (FrameLayout) findViewById(R.id.video_main_frame);
	}

	private void setUpVideoControls() {
		if (!areVideoControlsAvailable()) {
			videoControls = new MediaControls(this);
			videoControls.setMediaPlayer(this);
			videoControls.setAnchorView(getControlsContainer());
		}

	}

	private boolean areVideoControlsAvailable() {
		return videoControls != null;
	}

	private void setUpHandlers() {
		if (!layoutChangeHandlerAvailable()) {
			layoutChangeHandler = new Handler();
		}
	}

	private boolean layoutChangeHandlerAvailable() {
		return layoutChangeHandler != null;
	}

	private View getControlsContainer() {
		return findViewById(R.id.container_controls);
	}

	private void setUpVideoPlayback() {
		if (videoService.isVideoStarted()) {
			showThenAutoHideControls();
		} else {
			videoService.startVideo(getVideoShare(), getVideoFile());
			addLayoutChangeListener();
		}
	}

	private void showThenAutoHideControls() {
		if (!isFinishing()) {
			fullScreen.show();
			fullScreen.delayedHide();
			videoControls.show();
		}
	}

	private ProgressBar getProgressBar() {
		return (ProgressBar) findViewById(android.R.id.progress);
	}

	private ServerShare getVideoShare() {
		return getIntent().getParcelableExtra(Intents.Extras.SERVER_SHARE);
	}

	private void addLayoutChangeListener() {
		getSurfaceFrame().addOnLayoutChangeListener(this);
	}

	@Override
	public void onLayoutChange(View v, int left, int top, int right,
							   int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
		if (left != oldLeft || top != oldTop || right != oldRight || bottom != oldBottom) {
			layoutChangeHandler.removeCallbacks(mRunnable);
			layoutChangeHandler.post(mRunnable);
		}
	}

	private final Runnable mRunnable = new Runnable() {
		@Override
		public void run() {
			updateVideoSurfaces();
		}
	};

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	@Override
	public void onNewVideoLayout(IVLCVout vout, int width, int height, int visibleWidth, int visibleHeight, int sarNum, int sarDen) {
		mVideoWidth = width;
		mVideoHeight = height;
		mVideoVisibleWidth = visibleWidth;
		mVideoVisibleHeight = visibleHeight;
		mVideoSarNum = sarNum;
		mVideoSarDen = sarDen;
		updateVideoSurfaces();
	}

	private void updateVideoSurfaces() {
		int screenWidth = getWindow().getDecorView().getWidth();
		int screenHeight = getWindow().getDecorView().getHeight();

		// sanity check
		if (screenWidth * screenHeight == 0) {
			Log.e("Error", "Invalid surface size");
			return;
		}

		getMediaPlayer().getVLCVout().setWindowSize(screenWidth, screenHeight);
		ViewGroup.LayoutParams lp = getSurface().getLayoutParams();
		if (mVideoWidth * mVideoHeight == 0) {
            /* Case of OpenGL vouts: handles the placement of the video using MediaPlayer API */
			lp.width  = ViewGroup.LayoutParams.MATCH_PARENT;
			lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
			getSurface().setLayoutParams(lp);
			lp = getSurfaceFrame().getLayoutParams();
			lp.width  = ViewGroup.LayoutParams.MATCH_PARENT;
			lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
			getSurfaceFrame().setLayoutParams(lp);
			changeMediaPlayerLayout(screenWidth, screenHeight);
			return;
		}

		if (lp.width == lp.height && lp.width == ViewGroup.LayoutParams.MATCH_PARENT) {
            /* We handle the placement of the video using Android View LayoutParams */
			getMediaPlayer().setAspectRatio(null);
			getMediaPlayer().setScale(0);
		}

		double dw = screenWidth, dh = screenHeight;
		final boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
		if (screenWidth > screenHeight && isPortrait || screenWidth < screenHeight && !isPortrait) {
			dw = screenHeight;
			dh = screenWidth;
		}

		// compute the aspect ratio
		double ar, vw;
		if (mVideoSarDen == mVideoSarNum) {
            /* No indication about the density, assuming 1:1 */
			vw = mVideoVisibleWidth;
			ar = (double)mVideoVisibleWidth / (double)mVideoVisibleHeight;
		} else {
            /* Use the specified aspect ratio */
			vw = mVideoVisibleWidth * (double)mVideoSarNum / mVideoSarDen;
			ar = vw / mVideoVisibleHeight;
		}

		// compute the display aspect ratio
		double dar = dw / dh;
		switch (CURRENT_SIZE) {
			case SURFACE_BEST_FIT:
				if (dar < ar)
					dh = dw / ar;
				else
					dw = dh * ar;
				break;
			case SURFACE_FIT_SCREEN:
				if (dar >= ar)
					dh = dw / ar; /* horizontal */
				else
					dw = dh * ar; /* vertical */
				break;
			case SURFACE_FILL:
				break;
			case SURFACE_16_9:
				ar = 16.0 / 9.0;
				if (dar < ar)
					dh = dw / ar;
				else
					dw = dh * ar;
				break;
			case SURFACE_4_3:
				ar = 4.0 / 3.0;
				if (dar < ar)
					dh = dw / ar;
				else
					dw = dh * ar;
				break;
			case SURFACE_ORIGINAL:
				dh = mVideoVisibleHeight;
				dw = vw;
				break;
		}
		// set display size
		lp.width  = (int) Math.ceil(dw * mVideoWidth / mVideoVisibleWidth);
		lp.height = (int) Math.ceil(dh * mVideoHeight / mVideoVisibleHeight);
		getSurface().setLayoutParams(lp);
		if (mSubtitlesSurface != null)
			mSubtitlesSurface.setLayoutParams(lp);

		// set frame size (crop if necessary)
		lp = getSurfaceFrame().getLayoutParams();
		lp.width = (int) Math.floor(dw);
		lp.height = (int) Math.floor(dh);
		getSurfaceFrame().setLayoutParams(lp);
		getSurface().invalidate();
		if (mSubtitlesSurface != null)
			mSubtitlesSurface.invalidate();
	}

	private void changeMediaPlayerLayout(int displayW, int displayH) {
        /* Change the video placement using the MediaPlayer API */
		switch (CURRENT_SIZE) {
			case SURFACE_BEST_FIT:
				getMediaPlayer().setAspectRatio(null);
				getMediaPlayer().setScale(0);
				break;
			case SURFACE_FIT_SCREEN:
			case SURFACE_FILL: {
				Media.VideoTrack vtrack = getMediaPlayer().getCurrentVideoTrack();
				if (vtrack == null)
					return;
				final boolean videoSwapped = vtrack.orientation == Media.VideoTrack.Orientation.LeftBottom
						|| vtrack.orientation == Media.VideoTrack.Orientation.RightTop;
				if (CURRENT_SIZE == SurfaceSizes.SURFACE_FIT_SCREEN) {
					int videoW = vtrack.width;
					int videoH = vtrack.height;
					if (videoSwapped) {
						int swap = videoW;
						videoW = videoH;
						videoH = swap;
					}
					if (vtrack.sarNum != vtrack.sarDen)
						videoW = videoW * vtrack.sarNum / vtrack.sarDen;
					float videoAspectRatio = videoW / (float) videoH;
					float displayAspectRatio = displayW / (float) displayH;
					float scale;
					if (displayAspectRatio >= videoAspectRatio)
						scale = displayW / (float) videoW; /* horizontal */
					else
						scale = displayH / (float) videoH; /* vertical */
					getMediaPlayer().setScale(scale);
					getMediaPlayer().setAspectRatio(null);
				} else {
					getMediaPlayer().setScale(0);
					getMediaPlayer().setAspectRatio(!videoSwapped ? ""+displayW+":"+displayH
							: ""+displayH+":"+displayW);
				}
				break;
			}
			case SURFACE_16_9:
				getMediaPlayer().setAspectRatio("16:9");
				getMediaPlayer().setScale(0);
				break;
			case SURFACE_4_3:
				getMediaPlayer().setAspectRatio("4:3");
				getMediaPlayer().setScale(0);
				break;
			case SURFACE_ORIGINAL:
				getMediaPlayer().setAspectRatio(null);
				getMediaPlayer().setScale(1);
				break;
		}
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
		getMediaPlayer().setTime(time);
	}

	@Override
	public int getDuration() {
		return (int) getMediaPlayer().getLength();
	}

	@Override
	public int getCurrentPosition() {
		return (int) getMediaPlayer().getTime();
	}

	@Override
	public boolean isPlaying() {
		return getMediaPlayer().isPlaying();
	}

	@Override
	public int getBufferPercentage() {
		return (int) bufferPercent;
	}

	@Override
	public int getAudioSessionId() {
		return 0;
	}

	@Override
	public void onEvent(MediaPlayer.Event event) {

		switch(event.type) {
			case MediaPlayer.Event.MediaChanged:
				getVideoMainFrame().setVisibility(View.VISIBLE);
				break;
			case MediaPlayer.Event.Playing:
				getProgressBar().setVisibility(View.INVISIBLE);
				showThenAutoHideControls();
				break;
			case MediaPlayer.Event.Paused:
				showThenAutoHideControls();
				break;
			case MediaPlayer.Event.EndReached:
				finish();
				break;
			case MediaPlayer.Event.Buffering:
				bufferPercent = event.getBuffering();
				break;
			case MediaPlayer.Event.EncounteredError:
				Toast.makeText(this, R.string.message_error_video, Toast.LENGTH_SHORT).show();
				break;
		}

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
	public void onPause() {
		super.onPause();

		videoControls.hide();

		if (!isChangingConfigurations()) {
			pause();
		}

		if (isFinishing()) {
			tearDownVideoPlayback();
		}
	}

	private void tearDownVideoPlayback() {
		getMediaPlayer().stop();
	}

	@Override
	protected void onStop() {
		super.onStop();
		getSurfaceFrame().removeOnLayoutChangeListener(this);
		getMediaPlayer().getVLCVout().detachViews();
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

	public static boolean supports(String mime_type) {
		String type = mime_type.split("/")[0];

		return "video".equals(type);
	}
}
