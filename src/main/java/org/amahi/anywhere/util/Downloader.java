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
import android.support.v4.content.FileProvider;
import android.util.Log;

import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.FileDownloadFailedEvent;
import org.amahi.anywhere.bus.FileDownloadedEvent;
import org.amahi.anywhere.model.FileOption;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * File downloader. Uses system {@link android.app.DownloadManager}
 * for downloads placing and cancelling.
 */
@Singleton
public class Downloader extends BroadcastReceiver {
    public static final String OFFLINE_PATH = "offline_files";
    private final Context context;
    private long downloadId;
    private DownloadCallbacks downloadCallbacks;
    private ProgressTask downloadProgressTask;

    @Inject
    public Downloader(Context context) {
        this.context = context.getApplicationContext();
        this.downloadId = Integer.MIN_VALUE;
    }

    public void startFileDownloading(Uri fileUri, String fileName, @FileOption.Types int fileOption) {
        setUpDownloadReceiver();

        startDownloading(fileUri, fileName, fileOption);
    }

    private void setUpDownloadReceiver() {
        IntentFilter downloadActionsFilter = new IntentFilter();
        downloadActionsFilter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);

        context.registerReceiver(this, downloadActionsFilter);
    }

    private void startDownloading(Uri downloadUri, String downloadName, @FileOption.Types int fileOption) {
        File file;
        DownloadManager.Request downloadRequest = new DownloadManager.Request(downloadUri);

        if (fileOption == FileOption.DOWNLOAD) {
            // download in public directory
            file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), downloadName);
            downloadRequest.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, downloadName);
        } else {
            // download in App directory
            file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), downloadName);
            downloadRequest.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, downloadName);
        }

        //code to delete the file if it already exists
        if (file.exists())
            file.delete();

        downloadRequest.setVisibleInDownloadsUi(true)
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

    public long startDownloadingForOfflineMode(Uri downloadUri, String downloadName) {

        File file = new File(context.getExternalFilesDir(OFFLINE_PATH), downloadName);
        if (file.exists())
            file.delete();

        DownloadManager.Request downloadRequest = new DownloadManager.Request(downloadUri);
        downloadRequest.setDestinationInExternalFilesDir(context, OFFLINE_PATH, downloadName)
            .setVisibleInDownloadsUi(false)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);

        long id = getDownloadManager(context).enqueue(downloadRequest);
        downloadCallbacks.downloadStarted((int) id, downloadName);
        startProgressCount(id);
        return id;
    }

    private void startProgressCount(long id) {
        if (downloadProgressTask != null) {
            downloadProgressTask.shutDown();
        }
        downloadProgressTask = new ProgressTask(id);
        Thread thread = new Thread(downloadProgressTask);
        thread.start();
    }

    public void resumeProgressCount(long id) {
        startProgressCount(id);
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

            if (downloadUri.substring(0, 7).matches("file://")) {
                downloadUri = downloadUri.substring(7);
            }

            try {
                URI uri = new URI(downloadUri);
                downloadUri = uri.getPath();
            } catch (URISyntaxException e) {
                Log.e("Downloader", "Invalid Uri: " + downloadUri);
            }

            File file = new File(downloadUri);
            Uri contentUri = FileProvider.getUriForFile(context, "org.amahi.anywhere.fileprovider", file);
            BusProvider.getBus().post(new FileDownloadedEvent(contentUri));
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

    public void setDownloadCallbacks(DownloadCallbacks downloadCallbacks) {
        this.downloadCallbacks = downloadCallbacks;
    }

    private boolean isNetworkAvailable() {
        NetworkUtils networkUtils = new NetworkUtils(context);
        return networkUtils.isNetworkAvailable();
    }

    public interface DownloadCallbacks {
        void downloadStarted(int id, String fileName);

        void downloadProgress(int id, int progress);

        void downloadSuccess(long id);

        void downloadError(long id);

        void downloadPaused(long id, int progress);
    }

    private class ProgressTask implements Runnable {

        private boolean isDownloading;
        private long id;

        ProgressTask(long id) {
            this.id = id;
        }

        @Override
        public void run() {
            isDownloading = true;
            while (isDownloading) {
                DownloadManager.Query q = new DownloadManager.Query();
                q.setFilterById(id);
                Cursor cursor = getDownloadManager(context).query(q);
                if (cursor != null && cursor.moveToFirst()) {
                    cursor.moveToFirst();
                    long bytes_downloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                    long bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                    long progress = bytes_total != 0 ? (100 * bytes_downloaded) / bytes_total : 0;
                    downloadCallbacks.downloadProgress((int) id, (int) progress);

                    int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                    switch (status) {

                        case DownloadManager.STATUS_PAUSED:
                            if (!(isNetworkAvailable())) {
                                isDownloading = false;
                                downloadCallbacks.downloadPaused(id, (int) progress);
                            } else {
                                downloadCallbacks.downloadError(id);
                                isDownloading = false;
                            }

                            break;
                        case DownloadManager.STATUS_SUCCESSFUL:
                            isDownloading = false;
                            downloadCallbacks.downloadSuccess((int) id);
                            break;
                        case DownloadManager.STATUS_FAILED:
                            isDownloading = false;
                            downloadCallbacks.downloadError((int) id);
                            break;
                    }

                    cursor.close();
                } else {
                    isDownloading = false;
                }
            }
        }

        void shutDown() {
            isDownloading = false;
        }
    }

}
