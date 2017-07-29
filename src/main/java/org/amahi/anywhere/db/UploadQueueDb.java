package org.amahi.anywhere.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * SQLite db for maintaining image uploads in a persistent database.
 * Query methods managed by {@link UploadQueueDbHelper UploadQueueDbHelper}.
 */

class UploadQueueDb extends SQLiteOpenHelper {

	// Database version
	private static final int DATABASE_VERSION = 1;

	// Database Name
	private static final String DATABASE_NAME = "AMAHI_ANYWHERE_DATABASE";

	// Table name
	static final String TABLE_NAME = "UPLOAD_QUEUE_TABLE";

	// column names
	static final String KEY_ID = "id";
	static final String KEY_FILE_PATH = "file_path";

	UploadQueueDb(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
				+ KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ KEY_FILE_PATH + " VARCHAR(200) NOT NULL)";

		db.execSQL(CREATE_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

}
