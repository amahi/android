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
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.FileProvider;

import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.FileDownloadFailedEvent;
import org.amahi.anywhere.bus.FileDownloadedEvent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * File downloader. Uses system {@link android.app.DownloadManager}
 * for downloads placing and cancelling.
 */
@Singleton
public class Downloader extends BroadcastReceiver {
    private final Context context;

    private long downloadId;
    private int lastProgress = 0;

    private DownloadCallbacks downloadCallbacks;

    @Inject
    public Downloader(Context context) {
        this.context = context.getApplicationContext();
        this.downloadId = Integer.MIN_VALUE;
    }

    public void startFileDownloading(Uri fileUri, String fileName, boolean saveInExternal) {
        setUpDownloadReceiver();

        startDownloading(fileUri, fileName, saveInExternal);
    }

    private void setUpDownloadReceiver() {
        IntentFilter downloadActionsFilter = new IntentFilter();
        downloadActionsFilter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);

        context.registerReceiver(this, downloadActionsFilter);
    }

    private void startDownloading(Uri downloadUri, String downloadName, boolean saveInExternal) {
        File file;
        DownloadManager.Request downloadRequest = new DownloadManager.Request(downloadUri);
        if (saveInExternal) {

            file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + downloadName);

            downloadRequest.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, downloadName);
        } else {

            file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + "/" + downloadName);

            downloadRequest.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, downloadName);
        }

        //code to delete the file if it already exists
        if (file.exists())
            file.delete();

        downloadRequest.setVisibleInDownloadsUi(false)
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

        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), downloadName);
        if (file.exists())
            file.delete();

        DownloadManager.Request downloadRequest = new DownloadManager.Request(downloadUri);
        downloadRequest.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, downloadName)
            .setVisibleInDownloadsUi(false)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);

        this.downloadId = getDownloadManager(context).enqueue(downloadRequest);
        startProgressCount();
        downloadCallbacks.downloadStarted((int) downloadId, downloadName);
        return downloadId;
    }

    private void startProgressCount() {

        new Thread(() -> {
            boolean downloading = true;
            while (downloading) {
                DownloadManager.Query q = new DownloadManager.Query();
                q.setFilterById(downloadId);
                Cursor cursor = getDownloadManager(context).query(q);
                if (cursor != null && cursor.moveToFirst()) {
                    cursor.moveToFirst();
                    int bytes_downloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                    int bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));

                    int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        downloading = false;
                        downloadCallbacks.downloadSuccess((int) downloadId);
                    } else if (status == DownloadManager.STATUS_FAILED) {
                        downloading = false;
                        downloadCallbacks.downloadError((int) downloadId);
                    }
                    cursor.close();

                    Handler handler = new Handler(Looper.getMainLooper());
                    // update progress on UI thread
                    handler.post(new ProgressUpdater(bytes_downloaded, bytes_total));
                } else {
                    downloading = false;
                }
            }
        }).start();
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

    public void moveFileToInternalStorage(String fileName) {
        File sourceLocation = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + fileName);

        File targetLocation = new File(context.getFilesDir(), fileName);

        if (sourceLocation.exists()) {

            InputStream in;
            try {
                in = new FileInputStream(sourceLocation);
                OutputStream out = new FileOutputStream(targetLocation);

                byte[] buf = new byte[1024];
                int len;

                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }

                in.close();
                out.close();

                if (sourceLocation.exists())
                    sourceLocation.delete();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public void setDownloadCallbacks(DownloadCallbacks downloadCallbacks) {
        this.downloadCallbacks = downloadCallbacks;
    }

    public interface DownloadCallbacks {
        void downloadStarted(int id, String fileName);

        void downloadProgress(int id, int progress);

        void downloadSuccess(long id);

        void downloadError(long id);
    }

    private class ProgressUpdater implements Runnable {
        private long mDownloaded;
        private long mTotal;

        ProgressUpdater(long downloaded, long total) {
            mDownloaded = downloaded;
            mTotal = total;
        }

        @Override
        public void run() {
            int progress = (int) (100 * mDownloaded / mTotal);
            if (lastProgress != progress) {
                lastProgress = progress;
                downloadCallbacks.downloadProgress((int) downloadId, progress);
            }
        }
    }
}
