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
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;

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
public class ServerFileAudioActivity extends AppCompatActivity implements ServiceConnection, MediaController.MediaPlayerControl
{
	private static final Set<String> SUPPORTED_FORMATS;

	static {
		SUPPORTED_FORMATS = new HashSet<String>(Arrays.asList(
			"audio/flac",
			"audio/mp4",
			"audio/mpeg",
			"audio/ogg"
		));
	}

	public static boolean supports(String mime_type) {
		return SUPPORTED_FORMATS.contains(mime_type);
	}

	private static final class State
	{
		private State() {
		}

		public static final String AUDIO_TITLE = "audio_title";
		public static final String AUDIO_SUBTITLE = "audio_subtitle";
		public static final String AUDIO_ALBUM_ART = "audio_album_art";
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

		setUpAudio(savedInstanceState);
	}

	private void setUpInjections() {
		AmahiApplication.from(this).inject(this);
	}

	private void setUpHomeNavigation() {
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setIcon(R.drawable.ic_launcher);
	}

	private void setUpAudio(Bundle state) {
		setUpAudioFile();
		setUpAudioTitle();
		setUpAudioMetadata(state);
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

	private void setUpAudioMetadata(Bundle state) {
		if (isAudioMetadataStateValid(state)) {
			setUpAudioMetadataState(state);

			showAudioMetadata();
		}
	}

	private boolean isAudioMetadataStateValid(Bundle state) {
		return (state != null) && state.containsKey(State.AUDIO_TITLE);
	}

	private void setUpAudioMetadataState(Bundle state) {
		getAudioTitleView().setText(state.getString(State.AUDIO_TITLE));
		getAudioSubtitleView().setText(state.getString(State.AUDIO_SUBTITLE));
		getAudioAlbumArtView().setImageBitmap((Bitmap) state.getParcelable(State.AUDIO_ALBUM_ART));
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
		AudioMetadataFormatter audioMetadataFormatter = new AudioMetadataFormatter(
			event.getAudioTitle(), event.getAudioArtist(), event.getAudioAlbum());

		setUpAudioMetadata(audioMetadataFormatter, event.getAudioAlbumArt());
	}

	private void setUpAudioMetadata(AudioMetadataFormatter audioMetadataFormatter, Bitmap audioAlbumArt) {
		getAudioTitleView().setText(audioMetadataFormatter.getAudioTitle(audioFile));
		getAudioSubtitleView().setText(audioMetadataFormatter.getAudioSubtitle(getShare()));
		getAudioAlbumArtView().setImageBitmap(audioAlbumArt);
	}

	private ServerShare getShare() {
		return getIntent().getParcelableExtra(Intents.Extras.SERVER_SHARE);
	}

	@Override
	protected void onStart() {
		super.onStart();

		setUpAudioService();
		setUpAudioServiceBind();
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
			audioControls.showAnimated();
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
			audioControls.hideAnimated();
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

		showAudioControlsForced();

		BusProvider.getBus().register(this);

		setUpAudioMetadata();
	}

	private void showAudioControlsForced() {
		if (areAudioControlsAvailable() && !audioControls.isShowing()) {
			audioControls.show();
		}
	}

	private void setUpAudioMetadata() {
		if (!isAudioServiceAvailable()) {
			return;
		}

		if (!this.audioFile.equals(audioService.getAudioFile())) {
			this.audioFile = audioService.getAudioFile();

			tearDownAudioTitle();
			tearDownAudioMetadata();

			setUpAudioTitle();
			setUpAudioMetadata(audioService.getAudioMetadataFormatter(), audioService.getAudioAlbumArt());
		}
	}

	private boolean isAudioServiceAvailable() {
		return audioService != null;
	}

	@Override
	protected void onPause() {
		super.onPause();

		hideAudioControlsForced();

		BusProvider.getBus().unregister(this);

		if (isFinishing()) {
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

		tearDownAudioServiceBind();
	}

	private void tearDownAudioServiceBind() {
		unbindService(this);
	}

	@Override
	protected void onSaveInstanceState(Bundle state) {
		super.onSaveInstanceState(state);

		if (isAudioMetadataLoaded()) {
			tearDownAudioMetadataState(state);
		}
	}

	private boolean isAudioMetadataLoaded() {
		String audioTitle = getAudioTitleView().getText().toString();
		String audioSubtitle = getAudioSubtitleView().getText().toString();
		BitmapDrawable audioAlbumArt = (BitmapDrawable) getAudioAlbumArtView().getDrawable();

		return !audioTitle.isEmpty() && !audioSubtitle.isEmpty() && (audioAlbumArt != null);
	}

	private void tearDownAudioMetadataState(Bundle state) {
		String audioTitle = getAudioTitleView().getText().toString();
		String audioSubtitle = getAudioSubtitleView().getText().toString();
		BitmapDrawable audioAlbumArt = (BitmapDrawable) getAudioAlbumArtView().getDrawable();

		state.putString(State.AUDIO_TITLE, audioTitle);
		state.putString(State.AUDIO_SUBTITLE, audioSubtitle);
		state.putParcelable(State.AUDIO_ALBUM_ART, audioAlbumArt.getBitmap());
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (isFinishing()) {
			tearDownAudioService();
		}
	}

	private void tearDownAudioService() {
		Intent intent = new Intent(this, AudioService.class);
		stopService(intent);
	}

	private static final class AudioControlsNextListener implements View.OnClickListener
	{
		@Override
		public void onClick(View view) {
			BusProvider.getBus().post(new AudioControlNextEvent());
		}
	}

	private static final class AudioControlsPreviousListener implements View.OnClickListener
	{
		@Override
		public void onClick(View view) {
			BusProvider.getBus().post(new AudioControlPreviousEvent());
		}
	}
}
