package org.amahi.anywhere.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

/**
 * Performs CRUD operation on SQLite db provided by {@link UploadQueueDb} UploadQueueDb.
 */

public class UploadQueueDbHelper {

	private UploadQueueDb uploadQueueDb;
	private SQLiteDatabase sqLiteDatabase;
	private static UploadQueueDbHelper uploadQueueDbHelper;

	public static UploadQueueDbHelper init(Context context) {
		if (uploadQueueDbHelper == null) uploadQueueDbHelper = new UploadQueueDbHelper(context);
		return uploadQueueDbHelper;
	}

	private UploadQueueDbHelper(Context context) {
		uploadQueueDb = new UploadQueueDb(context);
		sqLiteDatabase = uploadQueueDb.getWritableDatabase();
	}

	public void addNewImagePath(String imagePath) {
		ContentValues values = new ContentValues();

		values.put(UploadQueueDb.KEY_FILE_PATH, imagePath);
		sqLiteDatabase.insert(UploadQueueDb.TABLE_NAME, null, values);
	}

	private ArrayList<String> getAllImagePaths() {
		ArrayList<String> imagePaths = new ArrayList<>();

		Cursor cursor = sqLiteDatabase.rawQuery("select * from " + UploadQueueDb.TABLE_NAME, null);
		if (cursor != null && cursor.moveToFirst()) {
			while (!cursor.isAfterLast()) {
				String imagePath = cursor.getString(
						cursor.getColumnIndex(UploadQueueDb.KEY_FILE_PATH));
				imagePaths.add(imagePath);
				cursor.moveToNext();
			}
		}
		if (cursor != null) {
			cursor.close();
		}

		return imagePaths;
	}

	public void clearDb() {
		sqLiteDatabase.execSQL("DELETE FROM " + UploadQueueDb.TABLE_NAME);
	}

	public void closeDataBase() {
		sqLiteDatabase.close();
		uploadQueueDb.close();
		uploadQueueDbHelper = null;
	}

}
