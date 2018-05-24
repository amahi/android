package org.amahi.anywhere.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.squareup.otto.Subscribe;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.R;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.FileMovedEvent;
import org.amahi.anywhere.db.entities.OfflineFile;
import org.amahi.anywhere.db.repositories.OfflineFileRepository;
import org.amahi.anywhere.job.NetConnectivityJob;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerShare;
import org.amahi.anywhere.util.Downloader;
import org.amahi.anywhere.util.FileManager;
import org.amahi.anywhere.util.Intents;
import org.amahi.anywhere.util.NetworkUtils;

import java.io.File;

import javax.inject.Inject;

import static org.amahi.anywhere.util.Downloader.OFFLINE_PATH;

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
        Log.i("DownloadService", "Download Service created");
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

        Log.i("DownloadService", "Download Service start command");

        if (intent != null && intent.hasExtra(Intents.Extras.SERVER_FILE)) {
            ServerFile serverFile = intent.getParcelableExtra(Intents.Extras.SERVER_FILE);
            ServerShare serverShare = intent.getParcelableExtra(Intents.Extras.SERVER_SHARE);
            saveFileInOfflineFileQueue(serverFile, serverShare);
            Log.i("DownloadService", "New File");
        }

        if (intent != null && intent.getAction() != null && intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            OfflineFile offlineFile = getNextDownloadFile();
            if (offlineFile != null && offlineFile.getDownloadId() != -1) {
                resumeDownload(offlineFile.getDownloadId(), offlineFile.getName());
            } else {
                startNextDownload();
            }
            Log.i("DownloadService", "Connectivity");
        }

        if (networkUtils.isNetworkAvailable()) {
            if (!isDownloading) {
                startNextDownload();

                Log.i("DownloadService", "Download starts");
            }

        } else {
            scheduleNetworkConnectivityJob();
        }

        return START_NOT_STICKY;
    }

    private void startNextDownload() {
        OfflineFile file = getNextDownloadFile();
        if (file != null) {
            long downloadId = downloader.startDownloadingForOfflineMode(Uri.parse(file.getFileUri()), file.getName());
            saveOfflineFileDownloadId(file, downloadId);
            Log.i("DownloadService", "Download id : " + file.getName() + downloadId);
            isDownloading = true;
        } else {
            stopDownloading();
        }
    }

    private void stopDownloading() {
        isDownloading = false;
    }

    private void saveOfflineFileDownloadId(OfflineFile offlineFile, long downloadId) {
        offlineFile.setDownloadId(downloadId);
        offlineFileRepository.update(offlineFile);
    }

    private void resumeDownload(long downloadId, String fileName) {
        notificationBuilder = new Notification.Builder(getApplicationContext());
        notificationBuilder
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_app_logo)
            .setContentTitle(getString(R.string.notification_download_title))
            .setContentText(getString(R.string.notification_upload_message, fileName))
            .setProgress(100, 0, false)
            .build();
        Notification notification = notificationBuilder.build();
        startForeground((int) downloadId, notification);
        downloader.setDownloadCallbacks(this);
        downloader.resumeProgressCount(downloadId);
    }

    private OfflineFile getNextDownloadFile() {
        OfflineFile file = offlineFileRepository.getFileWithState(OfflineFile.OUT_OF_DATE);
        if (file == null) {
            return offlineFileRepository.getFileWithState(OfflineFile.DOWNLOADING);
        }
        return file;
    }

    private void saveFileInOfflineFileQueue(ServerFile serverFile, ServerShare serverShare) {
        String downloadUri = serverClient.getFileUri(serverShare, serverFile).toString();
        OfflineFile offlineFile = new OfflineFile(serverShare.getName(), serverFile.getPath(), serverFile.getName());
        offlineFile.setFileUri(downloadUri);
        offlineFile.setTimeStamp(serverFile.getModificationTime().getTime());
        offlineFile.setMime(serverFile.getMime());
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
        OfflineFile offlineFile = offlineFileRepository.getFileWithDownloadId(id);
        if (offlineFile != null) {
            moveFileInOfflineDirectory(offlineFile.getName());
        } else {
            stopForeground(true);
        }
    }

    @Subscribe
    public void onFileMoved(FileMovedEvent event) {
        OfflineFile offlineFile = offlineFileRepository.getCurrentDownloadingFile();
        if (offlineFile != null) {

            stopForeground(false);
            NotificationManager notificationManager = (NotificationManager) getApplicationContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);

            notificationBuilder
                .setContentTitle(getString(R.string.notification_offline_download_complete))
                .setOngoing(false)
                .setProgress(100, 100, false);

            Notification notification = notificationBuilder.build();
            notificationManager.notify((int) offlineFile.getDownloadId(), notification);

            offlineFile.setState(OfflineFile.DOWNLOADED);
            offlineFile.setDownloadId(-1);
            offlineFileRepository.update(offlineFile);
            startNextDownload();
        }
    }

    private void moveFileInOfflineDirectory(String fileName) {
        File sourceLocation = new File(getExternalFilesDir(OFFLINE_PATH), fileName);
        File offlineFilesDirectory = new File(getFilesDir(), OFFLINE_PATH);
        File targetLocation = new File(getFilesDir(), OFFLINE_PATH + "/" + fileName);

        if (!offlineFilesDirectory.exists()) {
            offlineFilesDirectory.mkdir();
        }

        FileManager.newInstance(this).moveFile(sourceLocation, targetLocation);

    }

    @Override
    public void downloadError(long id) {
        removeOfflineFile(id);
    }

    @Override
    public void downloadPaused(long downloadId, int progress) {
        stopForeground(false);
        NotificationManager notificationManager = (NotificationManager) getApplicationContext()
            .getSystemService(Context.NOTIFICATION_SERVICE);

        notificationBuilder
            .setContentTitle(getString(R.string.notification_offline_download_paused))
            .setOngoing(false)
            .setProgress(100, progress, false);

        Notification notification = notificationBuilder.build();
        notificationManager.notify((int) downloadId, notification);

        stopDownloading();
    }

    private void removeOfflineFile(long id) {
        OfflineFile offlineFile = offlineFileRepository.getFileWithDownloadId(id);
        offlineFileRepository.delete(offlineFile);
    }

    private void scheduleNetworkConnectivityJob() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            NetConnectivityJob.scheduleJob(this);
        }
    }
}
