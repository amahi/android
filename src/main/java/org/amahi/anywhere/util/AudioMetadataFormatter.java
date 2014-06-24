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

package org.amahi.anywhere.util;

import android.text.TextUtils;

import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerShare;

public final class AudioMetadataFormatter
{
	private final String audioTitle;
	private final String audioArtist;
	private final String audioAlbum;

	public AudioMetadataFormatter(String audioTitle, String audioArtist, String audioAlbum) {
		this.audioTitle = audioTitle;
		this.audioArtist = audioArtist;
		this.audioAlbum = audioAlbum;
	}

	public String getAudioTitle(ServerFile audioFile) {
		if (TextUtils.isEmpty(audioTitle)) {
			return audioFile.getName();
		}

		if (TextUtils.isEmpty(audioArtist) && TextUtils.isEmpty(audioAlbum)) {
			return audioFile.getName();
		}

		return audioTitle;
	}

	public String getAudioSubtitle(ServerShare audioShare) {
		if (TextUtils.isEmpty(audioTitle)) {
			return audioShare.getName();
		}

		if (TextUtils.isEmpty(audioArtist) && TextUtils.isEmpty(audioAlbum)) {
			return audioShare.getName();
		}

		if (TextUtils.isEmpty(audioArtist)) {
			return audioAlbum;
		}

		if (TextUtils.isEmpty(audioAlbum)) {
			return audioArtist;
		}

		return String.format("%s - %s", audioArtist, audioAlbum);
	}
}
