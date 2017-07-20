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
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.R;
import org.amahi.anywhere.bus.ServerFileUploadCompleteEvent;
import org.amahi.anywhere.bus.ServerFileUploadProgressEvent;
import org.amahi.anywhere.server.client.ServerClient;

import java.io.File;

import javax.inject.Inject;

/**
 * File upload service
 */
public class UploadService extends Service {

	private final String TAG = "UPLOAD_SERVICE";

	@Inject
	ServerClient serverClient;

	private final int notificationId = 158;
	private NotificationCompat.Builder notificationBuilder;

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		Log.e(TAG, "on_create");
		super.onCreate();
		setUpInjections();
	}

	@Override
	public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
		Log.e(TAG, "on_start_command");
		assert intent != null;
		String imagePath = queryImagePath(intent.getData());
		Toast.makeText(getApplicationContext(), "New Photo: " + imagePath, Toast.LENGTH_SHORT)
				.show();
		if (imagePath != null) {
			File file = new File(imagePath);
			uploadFile(file);
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		Log.e(TAG, "on_destroy");
		super.onDestroy();
	}

	private void setUpInjections() {
		AmahiApplication.from(this).inject(this);
	}

	private void uploadFile(File uploadFile) {
		showNotification(uploadFile.getName());
		serverClient.uploadFile(uploadFile, "photos");
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

	private void showNotification(String fileName) {
		NotificationManager notificationManager = (NotificationManager) getApplicationContext()
				.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationBuilder = new NotificationCompat.Builder(getApplicationContext());
		notificationBuilder
				.setOngoing(true)
				.setSmallIcon(R.drawable.ic_app_logo)
				.setContentTitle(getString(R.string.notification_upload_title))
				.setContentText(getString(R.string.notification_upload_message, fileName))
				.setProgress(100, 0, false)
				.build();
		Notification notification = notificationBuilder.build();
		notificationManager.notify(notificationId, notification);
	}

	private void updateNotificationProgress(int progress) {
		NotificationManager notificationManager = (NotificationManager) getApplicationContext()
				.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationBuilder
				.setProgress(100, progress, false);
		Notification notification = notificationBuilder.build();
		notificationManager.notify(notificationId, notification);
	}

	@Subscribe
	public void onFileUploadProgressEvent(ServerFileUploadProgressEvent event) {
		updateNotificationProgress(event.getProgress());
	}

	@Subscribe
	public void onFileUploadCompleteEvent(ServerFileUploadCompleteEvent event) {
		NotificationManager notificationManager = (NotificationManager) getApplicationContext()
				.getSystemService(Context.NOTIFICATION_SERVICE);
		if (event.wasUploadSuccessful()) {
			notificationBuilder
					.setContentTitle("Upload Complete")
					.setOngoing(false);

		} else {
			notificationBuilder
					.setContentTitle("Upload failed")
					.setOngoing(false);
		}
		Notification notification = notificationBuilder.build();
		notificationManager.notify(notificationId, notification);
	}

}
