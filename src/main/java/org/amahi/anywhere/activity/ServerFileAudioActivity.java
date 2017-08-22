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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.gms.common.images.WebImage;
import com.squareup.otto.Subscribe;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.R;
import org.amahi.anywhere.bus.AudioCompletedEvent;
import org.amahi.anywhere.bus.AudioControlNextEvent;
import org.amahi.anywhere.bus.AudioControlPreviousEvent;
import org.amahi.anywhere.bus.AudioMetadataRetrievedEvent;
import org.amahi.anywhere.bus.AudioPreparedEvent;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerShare;
import org.amahi.anywhere.service.AudioService;
import org.amahi.anywhere.task.AudioMetadataRetrievingTask;
import org.amahi.anywhere.util.AudioMetadataFormatter;
import org.amahi.anywhere.util.Intents;
import org.amahi.anywhere.util.ViewDirector;
import org.amahi.anywhere.view.MediaControls;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

/**
 * Audio activity. Shows audio, supports basic operations such as pausing, resuming, audio changing.
 * The playback itself is done via {@link org.amahi.anywhere.service.AudioService}.
 * Backed up by {@link android.media.MediaPlayer}.
 */
public class ServerFileAudioActivity extends AppCompatActivity implements
		ServiceConnection,
		MediaController.MediaPlayerControl,
		SessionManagerListener<CastSession> {
	private CastContext mCastContext;
	private CastSession mCastSession;
	private AudioMetadataFormatter metadataFormatter;

	private static final Set<String> SUPPORTED_FORMATS;

	static {
		SUPPORTED_FORMATS = new HashSet<>(Arrays.asList(
				"audio/flac",
				"audio/mp4",
				"audio/mpeg",
				"audio/ogg"
		));
	}

	private PlaybackLocation mLocation = PlaybackLocation.LOCAL;

	public enum PlaybackLocation {
		LOCAL,
		REMOTE
	}

	public static boolean supports(String mime_type) {
		return SUPPORTED_FORMATS.contains(mime_type);
	}

	@Inject
	ServerClient serverClient;

	private AudioService audioService;
	private MediaControls audioControls;

	private ServerFile audioFile;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_server_file_audio);

		setUpInjections();

		setUpHomeNavigation();

		setUpCast();

		setUpAudio();
	}

	private void setUpInjections() {
		AmahiApplication.from(this).inject(this);
	}

	private void setUpHomeNavigation() {
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setIcon(R.drawable.ic_launcher);
	}

	private void setUpCast() {
		mCastContext = CastContext.getSharedInstance(this);
		mCastSession = mCastContext.getSessionManager().getCurrentCastSession();
		if (mCastSession != null && mCastSession.isConnected()) {
			mLocation = PlaybackLocation.REMOTE;
		}
	}

	private void setUpAudio() {
		setUpAudioFile();
		setUpAudioTitle();
	}

	private void setUpAudioFile() {
		this.audioFile = getFile();
	}

	private ServerFile getFile() {
		return getIntent().getParcelableExtra(Intents.Extras.SERVER_FILE);
	}

	private void setUpAudioTitle() {
		getSupportActionBar().setTitle(audioFile.getName());
	}

	private TextView getAudioTitleView() {
		return (TextView) findViewById(R.id.text_title);
	}

	private TextView getAudioSubtitleView() {
		return (TextView) findViewById(R.id.text_subtitle);
	}

	private ImageView getAudioAlbumArtView() {
		return (ImageView) findViewById(R.id.image_album_art);
	}

	private void showAudioMetadata() {
		ViewDirector.of(this, R.id.animator).show(R.id.layout_content);
	}

	@Subscribe
	public void onAudioMetadataRetrieved(AudioMetadataRetrievedEvent event) {
		metadataFormatter = new AudioMetadataFormatter(
				event.getAudioTitle(), event.getAudioArtist(), event.getAudioAlbum());
		metadataFormatter.setDuration(event.getDuration());
		if (mLocation == PlaybackLocation.LOCAL) {
			setUpAudioMetadata(metadataFormatter, event.getAudioAlbumArt());
		} else if (mLocation == PlaybackLocation.REMOTE) {
			loadRemoteMedia(0, true);
			finish();
		}
	}

	private void setUpAudioMetadata(AudioMetadataFormatter audioMetadataFormatter, Bitmap audioAlbumArt) {
		getAudioTitleView().setText(audioMetadataFormatter.getAudioTitle(audioFile));
		getAudioSubtitleView().setText(audioMetadataFormatter.getAudioSubtitle(getShare()));
		if (audioAlbumArt == null) {
			audioAlbumArt = BitmapFactory.decodeResource(getResources(), R.drawable.default_audiotrack);
			getAudioAlbumArtView().setScaleType(ImageView.ScaleType.CENTER_INSIDE);
		} else {
			getAudioAlbumArtView().setScaleType(ImageView.ScaleType.CENTER_CROP);
		}
		getAudioAlbumArtView().setImageBitmap(audioAlbumArt);
	}

	private ServerShare getShare() {
		return getIntent().getParcelableExtra(Intents.Extras.SERVER_SHARE);
	}

	private Uri getAudioUri() {
		return serverClient.getFileUri(getShare(), getFile());
	}

	@Override
	protected void onStart() {
		super.onStart();

		if (mLocation == PlaybackLocation.LOCAL) {
			setUpAudioService();
			setUpAudioServiceBind();
		} else if (mLocation == PlaybackLocation.REMOTE) {
			AudioMetadataRetrievingTask.execute(getAudioUri(), audioFile);
		}
	}

	private void setUpAudioService() {
		Intent intent = new Intent(this, AudioService.class);
		startService(intent);
	}

	private void setUpAudioServiceBind() {
		Intent intent = new Intent(this, AudioService.class);
		bindService(intent, this, Context.BIND_AUTO_CREATE);
	}

	@Override
	public void onServiceDisconnected(ComponentName serviceName) {
	}

	@Override
	public void onServiceConnected(ComponentName serviceName, IBinder serviceBinder) {
		setUpAudioServiceBind(serviceBinder);

		setUpAudioControls();
		setUpAudioPlayback();
	}

	private void setUpAudioServiceBind(IBinder serviceBinder) {
		AudioService.AudioServiceBinder audioServiceBinder = (AudioService.AudioServiceBinder) serviceBinder;
		audioService = audioServiceBinder.getAudioService();
	}

	private void setUpAudioControls() {
		if (!areAudioControlsAvailable()) {
			audioControls = new MediaControls(this);

			audioControls.setMediaPlayer(this);
			audioControls.setPrevNextListeners(new AudioControlsNextListener(), new AudioControlsPreviousListener());
			audioControls.setAnchorView(findViewById(R.id.animator));
		}
	}

	private boolean areAudioControlsAvailable() {
		return audioControls != null;
	}

	private void setUpAudioPlayback() {
		if (audioService.isAudioStarted()) {
			showAudio();
			setUpAudioMetadata();
		} else {
			audioService.startAudio(getShare(), getAudioFiles(), getFile());
		}
	}

	private List<ServerFile> getAudioFiles() {
		List<ServerFile> audioFiles = new ArrayList<ServerFile>();

		for (ServerFile file : getFiles()) {
			if (SUPPORTED_FORMATS.contains(file.getMime())) {
				audioFiles.add(file);
			}
		}

		return audioFiles;
	}

	private List<ServerFile> getFiles() {
		return getIntent().getParcelableArrayListExtra(Intents.Extras.SERVER_FILES);
	}

	@Subscribe
	public void onAudioPrepared(AudioPreparedEvent event) {
		this.audioFile = audioService.getAudioFile();

		start();

		setUpAudioTitle();

		showAudio();
	}

	private void showAudio() {
		showAudioMetadata();
		showAudioControls();
	}

	private void showAudioControls() {
		if (areAudioControlsAvailable() && !audioControls.isShowing()) {
			audioControls.show(0);
		}
	}

	@Subscribe
	public void onNextAudio(AudioControlNextEvent event) {
		tearDownAudioTitle();
		tearDownAudioMetadata();

		hideAudio();
	}

	@Subscribe
	public void onPreviousAudio(AudioControlPreviousEvent event) {
		tearDownAudioTitle();
		tearDownAudioMetadata();

		hideAudio();
	}

	@Subscribe
	public void onAudioCompleted(AudioCompletedEvent event) {
		tearDownAudioTitle();
		tearDownAudioMetadata();

		hideAudio();
	}

	private void tearDownAudioTitle() {
		getSupportActionBar().setTitle(null);
	}

	private void tearDownAudioMetadata() {
		getAudioTitleView().setText(null);
		getAudioSubtitleView().setText(null);
		getAudioAlbumArtView().setImageBitmap(null);
	}

	private void hideAudio() {
		hideAudioMetadata();
		hideAudioControls();
	}

	private void hideAudioMetadata() {
		ViewDirector.of(this, R.id.animator).show(android.R.id.progress);
	}

	private void hideAudioControls() {
		if (areAudioControlsAvailable() && audioControls.isShowing()) {
			audioControls.hide();
		}
	}

	@Override
	public void start() {
		audioService.playAudio();
	}

	@Override
	public boolean canPause() {
		return true;
	}

	@Override
	public void pause() {
		audioService.pauseAudio();
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
		audioService.getAudioPlayer().seekTo(time);
	}

	@Override
	public int getDuration() {
		return audioService.getAudioPlayer().getDuration();
	}

	@Override
	public int getCurrentPosition() {
		return audioService.getAudioPlayer().getCurrentPosition();
	}

	@Override
	public boolean isPlaying() {
		return audioService.getAudioPlayer().isPlaying();
	}

	@Override
	public int getBufferPercentage() {
		return 0;
	}

	@Override
	public int getAudioSessionId() {
		return audioService.getAudioPlayer().getAudioSessionId();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.action_bar_cast_button, menu);
		CastButtonFactory.setUpMediaRouteButton(getApplicationContext(), menu,
				R.id.media_route_menu_item);
		return true;
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

		mCastContext.getSessionManager().addSessionManagerListener(this, CastSession.class);

		showAudioControlsForced();

		BusProvider.getBus().register(this);

		if (hasAudioFileChanged()) {
			setUpAudioMetadata();
		}
	}

	private void showAudioControlsForced() {
		if (areAudioControlsAvailable() && !audioControls.isShowing()) {
			audioControls.show(0);
		}
	}

	private boolean hasAudioFileChanged() {
		return isAudioServiceAvailable() && !this.audioFile.equals(audioService.getAudioFile());
	}

	private void setUpAudioMetadata() {
		if (!isAudioServiceAvailable()) {
			return;
		}

		metadataFormatter = audioService.getAudioMetadataFormatter();
		this.audioFile = audioService.getAudioFile();

		tearDownAudioTitle();
		tearDownAudioMetadata();

		setUpAudioTitle();
		setUpAudioMetadata(audioService.getAudioMetadataFormatter(), audioService.getAudioAlbumArt());
	}

	private boolean isAudioServiceAvailable() {
		return audioService != null;
	}

	@Override
	protected void onPause() {
		super.onPause();

		mCastContext.getSessionManager().removeSessionManagerListener(this, CastSession.class);

		hideAudioControlsForced();

		BusProvider.getBus().unregister(this);

		if (isAudioServiceAvailable() && isFinishing()) {
			tearDownAudioPlayback();
		}
	}

	private void hideAudioControlsForced() {
		if (areAudioControlsAvailable() && audioControls.isShowing()) {
			audioControls.hide();
		}
	}

	private void tearDownAudioPlayback() {
		audioService.pauseAudio();
	}

	@Override
	protected void onStop() {
		super.onStop();

		if (isAudioServiceAvailable()) {
			tearDownAudioServiceBind();
		}
	}

	private void tearDownAudioServiceBind() {
		unbindService(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (isAudioServiceAvailable() && isFinishing()) {
			tearDownAudioService();
		}
	}

	private void tearDownAudioService() {
		Intent intent = new Intent(this, AudioService.class);
		stopService(intent);
	}

	private static final class AudioControlsNextListener implements View.OnClickListener {
		@Override
		public void onClick(View view) {
			BusProvider.getBus().post(new AudioControlNextEvent());
		}
	}

	private static final class AudioControlsPreviousListener implements View.OnClickListener {
		@Override
		public void onClick(View view) {
			BusProvider.getBus().post(new AudioControlPreviousEvent());
		}
	}

	@Override
	public void onSessionEnded(CastSession session, int error) {
		onApplicationDisconnected();
	}

	@Override
	public void onSessionResumed(CastSession session, boolean wasSuspended) {
		onApplicationConnected(session);
	}

	@Override
	public void onSessionResumeFailed(CastSession session, int error) {
		onApplicationDisconnected();
	}

	@Override
	public void onSessionStarted(CastSession session, String sessionId) {
		onApplicationConnected(session);
	}

	@Override
	public void onSessionStartFailed(CastSession session, int error) {
		onApplicationDisconnected();
	}

	@Override
	public void onSessionStarting(CastSession session) {
	}

	@Override
	public void onSessionEnding(CastSession session) {
	}

	@Override
	public void onSessionResuming(CastSession session, String sessionId) {
	}

	@Override
	public void onSessionSuspended(CastSession session, int reason) {
	}

	private void onApplicationConnected(CastSession castSession) {
		mCastSession = castSession;
		boolean isPlaying = false;
		int position = 0;
		if (audioService != null) {
			isPlaying = audioService.isAudioStarted();
			if (isPlaying) {
				audioService.pauseAudio();
				position = audioService.getAudioPlayer().getCurrentPosition();
			}
		}
		loadRemoteMedia(position, isPlaying);
		finish();
	}

	private void onApplicationDisconnected() {
		mCastSession = null;
		mLocation = PlaybackLocation.LOCAL;
		invalidateOptionsMenu();
		if (!isAudioServiceAvailable()) {
			setUpAudioService();
			setUpAudioServiceBind();
		}
	}

	private void loadRemoteMedia(int position, boolean autoPlay) {
		if (mCastSession == null) {
			return;
		}
		final RemoteMediaClient remoteMediaClient = mCastSession.getRemoteMediaClient();
		if (remoteMediaClient == null) {
			return;
		}
		remoteMediaClient.addListener(new RemoteMediaClient.Listener() {
			@Override
			public void onStatusUpdated() {
				Intent intent = new Intent(ServerFileAudioActivity.this, ExpandedControlsActivity.class);
				startActivity(intent);
				remoteMediaClient.removeListener(this);
			}

			@Override
			public void onMetadataUpdated() {
			}

			@Override
			public void onQueueStatusUpdated() {
			}

			@Override
			public void onPreloadStatusUpdated() {
			}

			@Override
			public void onSendingRemoteMediaRequest() {
			}

			@Override
			public void onAdBreakStatusUpdated() {
			}
		});
		remoteMediaClient.load(buildMediaInfo(), autoPlay, position);
	}

	private MediaInfo buildMediaInfo() {
		MediaMetadata audioMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MUSIC_TRACK);

		if (metadataFormatter != null) {
			audioMetadata.putString(MediaMetadata.KEY_TITLE, metadataFormatter.getAudioTitle(getFile()));
			audioMetadata.putString(MediaMetadata.KEY_ARTIST, metadataFormatter.getAudioArtist());
			audioMetadata.putString(MediaMetadata.KEY_ALBUM_TITLE, metadataFormatter.getAudioAlbum());
		} else {
			audioMetadata.putString(MediaMetadata.KEY_TITLE, getFile().getNameOnly());
		}

		audioMetadata.addImage(new WebImage(Uri.parse("http://alpha.amahi.org/cast/audio-play.jpg")));

		String audioSource = serverClient.getFileUri(getShare(), getFile()).toString();
		MediaInfo.Builder builder = new MediaInfo.Builder(audioSource)
				.setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
				.setContentType(getFile().getMime())
				.setMetadata(audioMetadata);
		if (metadataFormatter != null) {
			builder.setStreamDuration(metadataFormatter.getDuration());
		}
		return builder.build();
	}
}
