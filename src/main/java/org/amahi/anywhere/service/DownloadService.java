package org.amahi.anywhere.service;

import android.app.DownloadManager;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.db.entities.OfflineFile;
import org.amahi.anywhere.db.repositories.OfflineFileRepository;
import org.amahi.anywhere.receiver.DownloadReceiver;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerShare;
import org.amahi.anywhere.util.Downloader;
import org.amahi.anywhere.util.Intents;

import javax.inject.Inject;

public class DownloadService extends Service {

    @Inject
    ServerClient serverClient;

    @Inject
    Downloader downloader;
    private OfflineFileRepository offlineFileRepository;
    private DownloadReceiver downloadReceiver;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setUpInjections();
        setUpBus();
        setDatabaseRepository();
        setUpDownloadReceiver();
    }

    private void setUpInjections() {
        AmahiApplication.from(this).inject(this);
    }

    private void setUpBus() {
        BusProvider.getBus().register(this);
    }

    private void setDatabaseRepository() {
        offlineFileRepository = new OfflineFileRepository(this);
    }

    private void setUpDownloadReceiver() {
        downloadReceiver = new DownloadReceiver();
        IntentFilter downloadActionsFilter = new IntentFilter();
        downloadActionsFilter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        registerReceiver(downloadReceiver, downloadActionsFilter);
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {

        if (intent != null && intent.hasExtra(Intents.Extras.SERVER_FILE)) {
            if (downloadReceiver == null) {
                setUpDownloadReceiver();
            }
            ServerFile serverFile = intent.getParcelableExtra(Intents.Extras.SERVER_FILE);
            ServerShare serverShare = intent.getParcelableExtra(Intents.Extras.SERVER_SHARE);
            Uri downloadUri = serverClient.getFileUri(serverShare, serverFile);
            long downloadId = downloader.startDownloadingForOfflineMode(downloadUri, serverFile.getName());
            saveDownloadId(serverFile, serverShare, downloadId);
        }

        if (intent != null && intent.getAction() != null && intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
            long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
            finishDownloading(downloadId);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void saveDownloadId(ServerFile serverFile, ServerShare serverShare, long downloadId) {
        OfflineFile offlineFile = new OfflineFile();
        offlineFile.state = OfflineFile.DOWNLOADING;
        offlineFile.path = serverFile.getPath();
        offlineFile.downloadID = downloadId;
        offlineFile.name = serverFile.getName();
        offlineFile.timeStamp = serverFile.getModificationTime().getTime();
        offlineFileRepository.update(offlineFile);
    }

    private void finishDownloading(long downloadId) {
        OfflineFile offlineFile = offlineFileRepository.getFileWithDownloadId(downloadId);
        if (offlineFile != null) {
            downloader.finishDownloadingById(downloadId, offlineFile.name);
            offlineFile.state = OfflineFile.DOWNLOADED;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        tearDownDownloader();
        tearDownBus();
        tearDownReceiver();
    }

    private void tearDownDownloader() {
        if (downloader != null) {
            downloader = null;
        }
    }

    public void tearDownBus() {
        BusProvider.getBus().unregister(this);
    }

    private void tearDownReceiver() {
        unregisterReceiver(downloadReceiver);
    }
}
