package org.amahi.anywhere.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

import static android.support.customtabs.CustomTabsIntent.KEY_ID;
import static org.amahi.anywhere.db.UploadQueueDb.TABLE_NAME;

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

	public boolean addNewImagePath(String imagePath) {
		ContentValues values = new ContentValues();

		values.put(UploadQueueDb.KEY_FILE_PATH, imagePath);
		return sqLiteDatabase.insert(TABLE_NAME, null, values) != -1;
	}

	public ArrayList<String> getAllImagePaths() {
		ArrayList<String> imagePaths = new ArrayList<>();

		Cursor cursor = sqLiteDatabase.query(TABLE_NAME, null, null, null, null, null, null);
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

	public void removeFirstImagePath() {
		Cursor cursor = sqLiteDatabase.query(TABLE_NAME, null, null, null, null, null, null);
		if(cursor.moveToFirst()) {
			String rowId = cursor.getString(cursor.getColumnIndex(KEY_ID));

			sqLiteDatabase.delete(TABLE_NAME, KEY_ID + "=?",  new String[]{rowId});
		}
		cursor.close();
	}

	public void clearDb() {
		sqLiteDatabase.execSQL("DELETE FROM " + TABLE_NAME);
	}

	public void closeDataBase() {
		sqLiteDatabase.close();
		uploadQueueDb.close();
		uploadQueueDbHelper = null;
	}

}
