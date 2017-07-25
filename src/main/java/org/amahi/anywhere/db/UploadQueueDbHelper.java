package org.amahi.anywhere.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.amahi.anywhere.model.UploadFile;

import java.util.ArrayList;

import static org.amahi.anywhere.db.UploadQueueDb.KEY_ID;
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

	public UploadFile addNewImagePath(String imagePath) {
		ContentValues values = new ContentValues();

		values.put(UploadQueueDb.KEY_FILE_PATH, imagePath);
		int id = (int) sqLiteDatabase.insert(TABLE_NAME, null, values);
		if (id != -1) {
			return new UploadFile(id, imagePath);
		} else {
			return null;
		}
	}

	public ArrayList<UploadFile> getAllImagePaths() {
		ArrayList<UploadFile> imagePaths = new ArrayList<>();

		Cursor cursor = sqLiteDatabase.query(TABLE_NAME, null, null, null, null, null, null);
		if (cursor != null && cursor.moveToFirst()) {
			while (!cursor.isAfterLast()) {
				int id = cursor.getInt(
						cursor.getColumnIndex(UploadQueueDb.KEY_ID));
				String imagePath = cursor.getString(
						cursor.getColumnIndex(UploadQueueDb.KEY_FILE_PATH));

				UploadFile uploadFile = new UploadFile(id, imagePath);
				imagePaths.add(uploadFile);
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
		if (cursor.moveToFirst()) {
			String rowId = cursor.getString(cursor.getColumnIndex(KEY_ID));

			sqLiteDatabase.delete(TABLE_NAME, KEY_ID + "=?", new String[]{rowId});
		}
		cursor.close();
	}

	public void removeImagePath(int id) {
		sqLiteDatabase.delete(TABLE_NAME, KEY_ID + "=?", new String[]{String.valueOf(id)});
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
