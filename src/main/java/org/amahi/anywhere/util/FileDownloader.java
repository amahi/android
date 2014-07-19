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

import android.content.Context;
import android.net.Uri;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import okio.Buffer;

public final class FileDownloader
{
	private final File downloadDirectory;

	public FileDownloader(Context context) {
		this.downloadDirectory = context.getExternalCacheDir();
	}

	public Uri download(Uri remoteFileUri, String localFileName) {
		File localFile = new File(downloadDirectory, localFileName);

		InputStream remoteFileStream = buildRemoteFileStream(remoteFileUri);
		OutputStream localFileStream = buildLocalFileStream(localFile);

		download(remoteFileStream, localFileStream);

		return Uri.fromFile(localFile);
	}

	private InputStream buildRemoteFileStream(Uri remoteFileUri) {
		try {
			URL remoteFileUrl = new URL(remoteFileUri.toString());

			Request fileRequest = new Request.Builder()
				.url(remoteFileUrl)
				.build();

			Response fileResponse = new OkHttpClient()
				.newCall(fileRequest)
				.execute();

			return fileResponse.body().byteStream();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private OutputStream buildLocalFileStream(File localFile) {
		try {
			return new FileOutputStream(localFile);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void download(InputStream remoteFileStream, OutputStream localFileStream) {
		try {
			Buffer buffer = new Buffer();

			buffer.readFrom(remoteFileStream);
			buffer.writeTo(localFileStream);

			buffer.flush();

			localFileStream.flush();
			localFileStream.close();

			remoteFileStream.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
