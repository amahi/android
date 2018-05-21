package org.amahi.anywhere.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.R;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.db.entities.OfflineFile;
import org.amahi.anywhere.db.repositories.OfflineFileRepository;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerShare;
import org.amahi.anywhere.util.Downloader;
import org.amahi.anywhere.util.Intents;
import org.amahi.anywhere.util.NetworkUtils;

import javax.inject.Inject;

public class DownloadService extends Service implements Downloader.DownloadCallbacks {

    @Inject
    ServerClient serverClient;

    @Inject
    Downloader downloader;
    private OfflineFileRepository offlineFileRepository;
    private Notification.Builder notificationBuilder;
    private NetworkUtils networkUtils;
    private boolean isDownloading;

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
        setUpNetworkUtils();
        setDatabaseRepository();
        downloader.setDownloadCallbacks(this);
    }

    private void setUpInjections() {
        AmahiApplication.from(this).inject(this);
    }

    private void setUpBus() {
        BusProvider.getBus().register(this);
    }

    private void setUpNetworkUtils() {
        networkUtils = new NetworkUtils(this);
    }

    private void setDatabaseRepository() {
        offlineFileRepository = new OfflineFileRepository(this);
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {

        if (intent != null && intent.hasExtra(Intents.Extras.SERVER_FILE)) {
            ServerFile serverFile = intent.getParcelableExtra(Intents.Extras.SERVER_FILE);
            ServerShare serverShare = intent.getParcelableExtra(Intents.Extras.SERVER_SHARE);
            saveFileInOfflineFileQueue(serverFile, serverShare);
        }

        if (networkUtils.isNetworkAvailable()) {
            if (!isDownloading) {
                startNextDownload();
                isDownloading = true;
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void startNextDownload() {
        OfflineFile file = getNextDownloadFile();
        if (file != null) {
            long downloadId = downloader.startDownloadingForOfflineMode(Uri.parse(file.getFileUri()), file.getName());
            saveOfflineFileDownloadId(file, downloadId);
        } else {
            stopDownloading();
        }
    }

    private void stopDownloading() {
        stopSelf();
    }

    private void saveOfflineFileDownloadId(OfflineFile offlineFile, long downloadId) {
        offlineFile.setDownloadId(downloadId);
        offlineFileRepository.update(offlineFile);
    }

    private OfflineFile getNextDownloadFile() {
        return offlineFileRepository.getFileWithState(OfflineFile.DOWNLOADING);
    }

    private void saveFileInOfflineFileQueue(ServerFile serverFile, ServerShare serverShare) {
        String downloadUri = serverClient.getFileUri(serverShare, serverFile).toString();
        OfflineFile offlineFile = new OfflineFile(serverFile.getPath(), serverFile.getName());
        offlineFile.setFileUri(downloadUri);
        offlineFile.setTimeStamp(serverFile.getModificationTime().getTime());
        offlineFile.setState(OfflineFile.DOWNLOADING);
        offlineFile.setDownloadId(-1);
        offlineFileRepository.insert(offlineFile);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        tearDownDownloader();
        tearDownBus();
    }

    private void tearDownDownloader() {
        if (downloader != null) {
            downloader = null;
        }
    }

    public void tearDownBus() {
        BusProvider.getBus().unregister(this);
    }

    @Override
    public void downloadStarted(int id, String fileName) {
        notificationBuilder = new Notification.Builder(getApplicationContext());
        notificationBuilder
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_app_logo)
            .setContentTitle(getString(R.string.notification_download_title))
            .setContentText(getString(R.string.notification_upload_message, fileName))
            .setProgress(100, 0, false)
            .build();
        Notification notification = notificationBuilder.build();
        startForeground(id, notification);
    }

    @Override
    public void downloadProgress(int id, int progress) {
        NotificationManager notificationManager = (NotificationManager) getApplicationContext()
            .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationBuilder
            .setProgress(100, progress, false);
        Notification notification = notificationBuilder.build();
        notificationManager.notify(id, notification);
    }

    @Override
    public void downloadSuccess(long id) {
        stopForeground(false);
        NotificationManager notificationManager = (NotificationManager) getApplicationContext()
            .getSystemService(Context.NOTIFICATION_SERVICE);

        notificationBuilder
            .setContentTitle(getString(R.string.notification_offline_download_complete))
            .setOngoing(false)
            .setProgress(0, 0, false);

        Notification notification = notificationBuilder.build();
        notificationManager.notify((int) id, notification);
        updateOfflineFileState(id);
        startNextDownload();
    }

    private void updateOfflineFileState(long id) {
        OfflineFile offlineFile = offlineFileRepository.getFileWithDownloadId(id);
        if(offlineFile!=null) {
            offlineFile.setState(OfflineFile.DOWNLOADED);
            offlineFile.setDownloadId(-1);
            offlineFileRepository.update(offlineFile);
            downloader.moveFileToInternalStorage(offlineFile.getName());
        }
    }

    @Override
    public void downloadError(long id) {
        removeOfflineFile(id);
    }

    private void removeOfflineFile(long id) {
        OfflineFile offlineFile = offlineFileRepository.getFileWithDownloadId(id);
        offlineFileRepository.delete(offlineFile);
    }
}
