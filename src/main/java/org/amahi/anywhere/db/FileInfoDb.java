package org.amahi.anywhere.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * SQLite database to record whether a media file is played (upto a certain duration) or not.
 * Query methods are managed by helper class {@link FileInfoDbHelper FileInfoDbHelper}
 */

class FileInfoDb extends SQLiteOpenHelper {

    // Table Name
    static final String TABLE_NAME = "FILE_INFO_TABLE";

    // Column Attributes names
    static final String KEY_FILE_PATH = "file_path";
    static final String FILE_PLAYED = "file_played";

    // Database version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "FileInfo.db";


    FileInfoDb(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "("
            + KEY_FILE_PATH + " VARCHAR(200) PRIMARY KEY, "
            + FILE_PLAYED + " INTEGER NOT NULL CHECK (" + FILE_PLAYED + " IN (0,1)))";

        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Procedure to follow when database is upgraded (DATABASE_VERSION increased)

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);

        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Procedure to follow when database is downgraded (DATABASE_VERSION decreased)
    }

}
