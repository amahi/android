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
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.ViewAnimator;

import com.squareup.picasso.Downloader;
import com.squareup.picasso.Picasso;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.R;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerShare;
import org.amahi.anywhere.util.Fragments;
import org.videolan.libvlc.EventHandler;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.LibVlcException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

public class ServerFileAudioFragment extends Fragment implements MediaController.MediaPlayerControl
{
	public static final Set<String> SUPPORTED_FORMATS;

	static {
		SUPPORTED_FORMATS = new HashSet<String>(Arrays.asList(
			"audio/aac",
			"audio/flac",
			"audio/mp4",
			"audio/mpeg",
			"audio/x-aac",
			"audio/x-m4a"
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

	private long vlcTime;

	@Override
	public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
		return layoutInflater.inflate(R.layout.fragment_server_file_audio, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		setUpSavedState(savedInstanceState);

		setUpInjections();

		setUpFile();
	}

	private void setUpSavedState(Bundle savedState) {
		if (savedState == null) {
			return;
		}

		vlcTime = savedState.getLong(SavedState.VLC_TIME);
	}

	private void setUpInjections() {
		AmahiApplication.from(getActivity()).inject(this);
	}

	private void setUpFile() {
		setUpFileMetadata();
		setUpFileImage();
	}

	private void setUpFileMetadata() {
		MediaMetadataRetriever metadataRetriever = getFileMetadataRetriever();

		String title = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
		String artist = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
		String album = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);

		TextView titleView = (TextView) getView().findViewById(R.id.text_title);
		TextView artistView = (TextView) getView().findViewById(R.id.text_artist);
		TextView albumView = (TextView) getView().findViewById(R.id.text_album);

		titleView.setText(title);
		artistView.setText(artist);
		albumView.setText(album);
	}

	private MediaMetadataRetriever getFileMetadataRetriever() {
		MediaMetadataRetriever retriever = new MediaMetadataRetriever();

		retriever.setDataSource(getFileUri().toString(), new HashMap<String, String>());

		return retriever;
	}

	private void setUpFileImage() {
		ImageView albumArtView = (ImageView) getView().findViewById(R.id.image_album_art);

		Picasso picasso = new Picasso.Builder(getActivity())
			.downloader(new AlbumArtDownloader())
			.build();

		picasso
			.load(getFileUri())
			.fit()
			.centerInside()
			.placeholder(android.R.color.darker_gray)
			.into(albumArtView);
	}

	private static final class AlbumArtDownloader implements Downloader
	{
		@Override
		public Response load(Uri uri, boolean localCacheOnly) throws IOException {
			byte[] albumArtBytes = getMetadataRetriever(uri).getEmbeddedPicture();
			InputStream albumArtStream = new ByteArrayInputStream(albumArtBytes);

			return new Response(albumArtStream, false);
		}

		private MediaMetadataRetriever getMetadataRetriever(Uri uri) {
			MediaMetadataRetriever retriever = new MediaMetadataRetriever();

			retriever.setDataSource(uri.toString(), new HashMap<String, String>());

			return retriever;
		}
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

			vlcStatus = VlcStatus.PAUSED;
		} catch (LibVlcException e) {
			throw new RuntimeException(e);
		}
	}

	private void createVlcControls() {
		vlcControls = new MediaController(getActivity());

		vlcControls.setMediaPlayer(this);
		vlcControls.setAnchorView(getView().findViewById(R.id.animator));
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
		private final WeakReference<ServerFileAudioFragment> fragmentKeeper;

		private VlcEvents(ServerFileAudioFragment fragment) {
			this.fragmentKeeper = new WeakReference<ServerFileAudioFragment>(fragment);
		}

		@Override
		public void handleMessage(Message message) {
			super.handleMessage(message);

			switch (message.getData().getInt("event")) {
				case EventHandler.MediaPlayerPlaying:
					fragmentKeeper.get().setUpVlcTime();
					fragmentKeeper.get().showFileContent();
					fragmentKeeper.get().showVlcControls();
					break;

				default:
					break;
			}
		}
	}

	private void setUpVlcTime() {
		if (vlc.getTime() == 0) {
			vlc.setTime(vlcTime);
		}
	}

	private void showFileContent() {
		ViewAnimator animator = (ViewAnimator) getView().findViewById(R.id.animator);

		View content = getView().findViewById(R.id.layout_content);

		if (animator.getDisplayedChild() != animator.indexOfChild(content)) {
			animator.setDisplayedChild(animator.indexOfChild(content));
		}
	}

	private void showVlcControls() {
		Animation showAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_up);
		vlcControls.startAnimation(showAnimation);

		vlcControls.show(0);
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
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		tearDownSavedState(outState);
	}

	private void tearDownSavedState(Bundle savedState) {
		savedState.putLong(SavedState.VLC_TIME, vlcTime);
	}
}
