
package org.amahi.anywhere.db;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import android.content.Context;

import org.amahi.anywhere.db.daos.HDADao;
import org.amahi.anywhere.db.daos.OfflineFileDao;
import org.amahi.anywhere.db.daos.PlayedFileDao;
import org.amahi.anywhere.db.daos.RecentFileDao;
import org.amahi.anywhere.db.entities.HDA;
import org.amahi.anywhere.db.entities.OfflineFile;
import org.amahi.anywhere.db.entities.PlayedFile;
import org.amahi.anywhere.db.entities.RecentFile;

@Database(entities = {OfflineFile.class, PlayedFile.class, RecentFile.class, HDA.class}, version = 2, exportSchema = false)
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

    public abstract HDADao hdaDao();

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE `hda_cache` (`ip` TEXT NOT NULL, "
                + "PRIMARY KEY(`ip`))");

            //for adding modificationTime and mime columns
            database.execSQL("ALTER TABLE `recent_file_table` "
                + " ADD COLUMN `modificationTime` INTEGER");
            database.execSQL("ALTER TABLE `recent_file_table` "
                + " ADD COLUMN `mime` TEXT");

            //for ordering the columns according to the expected table creation found in logs

            database.execSQL("CREATE TABLE IF NOT EXISTS `temp_recent_file_table` (`visitTime` INTEGER NOT NULL, `size` INTEGER NOT NULL, `modificationTime` INTEGER NOT NULL, `uniqueKey` TEXT NOT NULL, `mime` TEXT, `serverName` TEXT, `uri` TEXT,  PRIMARY KEY(`uniqueKey`))");

            database.execSQL("INSERT INTO `temp_recent_file_table` SELECT `visitTime`, `size`, `modificationTime`, `uniqueKey`, `mime`, `serverName`, `uri` FROM `recent_file_table`");

            database.execSQL("DROP TABLE `recent_file_table`;");

            database.execSQL("ALTER TABLE `temp_recent_file_table` RENAME TO `recent_file_table`");

        }
    };
}
