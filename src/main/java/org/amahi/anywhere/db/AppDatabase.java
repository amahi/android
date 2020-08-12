
package org.amahi.anywhere.db;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import android.content.Context;

import org.amahi.anywhere.db.daos.FileInfoDao;
import org.amahi.anywhere.db.daos.OfflineFileDao;
import org.amahi.anywhere.db.daos.PlayedFileDao;
import org.amahi.anywhere.db.daos.RecentFileDao;
import org.amahi.anywhere.db.entities.FileInfo;
import org.amahi.anywhere.db.entities.OfflineFile;
import org.amahi.anywhere.db.entities.PlayedFile;
import org.amahi.anywhere.db.entities.RecentFile;

@Database(entities = {OfflineFile.class, PlayedFile.class, RecentFile.class, FileInfo.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase INSTANCE;

    public static AppDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                AppDatabase.class, "app_database")
                .allowMainThreadQueries()
                .addMigrations(MIGRATION_1_2)
                .build();
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }

    public abstract OfflineFileDao offlineFileDao();

    public abstract RecentFileDao recentFileDao();

    public abstract PlayedFileDao playedFileDao();

    public abstract FileInfoDao fileInfoDao();

    //migrations

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE `file_info_table` (`uniqueKey` TEXT NOT NULL, "
                + "`lastOpened` TEXT, PRIMARY KEY(`uniqueKey`))");
        }
    };
}
