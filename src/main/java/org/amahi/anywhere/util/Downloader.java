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

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;

import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.FileDownloadFailedEvent;
import org.amahi.anywhere.bus.FileDownloadedEvent;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * File downloader. Uses system {@link android.app.DownloadManager}
 * for downloads placing and cancelling.
 */
@Singleton
public class Downloader extends BroadcastReceiver
{
	private final Context context;

	private long downloadId;

	@Inject
	public Downloader(Context context) {
		this.context = context.getApplicationContext();

		this.downloadId = Integer.MIN_VALUE;
	}

	public void startFileDownloading(Uri fileUri, String fileName) {
		setUpDownloadReceiver();

		startDownloading(fileUri, fileName);
	}

	private void setUpDownloadReceiver() {
		IntentFilter downloadActionsFilter = new IntentFilter();
		downloadActionsFilter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);

		context.registerReceiver(this, downloadActionsFilter);
	}

	private void startDownloading(Uri downloadUri, String downloadName) {
		DownloadManager.Request downloadRequest = new DownloadManager.Request(downloadUri)
			.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, downloadName)
			.setVisibleInDownloadsUi(false)
			.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);

		this.downloadId = getDownloadManager(context).enqueue(downloadRequest);
	}

	private DownloadManager getDownloadManager(Context context) {
		return (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (isDownloadCurrent(intent)) {
			finishDownloading();

			tearDownDownloadReceiver();
		}
	}

	private boolean isDownloadCurrent(Intent intent) {
		return downloadId == intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
	}

	private void finishDownloading() {
		DownloadManager.Query downloadQuery = new DownloadManager.Query()
			.setFilterById(downloadId);

		Cursor downloadInformation = getDownloadManager(context).query(downloadQuery);

		downloadInformation.moveToFirst();

		int downloadStatus = downloadInformation.getInt(
			downloadInformation.getColumnIndex(DownloadManager.COLUMN_STATUS));

		if (downloadStatus == DownloadManager.STATUS_SUCCESSFUL) {
			String downloadUri = downloadInformation.getString(
				downloadInformation.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));

			BusProvider.getBus().post(new FileDownloadedEvent(Uri.parse(downloadUri)));
		} else {
			BusProvider.getBus().post(new FileDownloadFailedEvent());
		}

		downloadInformation.close();
	}

	private void tearDownDownloadReceiver() {
		try {
			context.unregisterReceiver(this);
		} catch (IllegalArgumentException e) {
			// False alarm, no need to unregister.
		}
	}

	public void finishFileDownloading() {
		getDownloadManager(context).remove(downloadId);

		tearDownDownloadReceiver();
	}
}
