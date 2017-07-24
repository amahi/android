package org.amahi.anywhere.queue;

import android.content.Context;

import org.amahi.anywhere.db.UploadQueueDbHelper;

import java.util.ArrayList;
import java.util.Queue;

/**
 * A persistent Queue implementation for images to be uploaded using SQLite db for storage.
 */
public class PersistentUploadQueue extends ArrayList<String> implements Queue<String> {

	private UploadQueueDbHelper uploadQueueDbHelper;

	public static PersistentUploadQueue init(Context context) {
		PersistentUploadQueue persistentUploadQueue = new PersistentUploadQueue(context);
		persistentUploadQueue.addAll(persistentUploadQueue.uploadQueueDbHelper.getAllImagePaths());
		return persistentUploadQueue;
	}

	private PersistentUploadQueue(Context context) {
		uploadQueueDbHelper = UploadQueueDbHelper.init(context);
	}

	@Override
	public boolean offer(String imagePath) {
		return uploadQueueDbHelper.addNewImagePath(imagePath);
	}

	@Override
	public String remove() {
		uploadQueueDbHelper.removeFirstImagePath();
		return remove(0);
	}

	@Override
	public String poll() {
		return remove();
	}

	@Override
	public String element() {
		return get(0);
	}

	@Override
	public String peek() {
		return element();
	}
}
