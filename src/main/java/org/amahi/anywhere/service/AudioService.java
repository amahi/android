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

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.RemoteControlClient;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;

import com.squareup.otto.Subscribe;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.R;
import org.amahi.anywhere.bus.AudioCompletedEvent;
import org.amahi.anywhere.bus.AudioControlNextEvent;
import org.amahi.anywhere.bus.AudioControlPauseEvent;
import org.amahi.anywhere.bus.AudioControlPlayEvent;
import org.amahi.anywhere.bus.AudioControlPlayPauseEvent;
import org.amahi.anywhere.bus.AudioControlPreviousEvent;
import org.amahi.anywhere.bus.AudioMetadataRetrievedEvent;
import org.amahi.anywhere.bus.AudioPreparedEvent;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.receiver.AudioReceiver;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerShare;
import org.amahi.anywhere.task.AudioMetadataRetrievingTask;
import org.amahi.anywhere.util.AudioMetadataFormatter;
import org.amahi.anywhere.util.Intents;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

public class AudioService extends Service implements AudioManager.OnAudioFocusChangeListener,
	MediaPlayer.OnPreparedListener,
	MediaPlayer.OnCompletionListener,
	MediaPlayer.OnErrorListener
{
	private static final class Notifications
	{
		private Notifications() {
		}

		public static final int AUDIO_PLAYER = 42;
	}

	private MediaPlayer audioPlayer;
	private RemoteControlClient audioPlayerRemote;

	private ServerShare audioShare;
	private List<ServerFile> audioFiles;
	private ServerFile audioFile;

	private AudioMetadataFormatter audioMetadataFormatter;
	private Bitmap audioAlbumArt;

	@Inject
	ServerClient serverClient;

	@Override
	public IBinder onBind(Intent intent) {
		return new AudioServiceBinder(this);
	}

	@Override
	public void onCreate() {
		super.onCreate();

		setUpInjections();

		setUpBus();

		setUpAudioPlayer();
		setUpAudioPlayerRemote();
	}

	private void setUpInjections() {
		AmahiApplication.from(this).inject(this);
	}

	private void setUpBus() {
		BusProvider.getBus().register(this);
	}

	private void setUpAudioPlayer() {
		audioPlayer = new MediaPlayer();

		audioPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		audioPlayer.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);

		audioPlayer.setOnCompletionListener(this);
		audioPlayer.setOnErrorListener(this);
	}

	private void setUpAudioPlayerRemote() {
		AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		ComponentName audioReceiver = new ComponentName(getPackageName(), AudioReceiver.class.getName());

		Intent audioIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
		audioIntent.setComponent(audioReceiver);
		PendingIntent audioPendingIntent = PendingIntent.getBroadcast(this, 0, audioIntent, 0);

		audioPlayerRemote = new RemoteControlClient(audioPendingIntent);
		audioPlayerRemote.setTransportControlFlags(
			RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE |
			RemoteControlClient.FLAG_KEY_MEDIA_NEXT |
			RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS);
		audioPlayerRemote.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);

		audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
		audioManager.registerMediaButtonEventReceiver(audioReceiver);
		audioManager.registerRemoteControlClient(audioPlayerRemote);
	}

	public boolean isAudioStarted() {
		return (audioShare != null) && (audioFiles != null) && (audioFile != null);
	}

	public void startAudio(ServerShare audioShare, List<ServerFile> audioFiles, ServerFile audioFile) {
		this.audioShare = audioShare;
		this.audioFiles = audioFiles;
		this.audioFile = audioFile;

		setUpAudioPlayback();
		setUpAudioMetadata();
	}

	private void setUpAudioPlayback() {
		try {
			audioPlayer.setDataSource(this, getAudioUri());
			audioPlayer.setOnPreparedListener(this);
			audioPlayer.prepareAsync();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private Uri getAudioUri() {
		return serverClient.getFileUri(audioShare, audioFile);
	}

	@Override
	public void onPrepared(MediaPlayer audioPlayer) {
		BusProvider.getBus().post(new AudioPreparedEvent());

		playAudio();
	}

	private void setUpAudioMetadata() {
		AudioMetadataRetrievingTask.execute(getAudioUri());
	}

	@Subscribe
	public void onAudioMetadataRetrieved(AudioMetadataRetrievedEvent event) {
		this.audioMetadataFormatter = new AudioMetadataFormatter(
			event.getAudioTitle(), event.getAudioArtist(), event.getAudioAlbum());
		this.audioAlbumArt = event.getAudioAlbumArt();

		setUpAudioPlayerRemote(audioMetadataFormatter, audioAlbumArt);
		setUpAudioPlayerNotification(audioMetadataFormatter, audioAlbumArt);
	}

	private void setUpAudioPlayerRemote(AudioMetadataFormatter audioMetadataFormatter, Bitmap audioAlbumArt) {
		audioPlayerRemote
			.editMetadata(true)
			.putString(MediaMetadataRetriever.METADATA_KEY_TITLE, audioMetadataFormatter.getAudioTitle(audioFile))
			.putString(MediaMetadataRetriever.METADATA_KEY_ALBUM, audioMetadataFormatter.getAudioSubtitle(audioShare))
			.putBitmap(RemoteControlClient.MetadataEditor.BITMAP_KEY_ARTWORK, getAudioPlayerRemoteArtwork(audioAlbumArt))
			.apply();
	}

	private Bitmap getAudioPlayerRemoteArtwork(Bitmap audioAlbumArt) {
		if (audioAlbumArt == null) {
			return null;
		}

		Bitmap.Config artworkConfig = audioAlbumArt.getConfig();

		if (artworkConfig == null) {
			artworkConfig = Bitmap.Config.ARGB_8888;
		}

		return audioAlbumArt.copy(artworkConfig, false);
	}

	private void setUpAudioPlayerNotification(AudioMetadataFormatter audioMetadataFormatter, Bitmap audioAlbumArt) {
		Intent audioIntent = Intents.Builder.with(this).buildServerFileIntent(audioShare, audioFiles, audioFile);
		PendingIntent audioPendingIntent = PendingIntent.getActivity(this, 0, audioIntent, 0);

		Notification notification = new NotificationCompat.Builder(this)
			.setContentTitle(audioMetadataFormatter.getAudioTitle(audioFile))
			.setContentText(audioMetadataFormatter.getAudioSubtitle(audioShare))
			.setSmallIcon(getAudioPlayerNotificationIcon())
			.setLargeIcon(getAudioPlayerNotificationArtwork(audioAlbumArt))
			.setOngoing(true)
			.setWhen(0)
			.setContentIntent(audioPendingIntent)
			.build();

		startForeground(Notifications.AUDIO_PLAYER, notification);
	}

	private int getAudioPlayerNotificationIcon() {
		return R.drawable.ic_notification_audio;
	}

	private Bitmap getAudioPlayerNotificationArtwork(Bitmap audioAlbumArt) {
		int iconHeight = (int) getResources().getDimension(android.R.dimen.notification_large_icon_height);
		int iconWidth = (int) getResources().getDimension(android.R.dimen.notification_large_icon_width);

		if (audioAlbumArt == null) {
			return null;
		}

		return Bitmap.createScaledBitmap(audioAlbumArt, iconWidth, iconHeight, false);
	}

	public ServerFile getAudioFile() {
		return audioFile;
	}

	public AudioMetadataFormatter getAudioMetadataFormatter() {
		return audioMetadataFormatter;
	}

	public Bitmap getAudioAlbumArt() {
		return audioAlbumArt;
	}

	public MediaPlayer getAudioPlayer() {
		return audioPlayer;
	}

	@Subscribe
	public void onAudioControlPlayPause(AudioControlPlayPauseEvent event) {
		if (audioPlayer.isPlaying()) {
			pauseAudio();
		} else {
			playAudio();
		}
	}

	@Subscribe
	public void onAudioControlPlay(AudioControlPlayEvent event) {
		playAudio();
	}

	@Subscribe
	public void onAudioControlPause(AudioControlPauseEvent event) {
		pauseAudio();
	}

	@Subscribe
	public void onAudioControlNext(AudioControlNextEvent event) {
		startNextAudio();
	}

	@Subscribe
	public void onAudioControlPrevious(AudioControlPreviousEvent event) {
		startPreviousAudio();
	}

	public void playAudio() {
		audioPlayer.start();

		audioPlayerRemote.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
	}

	public void pauseAudio() {
		audioPlayer.pause();

		audioPlayerRemote.setPlaybackState(RemoteControlClient.PLAYSTATE_PAUSED);
	}

	private void startNextAudio() {
		this.audioFile = getNextAudioFile();

		tearDownAudioPlayback();

		setUpAudioPlayback();
		setUpAudioMetadata();
	}

	private ServerFile getNextAudioFile() {
		int currentAudioFilePosition = audioFiles.indexOf(audioFile);

		if (currentAudioFilePosition == audioFiles.size() - 1) {
			return audioFiles.get(0);
		}

		return audioFiles.get(currentAudioFilePosition + 1);
	}

	private void tearDownAudioPlayback() {
		audioPlayer.reset();
	}

	private void startPreviousAudio() {
		this.audioFile = getPreviousAudioFile();

		tearDownAudioPlayback();

		setUpAudioPlayback();
		setUpAudioMetadata();
	}

	private ServerFile getPreviousAudioFile() {
		int currentAudioFilePosition = audioFiles.indexOf(audioFile);

		if (currentAudioFilePosition == 0) {
			return audioFiles.get(audioFiles.size() - 1);
		}

		return audioFiles.get(currentAudioFilePosition - 1);
	}

	@Override
	public void onAudioFocusChange(int audioFocus) {
		switch (audioFocus) {
			case AudioManager.AUDIOFOCUS_GAIN:
				if (isAudioPlaying()) {
					setUpAudioVolume();
				} else {
					playAudio();
				}
				break;

			case AudioManager.AUDIOFOCUS_LOSS:
			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
				if (isAudioPlaying()) {
					pauseAudio();
				}
				break;

			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
				tearDownAudioVolume();
				break;

			default:
				break;
		}
	}

	private boolean isAudioPlaying() {
		try {
			return isAudioStarted() && audioPlayer.isPlaying();
		} catch (IllegalStateException e) {
			return false;
		}
	}

	private void setUpAudioVolume() {
		audioPlayer.setVolume(1.0f, 1.0f);
	}

	private void tearDownAudioVolume() {
		audioPlayer.setVolume(0.3f, 0.3f);
	}

	@Override
	public void onCompletion(MediaPlayer audioPlayer) {
		BusProvider.getBus().post(new AudioCompletedEvent());

		startNextAudio();
	}

	@Override
	public boolean onError(MediaPlayer audioPlayer, int errorReason, int errorExtra) {
		return true;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		tearDownBus();

		tearDownAudioPlayer();
		tearDownAudioPlayerRemote();
		tearDownAudioPlayerNotification();
	}

	private void tearDownBus() {
		BusProvider.getBus().unregister(this);
	}

	private void tearDownAudioPlayer() {
		audioPlayer.reset();
		audioPlayer.release();
	}

	private void tearDownAudioPlayerRemote() {
		AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		ComponentName audioReceiver = new ComponentName(getPackageName(), AudioReceiver.class.getName());

		audioManager.unregisterMediaButtonEventReceiver(audioReceiver);
		audioManager.unregisterRemoteControlClient(audioPlayerRemote);
	}

	private void tearDownAudioPlayerNotification() {
		stopForeground(true);
	}

	public static final class AudioServiceBinder extends Binder
	{
		private final AudioService audioService;

		public AudioServiceBinder(AudioService audioService) {
			this.audioService = audioService;
		}

		public AudioService getAudioService() {
			return audioService;
		}
	}
}
