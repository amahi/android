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

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import org.amahi.anywhere.bus.BusEvent;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.FileDownloadedEvent;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.util.Downloader;

public class FileDownloadingTask extends AsyncTask<Void, Void, BusEvent>
{
	private final Downloader downloader;

	private final ServerFile file;
	private final Uri fileUri;

	public static void execute(Context context, ServerFile file, Uri fileUri) {
		new FileDownloadingTask(context, file, fileUri).execute();
	}

	public FileDownloadingTask(Context context, ServerFile file, Uri fileUri) {
		this.downloader = new Downloader(context);

		this.file = file;
		this.fileUri = fileUri;
	}

	@Override
	protected BusEvent doInBackground(Void... parameters) {
		return new FileDownloadedEvent(file, downloader.download(fileUri, file.getName()));
	}

	@Override
	protected void onPostExecute(BusEvent busEvent) {
		super.onPostExecute(busEvent);

		BusProvider.getBus().post(busEvent);
	}
}
