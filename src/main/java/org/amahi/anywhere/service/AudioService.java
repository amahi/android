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

import org.amahi.anywhere.R;
import org.amahi.anywhere.bus.AudioControlPauseEvent;
import org.amahi.anywhere.bus.AudioControlPlayEvent;
import org.amahi.anywhere.bus.AudioControlPlayPauseEvent;
import org.amahi.anywhere.bus.AudioMetadataRetrievedEvent;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.receiver.AudioReceiver;
import org.amahi.anywhere.task.AudioMetadataRetrievingTask;

import java.io.IOException;

public class AudioService extends Service implements MediaPlayer.OnCompletionListener, AudioManager.OnAudioFocusChangeListener
{
	private static final int AUDIO_PLAYER_NOTIFICATION = 42;

	private MediaPlayer audioPlayer;
	private RemoteControlClient audioPlayerRemote;

	private Uri audioSource;

	@Override
	public IBinder onBind(Intent intent) {
		return new AudioServiceBinder(this);
	}

	@Override
	public void onCreate() {
		super.onCreate();

		setUpBus();

		setUpAudioPlayer();
		setUpAudioPlayerRemote();
	}

	private void setUpBus() {
		BusProvider.getBus().register(this);
	}

	private void setUpAudioPlayer() {
		audioPlayer = new MediaPlayer();

		audioPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		audioPlayer.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);

		audioPlayer.setOnCompletionListener(this);
	}

	private void setUpAudioPlayerRemote() {
		AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		ComponentName audioReceiver = new ComponentName(getPackageName(), AudioReceiver.class.getName());

		Intent audioIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
		audioIntent.setComponent(audioReceiver);
		PendingIntent audioPendingIntent = PendingIntent.getBroadcast(this, 0, audioIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		audioPlayerRemote = new RemoteControlClient(audioPendingIntent);
		audioPlayerRemote.setTransportControlFlags(RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE);
		audioPlayerRemote.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);

		audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
		audioManager.registerMediaButtonEventReceiver(audioReceiver);
		audioManager.registerRemoteControlClient(audioPlayerRemote);
	}

	public boolean isAudioStarted() {
		return audioSource != null;
	}

	public boolean isAudioPlaying() {
		try {
			return isAudioStarted() && audioPlayer.isPlaying();
		} catch (IllegalStateException e) {
			return false;
		}
	}

	public void startAudio(Uri audioUri, MediaPlayer.OnPreparedListener audioListener) {
		try {
			audioSource = audioUri;

			audioPlayer.setDataSource(this, audioUri);
			audioPlayer.setOnPreparedListener(audioListener);
			audioPlayer.prepareAsync();

			setUpAudioMetadata();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void setUpAudioMetadata() {
		AudioMetadataRetrievingTask.execute(audioSource);
	}

	@Subscribe
	public void onAudioMetadataRetrieved(AudioMetadataRetrievedEvent event) {
		setUpAudioPlayerRemote(event.getAudioTitle(), event.getAudioArtist(), event.getAudioAlbum(), event.getAudioAlbumArt());
		setUpAudioPlayerNotification(event.getAudioTitle(), event.getAudioArtist(), event.getAudioAlbum(), event.getAudioAlbumArt());
	}

	private void setUpAudioPlayerRemote(String audioTitle, String audioArtist, String audioAlbum, Bitmap audioAlbumArt) {
		audioPlayerRemote
			.editMetadata(true)
			.putString(MediaMetadataRetriever.METADATA_KEY_TITLE, audioTitle)
			.putString(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST, audioArtist)
			.putString(MediaMetadataRetriever.METADATA_KEY_ALBUM, audioAlbum)
			.putBitmap(RemoteControlClient.MetadataEditor.BITMAP_KEY_ARTWORK, getAudioPlayerRemoteArtwork(audioAlbumArt))
			.apply();
	}

	private Bitmap getAudioPlayerRemoteArtwork(Bitmap audioAlbumArt) {
		Bitmap.Config artworkConfig = audioAlbumArt.getConfig();

		if (artworkConfig == null) {
			artworkConfig = Bitmap.Config.ARGB_8888;
		}

		return audioAlbumArt.copy(artworkConfig, false);
	}

	private void setUpAudioPlayerNotification(String audioTitle, String audioArtist, String audioAlbum, Bitmap audioAlbumArt) {
		Notification notification = new NotificationCompat.Builder(this)
			.setContentTitle(getAudioPlayerNotificationTitle(audioTitle))
			.setContentText(getAudioPlayerNotificationText(audioArtist, audioAlbum))
			.setSmallIcon(getAudioPlayerNotificationSmallIcon())
			.setLargeIcon(getAudioPlayerNotificationLargeIcon(audioAlbumArt))
			.setOngoing(true)
			.setWhen(0)
			.build();

		startForeground(AUDIO_PLAYER_NOTIFICATION, notification);
	}

	private String getAudioPlayerNotificationTitle(String audioTitle) {
		return audioTitle;
	}

	private String getAudioPlayerNotificationText(String audioArtist, String audioAlbum) {
		return String.format("%s - %s", audioArtist, audioAlbum);
	}

	private int getAudioPlayerNotificationSmallIcon() {
		return R.drawable.ic_notification_audio;
	}

	private Bitmap getAudioPlayerNotificationLargeIcon(Bitmap audioAlbumArt) {
		int iconHeight = (int) getResources().getDimension(android.R.dimen.notification_large_icon_height);
		int iconWidth = (int) getResources().getDimension(android.R.dimen.notification_large_icon_width);

		return Bitmap.createScaledBitmap(audioAlbumArt, iconWidth, iconHeight, false);
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

	public void playAudio() {
		audioPlayer.start();

		audioPlayerRemote.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
	}

	public void pauseAudio() {
		audioPlayer.pause();

		audioPlayerRemote.setPlaybackState(RemoteControlClient.PLAYSTATE_PAUSED);
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
				tearDownVolume();
				break;

			default:
				break;
		}
	}

	private void setUpAudioVolume() {
		audioPlayer.setVolume(1.0f, 1.0f);
	}

	private void tearDownVolume() {
		audioPlayer.setVolume(0.3f, 0.3f);
	}

	@Override
	public void onCompletion(MediaPlayer audioPlayer) {
		pauseAudio();
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

	private void tearDownAudioPlayerNotification() {
		stopForeground(true);
	}

	private void tearDownAudioPlayerRemote() {
		AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		ComponentName audioReceiver = new ComponentName(getPackageName(), AudioReceiver.class.getName());

		audioManager.unregisterMediaButtonEventReceiver(audioReceiver);
		audioManager.unregisterRemoteControlClient(audioPlayerRemote);
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
