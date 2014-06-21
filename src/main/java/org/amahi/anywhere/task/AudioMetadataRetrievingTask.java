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

package org.amahi.anywhere.task;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;

import org.amahi.anywhere.bus.AudioMetadataRetrievedEvent;
import org.amahi.anywhere.bus.BusEvent;
import org.amahi.anywhere.bus.BusProvider;

import java.util.HashMap;

public class AudioMetadataRetrievingTask extends AsyncTask<Void, Void, BusEvent>
{
	private final Uri audioUri;

	public static void execute(Uri audioUri) {
		new AudioMetadataRetrievingTask(audioUri).execute();
	}

	private AudioMetadataRetrievingTask(Uri audioUri) {
		this.audioUri = audioUri;
	}

	@Override
	protected BusEvent doInBackground(Void... parameters) {
		MediaMetadataRetriever audioMetadataRetriever = new MediaMetadataRetriever();

		audioMetadataRetriever.setDataSource(audioUri.toString(), new HashMap<String, String>());

		String audioTitle = audioMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
		String audioArtist = audioMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
		String audioAlbum = audioMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);

		byte[] audioAlbumArtBytes = audioMetadataRetriever.getEmbeddedPicture();
		Bitmap audioAlbumArt = BitmapFactory.decodeByteArray(audioAlbumArtBytes, 0, audioAlbumArtBytes.length);

		audioMetadataRetriever.release();

		return new AudioMetadataRetrievedEvent(audioTitle, audioArtist, audioAlbum, audioAlbumArt);
	}

	@Override
	protected void onPostExecute(BusEvent busEvent) {
		super.onPostExecute(busEvent);

		BusProvider.getBus().post(busEvent);
	}
}
