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

import android.media.MediaMetadataRetriever;
import android.net.Uri;

import com.squareup.picasso.Downloader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public class AudioAlbumArtDownloader implements Downloader
{
	@Override
	public Response load(Uri audioUri, boolean localCacheOnly) throws IOException {
		MediaMetadataRetriever audioMetadataRetriever = new MediaMetadataRetriever();

		audioMetadataRetriever.setDataSource(audioUri.toString(), new HashMap<String, String>());

		byte[] audioAlbumArtBytes = audioMetadataRetriever.getEmbeddedPicture();
		InputStream audioAlbumArtStream = new ByteArrayInputStream(audioAlbumArtBytes);

		audioMetadataRetriever.release();

		return new Response(audioAlbumArtStream, false);
	}
}
