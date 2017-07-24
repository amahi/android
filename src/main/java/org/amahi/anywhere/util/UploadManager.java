package org.amahi.anywhere.util;

import android.content.Context;
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

	private Context context;
	private ArrayList<UploadFile> uploadFiles;
	private UploadCallbacks uploadCallbacks;

	private UploadFile currentFile;

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
		if (uploadFiles.isEmpty()) {
			uploadCallbacks.uploadQueueFinished();
		} else {
			currentFile = uploadFiles.remove(0);
			uploadImage(currentFile);
		}
	}

	private void uploadImage(UploadFile uploadFile) {
		File image = new File(uploadFile.getPath());
		if (image.exists()) {
			uploadCallbacks.uploadStarted(uploadFile.getId(), image.getName());
			serverClient.uploadFile(image, getUploadShareName(), getUploadPath());
		}

	}

	private String getUploadShareName() {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getString(context.getString(R.string.preference_key_upload_share), null);
	}

	private String getUploadPath() {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getString(context.getString(R.string.preference_key_upload_path), null);
	}


	public void add(UploadFile uploadFile) {
		uploadFiles.add(uploadFile);
	}

	@Subscribe
	public void onFileUploadProgressEvent(ServerFileUploadProgressEvent event) {
		uploadCallbacks.uploadProgress(currentFile.getId(), event.getProgress());
	}

	@Subscribe
	public void onFileUploadCompleteEvent(ServerFileUploadCompleteEvent event) {
		if (event.wasUploadSuccessful()) {
			uploadFiles.remove(currentFile);
			uploadCallbacks.uploadComplete(currentFile.getId());
		} else {
			uploadCallbacks.uploadError(currentFile.getId());
		}
		currentFile = null;
		startUploading();
	}

	public interface UploadCallbacks {
		void uploadStarted(int id, String fileName);

		void uploadProgress(int id, int progress);

		void uploadComplete(int id);

		void uploadError(int id);

		void uploadQueueFinished();
	}
}
