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

package org.amahi.anywhere.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.view.KeyEvent;

import org.amahi.anywhere.bus.AudioControlNextEvent;
import org.amahi.anywhere.bus.AudioControlPauseEvent;
import org.amahi.anywhere.bus.AudioControlPlayEvent;
import org.amahi.anywhere.bus.AudioControlPlayPauseEvent;
import org.amahi.anywhere.bus.AudioControlPreviousEvent;
import org.amahi.anywhere.bus.BusProvider;

public class AudioReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
			handleAudioChangeEvent();
		}

		if (intent.getAction().equals(Intent.ACTION_MEDIA_BUTTON)) {
			handleAudioControlEvent(intent);
		}
	}

	private void handleAudioChangeEvent() {
		BusProvider.getBus().post(new AudioControlPauseEvent());
	}

	private void handleAudioControlEvent(Intent intent) {
		KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);

		if (event.getAction() != KeyEvent.ACTION_DOWN) {
			return;
		}

		switch (event.getKeyCode()) {
			case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
				BusProvider.getBus().post(new AudioControlPlayPauseEvent());
				break;

			case KeyEvent.KEYCODE_MEDIA_PLAY:
				BusProvider.getBus().post(new AudioControlPlayEvent());
				break;

			case KeyEvent.KEYCODE_MEDIA_PAUSE:
				BusProvider.getBus().post(new AudioControlPauseEvent());
				break;

			case KeyEvent.KEYCODE_MEDIA_NEXT:
				BusProvider.getBus().post(new AudioControlNextEvent());
				break;

			case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
				BusProvider.getBus().post(new AudioControlPreviousEvent());
				break;

			default:
				break;
		}
	}
}
