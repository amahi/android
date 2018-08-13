package org.amahi.anywhere.db.repositories;

import android.content.Context;

import org.amahi.anywhere.db.AppDatabase;
import org.amahi.anywhere.db.daos.RecentFileDao;
import org.amahi.anywhere.db.entities.RecentFile;
import org.amahi.anywhere.util.Preferences;

import java.util.List;

public class RecentFileRepository {

    private Context context;

    private RecentFileDao mRecentFileDao;

    public RecentFileRepository(Context context) {
        this.context = context;
        AppDatabase db = AppDatabase.getDatabase(context);
        mRecentFileDao = db.recentFileDao();
    }

    public void insert(RecentFile recentFile) {
        if (getRecentFile(recentFile.getUniqueKey()) == null) {
            int size = getAllRecentFiles().size();
            if (size == 50) {
                deleteLastFile();
            }
        } else {
            deleteFile(recentFile.getUniqueKey());
        }

        mRecentFileDao.insert(recentFile);
    }

    public RecentFile getRecentFile(String uniqueKey) {
        return mRecentFileDao.getRecentFile(uniqueKey);
    }

    private void deleteLastFile() {
        RecentFile file = mRecentFileDao.getOldestFile();
        mRecentFileDao.delete(file.getUniqueKey());
    }

    public void deleteFile(String uniqueKey) {
        mRecentFileDao.delete(uniqueKey);
    }

    public List<RecentFile> getAllRecentFiles() {
        String serverName = Preferences.getServerName(context);
        return mRecentFileDao.getRecentFiles(serverName);
    }

}
