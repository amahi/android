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

import android.os.Handler;
import android.os.Looper;

import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.ServerFileUploadProgressEvent;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;

/**
 * Extension of RequestBody {@link okhttp3.RequestBody} to provide progress callbacks
 * for file upload.
 */
public class ProgressRequestBody extends RequestBody {
	private int mId;
	private File mFile;
	private int lastProgress = 0;

	private static final int DEFAULT_BUFFER_SIZE = 2048;

	public ProgressRequestBody(int id, File file) {
		mId = id;
		mFile = new File(file.getPath());
	}

	@Override
	public MediaType contentType() {
		// Only for uploading images
		return MediaType.parse("image/*");
	}

	@Override
	public long contentLength() throws IOException {
		return mFile.length();
	}

	@Override
	public void writeTo(BufferedSink sink) throws IOException {
		long fileLength = mFile.length();
		byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
		FileInputStream in = new FileInputStream(mFile);
		long uploaded = 0;

		//noinspection TryFinallyCanBeTryWithResources
		try {
			int read;
			Handler handler = new Handler(Looper.getMainLooper());
			while ((read = in.read(buffer)) != -1) {

				uploaded += read;
				sink.write(buffer, 0, read);

				// update progress on UI thread
				handler.post(new ProgressUpdater(uploaded, fileLength));
			}
		} finally {
			in.close();
		}
	}

	private class ProgressUpdater implements Runnable {
		private long mUploaded;
		private long mTotal;

		ProgressUpdater(long uploaded, long total) {
			mUploaded = uploaded;
			mTotal = total;
		}

		@Override
		public void run() {
			int progress = (int) (100 * mUploaded / mTotal);
			if (lastProgress != progress) {
				lastProgress = progress;
				BusProvider.getBus().post(new ServerFileUploadProgressEvent(mId, progress));
			}
		}
	}
}
