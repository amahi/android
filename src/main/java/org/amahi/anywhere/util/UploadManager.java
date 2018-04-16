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

import android.content.Context;
import android.os.Handler;
import android.preference.PreferenceManager;

import com.squareup.otto.Subscribe;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.R;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.ServerFileUploadCompleteEvent;
import org.amahi.anywhere.bus.ServerFileUploadProgressEvent;
import org.amahi.anywhere.model.UploadFile;
import org.amahi.anywhere.server.client.ServerClient;

import java.io.File;
import java.util.ArrayList;

import javax.inject.Inject;

/**
 * An Upload Manager that manages all the uploads one by one present in the queue.
 */
public class UploadManager {
    @Inject
    public ServerClient serverClient;
    private boolean isRunning = false;
    private Context context;
    private ArrayList<UploadFile> uploadFiles;
    private UploadCallbacks uploadCallbacks;

    public <T extends Context & UploadManager.UploadCallbacks>
    UploadManager(T context, ArrayList<UploadFile> uploadFiles) {

        this.context = context;
        this.uploadCallbacks = context;
        this.uploadFiles = uploadFiles;

        setUpInjections();
        setUpBus();
    }

    private void setUpInjections() {
        AmahiApplication.from(context).inject(this);
    }

    private void setUpBus() {
        BusProvider.getBus().register(this);
    }

    public void tearDownBus() {
        BusProvider.getBus().unregister(this);
    }

    public void startUploading() {
        if (!isRunning) {
            isRunning = true;
            processNextFile();
        }
    }

    private void processNextFile() {
        if (uploadFiles.isEmpty()) {
            isRunning = false;
            uploadCallbacks.uploadQueueFinished();
        } else {
            UploadFile currentFile = uploadFiles.remove(0);
            upload(currentFile);
        }
    }

    private void upload(UploadFile uploadFile) {
        File image = new File(uploadFile.getPath());
        if (image.exists()) {
            uploadCallbacks.uploadStarted(uploadFile.getId(), image.getName());
            serverClient.uploadFile(uploadFile.getId(), image, getUploadShareName(),
                getUploadPath());
        } else {
            uploadCallbacks.removeFileFromDb(uploadFile.getId());
            processNextFile();
        }

    }

    private String getUploadShareName() {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getString(context.getString(R.string.preference_key_upload_share), null);
    }

    private String getUploadPath() {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getString(context.getString(R.string.preference_key_upload_location), null);
    }


    public void add(UploadFile uploadFile) {
        uploadFiles.add(uploadFile);
    }

    @Subscribe
    public void onFileUploadProgressEvent(ServerFileUploadProgressEvent event) {
        uploadCallbacks.uploadProgress(event.getId(), event.getProgress());
    }

    @Subscribe
    public void onFileUploadCompleteEvent(ServerFileUploadCompleteEvent event) {
        if (event.wasUploadSuccessful()) {
            uploadCallbacks.removeFileFromDb(event.getId());
            uploadCallbacks.uploadSuccess(event.getId());
        } else {
            uploadCallbacks.uploadError(event.getId());
        }

        final Handler handler = new Handler();
        handler.postDelayed(this::processNextFile, 500);
    }

    public interface UploadCallbacks {
        void uploadStarted(int id, String fileName);

        void uploadProgress(int id, int progress);

        void uploadSuccess(int id);

        void uploadError(int id);

        void removeFileFromDb(int id);

        void uploadQueueFinished();
    }
}
