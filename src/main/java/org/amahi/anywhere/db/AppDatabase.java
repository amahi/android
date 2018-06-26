
package org.amahi.anywhere.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import org.amahi.anywhere.db.daos.OfflineFileDao;
import org.amahi.anywhere.db.daos.PlayedFileDao;
import org.amahi.anywhere.db.daos.RecentFileDao;
import org.amahi.anywhere.db.entities.OfflineFile;
import org.amahi.anywhere.db.entities.PlayedFile;
import org.amahi.anywhere.db.entities.RecentFile;

@Database(entities = {OfflineFile.class, PlayedFile.class, RecentFile.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase INSTANCE;

    public static AppDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                AppDatabase.class, "app_database")
                .allowMainThreadQueries()
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

}
