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

import android.app.Fragment;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.R;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerShare;
import org.amahi.anywhere.util.Android;
import org.amahi.anywhere.util.Fragments;
import org.videolan.libvlc.EventHandler;
import org.videolan.libvlc.IVideoPlayer;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.LibVlcException;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

public class ServerFileVideoFragment extends Fragment implements SurfaceHolder.Callback, IVideoPlayer, MediaController.MediaPlayerControl, View.OnTouchListener
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

	private LibVLC vlc;

	private VlcEvents vlcEvents;

	private MediaController vlcControls;

	@Override
	public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
		return layoutInflater.inflate(R.layout.fragment_server_file_video, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		setUpInjections();

		setUpFile();
	}

	private void setUpInjections() {
		AmahiApplication.from(getActivity()).inject(this);
	}

	private void setUpFile() {
		setUpFileView();
	}

	private void setUpFileView() {
		SurfaceHolder surfaceHolder = getSurface().getHolder();

		surfaceHolder.setFormat(PixelFormat.RGBX_8888);
		surfaceHolder.setKeepScreenOn(true);

		surfaceHolder.addCallback(this);
	}

	private SurfaceView getSurface() {
		return (SurfaceView) getView().findViewById(R.id.surface);
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
		Message message = Message.obtain(vlcEvents, 0, width, height);
		message.sendToTarget();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
		vlc.detachSurface();
	}

	@Override
	public void onResume() {
		super.onResume();

		createVlc();
		createVlcControls();

		startVlc(getFileUri());
	}

	private void createVlc() {
		try {
			vlc = LibVLC.getInstance();
			vlc.init(getActivity());
		} catch (LibVlcException e) {
			throw new RuntimeException(e);
		}
	}

	private void createVlcControls() {
		vlcControls = new MediaController(getActivity());

		vlcControls.setMediaPlayer(this);
		vlcControls.setAnchorView(getView().findViewById(R.id.layout_content));

		getView().setOnTouchListener(this);
	}

	@Override
	public void start() {
		vlc.play();
	}

	@Override
	public boolean canPause() {
		return true;
	}

	@Override
	public void pause() {
		vlc.pause();
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
		return vlc.isPlaying();
	}

	@Override
	public int getBufferPercentage() {
		return 0;
	}

	@Override
	public int getAudioSessionId() {
		return 0;
	}

	@Override
	public boolean onTouch(View view, MotionEvent motionEvent) {
		vlcControls.show();

		return false;
	}

	private void startVlc(Uri uri) {
		vlcEvents = new VlcEvents(this);
		EventHandler.getInstance().addHandler(vlcEvents);

		vlc.playMRL(uri.toString());
	}

	private Uri getFileUri() {
		return serverClient.getFileUri(getShare(), getFile());
	}

	private ServerShare getShare() {
		return getArguments().getParcelable(Fragments.Arguments.SERVER_SHARE);
	}

	private ServerFile getFile() {
		return getArguments().getParcelable(Fragments.Arguments.SERVER_FILE);
	}

	private static final class VlcEvents extends Handler
	{
		private final WeakReference<ServerFileVideoFragment> fragmentKeeper;

		private VlcEvents(ServerFileVideoFragment fragment) {
			this.fragmentKeeper = new WeakReference<ServerFileVideoFragment>(fragment);
		}

		@Override
		public void handleMessage(Message message) {
			super.handleMessage(message);

			if (message.what == 0) {
				fragmentKeeper.get().changeSurfaceSize(message.arg1, message.arg2);
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
		int screenWidth = Android.getDeviceScreenWidth(getActivity());
		int screenHeight = Android.getDeviceScreenHeight(getActivity());

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
	public void onPause() {
		super.onPause();

		stopVlc();
	}

	private void stopVlc() {
		EventHandler.getInstance().removeHandler(vlcEvents);

		vlc.stop();
		vlc.detachSurface();
	}
}
