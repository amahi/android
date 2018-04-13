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

package org.amahi.anywhere.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.squareup.otto.Subscribe;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.R;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.ServerConnectedEvent;
import org.amahi.anywhere.bus.ServerConnectionChangedEvent;
import org.amahi.anywhere.db.UploadQueueDbHelper;
import org.amahi.anywhere.job.NetConnectivityJob;
import org.amahi.anywhere.model.UploadFile;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.Server;
import org.amahi.anywhere.util.Intents;
import org.amahi.anywhere.util.NetworkUtils;
import org.amahi.anywhere.util.UploadManager;

import java.util.ArrayList;

import javax.inject.Inject;

/**
 * File upload service
 */
public class UploadService extends Service implements UploadManager.UploadCallbacks {

    @Inject
    ServerClient serverClient;

    private UploadManager uploadManager;
    private UploadQueueDbHelper uploadQueueDbHelper;
    private NotificationCompat.Builder notificationBuilder;
    private NetworkUtils networkUtils;

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
        setUpDbHelper();
        setUpNetworkUtils();
    }

    private void setUpInjections() {
        AmahiApplication.from(this).inject(this);
    }

    private void setUpBus() {
        BusProvider.getBus().register(this);
    }

    private void setUpDbHelper() {
        uploadQueueDbHelper = UploadQueueDbHelper.init(this);
    }

    private void setUpNetworkUtils() {
        networkUtils = new NetworkUtils(this);
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {

        if (intent != null && intent.hasExtra(Intents.Extras.IMAGE_URIS)) {
            if (isAutoUploadEnabled()) {
                ArrayList<Uri> uris = intent.getParcelableArrayListExtra(Intents.Extras.IMAGE_URIS);
                for (Uri uri : uris) {
                    String imagePath = queryImagePath(uri);
                    if (imagePath != null) {
                        UploadFile uploadFile = uploadQueueDbHelper.addNewImagePath(imagePath);
                        if (uploadFile != null && uploadManager != null)
                            uploadManager.add(uploadFile);
                    }
                }
            }
        }

        if (isAutoUploadEnabled()) {
            if (isUploadAllowed()) {
                connectToServer();
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    NetConnectivityJob.scheduleJob(this);
                }
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private boolean isAutoUploadEnabled() {
        return PreferenceManager.getDefaultSharedPreferences(this)
            .getBoolean(getString(R.string.preference_key_upload_switch), false);
    }

    private boolean isUploadAllowed() {
        return networkUtils.isUploadAllowed();
    }

    private void connectToServer() {
        Server server = getUploadServer();
        if (server != null) {
            setUpServerConnection(server);
        }
    }

    private void setUpServerConnection(@NonNull Server server) {
        if (serverClient.isConnected(server)) {
            setUpServerConnection();
        } else {
            serverClient.connect(this, server);
        }
    }

    @Subscribe
    public void onServerConnected(ServerConnectedEvent event) {
        Server uploadServer = getUploadServer();
        if (uploadServer != null && uploadServer == event.getServer()) {
            setUpServerConnection();
        }
    }

    private void setUpServerConnection() {
        if (!isConnectionAvailable() || isConnectionAuto()) {
            serverClient.connectAuto();
            return;
        }

        if (isConnectionLocal()) {
            serverClient.connectLocal();
        } else {
            serverClient.connectRemote();
        }
    }

    private boolean isConnectionAvailable() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        return preferences.contains(getString(R.string.preference_key_server_connection));
    }

    private boolean isConnectionAuto() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String preferenceConnection = preferences.getString(getString(R.string.preference_key_server_connection), null);

        return preferenceConnection.equals(getString(R.string.preference_key_server_connection_auto));
    }

    private boolean isConnectionLocal() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String preferenceConnection = preferences.getString(getString(R.string.preference_key_server_connection), null);

        return preferenceConnection.equals(getString(R.string.preference_key_server_connection_local));
    }

    @Subscribe
    public void onServerConnectionChanged(ServerConnectionChangedEvent event) {
        if (uploadManager == null) {
            setUpUploadManager();
        }
        uploadManager.startUploading();
    }

    private Server getUploadServer() {
        String session = PreferenceManager.getDefaultSharedPreferences(this)
            .getString(getString(R.string.preference_key_upload_server), null);
        if (session != null) {
            return new Server(session);
        } else {
            return null;
        }
    }

    private void setUpUploadManager() {
        ArrayList<UploadFile> uploadFiles = uploadQueueDbHelper.getAllImagePaths();
        uploadManager = new UploadManager(this, uploadFiles);
    }

    private String queryImagePath(Uri imageUri) {
        String filePath = null;
        if ("content".equals(imageUri.getScheme())) {
            Cursor cursor = this.getContentResolver()
                .query(imageUri, null, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                filePath = cursor.getString(columnIndex);
                cursor.close();
            }
        } else {
            filePath = imageUri.toString();
        }
        return filePath;
    }

    @Override
    public void uploadStarted(int id, String fileName) {
        notificationBuilder = new NotificationCompat.Builder(getApplicationContext());
        notificationBuilder
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_app_logo)
            .setContentTitle(getString(R.string.notification_upload_title))
            .setContentText(getString(R.string.notification_upload_message, fileName))
            .setProgress(100, 0, false)
            .build();
        Notification notification = notificationBuilder.build();
        startForeground(id, notification);
    }

    @Override
    public void uploadProgress(int id, int progress) {
        NotificationManager notificationManager = (NotificationManager) getApplicationContext()
            .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationBuilder
            .setProgress(100, progress, false);
        Notification notification = notificationBuilder.build();
        notificationManager.notify(id, notification);
    }

    @Override
    public void uploadSuccess(int id) {
        uploadComplete(id, getString(R.string.message_upload_success));
    }

    @Override
    public void uploadError(int id) {
        uploadComplete(id, getString(R.string.message_upload_error));
    }

    private void uploadComplete(int id, String title) {
        stopForeground(false);
        NotificationManager notificationManager = (NotificationManager) getApplicationContext()
            .getSystemService(Context.NOTIFICATION_SERVICE);

        notificationBuilder
            .setContentTitle(title)
            .setOngoing(false)
            .setProgress(0, 0, false);

        Notification notification = notificationBuilder.build();
        notificationManager.notify(id, notification);
    }

    @Override
    public void removeFileFromDb(int id) {
        uploadQueueDbHelper.removeImagePath(id);
    }

    @Override
    public void uploadQueueFinished() {
        tearDownUploadManager();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uploadQueueDbHelper.closeDataBase();
        tearDownUploadManager();
        tearDownBus();
    }

    private void tearDownUploadManager() {
        if (uploadManager != null) {
            uploadManager.tearDownBus();
            uploadManager = null;
        }
    }

    public void tearDownBus() {
        BusProvider.getBus().unregister(this);
    }
}
