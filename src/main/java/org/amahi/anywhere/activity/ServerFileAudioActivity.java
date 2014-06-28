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
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.ViewAnimator;

import com.squareup.otto.Subscribe;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.R;
import org.amahi.anywhere.bus.AudioMetadataRetrievedEvent;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerShare;
import org.amahi.anywhere.service.AudioService;
import org.amahi.anywhere.task.AudioMetadataRetrievingTask;
import org.amahi.anywhere.util.AudioMetadataFormatter;
import org.amahi.anywhere.util.Intents;
import org.amahi.anywhere.view.AudioController;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

public class ServerFileAudioActivity extends Activity implements ServiceConnection,
	MediaPlayer.OnPreparedListener,
	MediaController.MediaPlayerControl
{
	public static final Set<String> SUPPORTED_FORMATS;

	static {
		SUPPORTED_FORMATS = new HashSet<String>(Arrays.asList(
			"audio/flac",
			"audio/mp4",
			"audio/mpeg",
			"audio/ogg"
		));
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
	private AudioController audioControls;

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
		getActionBar().setHomeButtonEnabled(true);
	}

	private void setUpAudio(Bundle state) {
		setUpAudioTitle();
		setUpAudioMetadata(state);
	}

	private void setUpAudioTitle() {
		getActionBar().setTitle(getFile().getName());
	}

	private ServerFile getFile() {
		return getIntent().getParcelableExtra(Intents.Extras.SERVER_FILE);
	}

	private void setUpAudioMetadata(Bundle state) {
		if (isAudioMetadataStateValid(state)) {
			setUpAudioMetadataState(state);

			showAudioMetadata();
		} else {
			AudioMetadataRetrievingTask.execute(getAudioUri());
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
		ViewAnimator animator = (ViewAnimator) findViewById(R.id.animator);

		View content = findViewById(R.id.layout_content);

		if (animator.getDisplayedChild() != animator.indexOfChild(content)) {
			animator.setDisplayedChild(animator.indexOfChild(content));
		}
	}

	private Uri getAudioUri() {
		return serverClient.getFileUri(getShare(), getFile());
	}

	private ServerShare getShare() {
		return getIntent().getParcelableExtra(Intents.Extras.SERVER_SHARE);
	}

	@Subscribe
	public void onAudioMetadataRetrieved(AudioMetadataRetrievedEvent event) {
		AudioMetadataFormatter audioMetadataFormatter = new AudioMetadataFormatter(
			event.getAudioTitle(), event.getAudioArtist(), event.getAudioAlbum());

		setUpAudioMetadata(audioMetadataFormatter, event.getAudioAlbumArt());
	}

	private void setUpAudioMetadata(AudioMetadataFormatter audioMetadataFormatter, Bitmap audioAlbumArt) {
		getAudioTitleView().setText(audioMetadataFormatter.getAudioTitle(getFile()));
		getAudioSubtitleView().setText(audioMetadataFormatter.getAudioSubtitle(getShare()));
		getAudioAlbumArtView().setImageBitmap(audioAlbumArt);
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
			audioControls = new AudioController(this);

			audioControls.setMediaPlayer(this);
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
			audioService.startAudio(getShare(), getFile(), this);
		}
	}

	@Override
	public void onPrepared(MediaPlayer audioPlayer) {
		start();

		showAudio();
	}

	private void showAudio() {
		showAudioMetadata();
		showAudioControlsAnimated();
	}

	private void showAudioControlsAnimated() {
		if (areAudioControlsAvailable() && !audioControls.isShowing()) {
			Animation showAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_up_view);
			audioControls.startAnimation(showAnimation);

			showAudioControls();
		}
	}

	private void showAudioControls() {
		if (areAudioControlsAvailable() && !audioControls.isShowing()) {
			audioControls.show();
		}
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

		showAudioControls();

		BusProvider.getBus().register(this);
	}

	@Override
	protected void onPause() {
		super.onPause();

		hideAudioControls();

		BusProvider.getBus().unregister(this);

		if (isFinishing()) {
			tearDownAudioPlayback();
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

		tearDownAudioMetadataState(state);
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
}
