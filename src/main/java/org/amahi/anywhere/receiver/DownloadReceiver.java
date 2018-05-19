package org.amahi.anywhere.receiver;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.amahi.anywhere.service.DownloadService;

public class DownloadReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        startDownloadService(context, intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0));
    }

    private void startDownloadService(Context context, long downloadId) {
        Intent downloadService = new Intent(context, DownloadService.class);
        downloadService.putExtra(DownloadManager.EXTRA_DOWNLOAD_ID, downloadId);
        downloadService.setAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        context.startService(downloadService);
    }
}
