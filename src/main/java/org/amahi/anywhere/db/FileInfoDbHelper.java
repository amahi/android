package org.amahi.anywhere.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import static org.amahi.anywhere.db.FileInfoDb.FILE_PLAYED;
import static org.amahi.anywhere.db.FileInfoDb.KEY_FILE_PATH;
import static org.amahi.anywhere.db.FileInfoDb.TABLE_NAME;

/**
 * Performs CRUD operation on the SQLite database provided by {@link FileInfoDb FileInfoDb}
 */

public class FileInfoDbHelper {

    private static FileInfoDbHelper fileInfoDbHelper;
    private FileInfoDb fileInfoDb;
    private SQLiteDatabase sqLiteDatabase;

    private FileInfoDbHelper(Context context) {
        fileInfoDb = new FileInfoDb(context);
        sqLiteDatabase = fileInfoDb.getWritableDatabase();
    }

    public static FileInfoDbHelper init(Context context) {
        if (fileInfoDbHelper == null) {
            fileInfoDbHelper = new FileInfoDbHelper(context);
        }

        return fileInfoDbHelper;
    }

    public void setFilePlayed(String filePath, boolean filePlayedBool) {

        int filePlayedInt;

        if (filePlayedBool) {
            filePlayedInt = 1;
        } else {
            filePlayedInt = 0;
        }

        ContentValues values = new ContentValues();

        values.put(KEY_FILE_PATH, filePath);
        values.put(FILE_PLAYED, filePlayedInt);

        try {
            sqLiteDatabase.insert(TABLE_NAME, null, values);
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

    }

    public boolean getFilePlayed(String filePath) {
        int filePlayedInt = 0;

        String[] projection = {
            KEY_FILE_PATH,
            FILE_PLAYED
        };

        String selection = KEY_FILE_PATH + "=?";
        String[] selectionArgs = {filePath};

        Cursor cursor = sqLiteDatabase.query(
            TABLE_NAME,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            null
        );

        if (cursor != null) {
            if (cursor.moveToNext()) {
                String filePlayedStr = cursor.getString(1);
                filePlayedInt = Integer.parseInt(filePlayedStr);
            } else {
                cursor.close();
                return false;
            }
        } else {
            return false;
        }

        cursor.close();

        if (filePlayedInt == 1) {
            return true;
        } else {
            return false;
        }

    }

    public void removeFilePlayed(String filePath) {
        sqLiteDatabase.delete(TABLE_NAME, KEY_FILE_PATH + "=?", new String[]{String.valueOf(filePath)});
    }

    public void clearDb() {
        sqLiteDatabase.execSQL("DELETE FROM " + TABLE_NAME);
    }

    public void closeDataBase() {
        sqLiteDatabase.close();
        fileInfoDb.close();
        fileInfoDbHelper = null;
    }

}
