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

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;

import java.io.IOException;

public class AudioService extends Service
{
	private MediaPlayer audioPlayer;

	private Uri audioSource;

	@Override
	public IBinder onBind(Intent intent) {
		return new AudioServiceBinder(this);
	}

	@Override
	public void onCreate() {
		super.onCreate();

		setUpAudioPlayer();
	}

	private void setUpAudioPlayer() {
		audioPlayer = new MediaPlayer();

		audioPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		audioPlayer.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
	}

	public boolean isStarted() {
		return audioSource != null;
	}

	public void startAudio(Uri audioUri, MediaPlayer.OnPreparedListener audioListener) {
		try {
			audioSource = audioUri;

			audioPlayer.setDataSource(this, audioUri);
			audioPlayer.setOnPreparedListener(audioListener);
			audioPlayer.prepareAsync();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public MediaPlayer getAudioPlayer() {
		return audioPlayer;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		tearDownAudioPlayer();
	}

	private void tearDownAudioPlayer() {
		audioPlayer.reset();
		audioPlayer.release();
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
