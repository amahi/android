package org.amahi.anywhere.db.repositories;

import android.content.Context;

import org.amahi.anywhere.db.AppDatabase;
import org.amahi.anywhere.db.daos.OfflineFileDao;
import org.amahi.anywhere.db.entities.OfflineFile;

import java.util.List;

public class OfflineFileRepository {

    private OfflineFileDao mOfflineFileDao;

    public OfflineFileRepository(Context context) {
        AppDatabase db = AppDatabase.getDatabase(context);
        mOfflineFileDao = db.offlineFileDao();
    }

    public OfflineFile getOfflineFile(String share, String path, String name) {
        return mOfflineFileDao.getOfflineFile(share, path, name);
    }

    public OfflineFile getOfflineFile(String name, long timeStamp) {
        return mOfflineFileDao.getOfflineFile(name, timeStamp);
    }

    public OfflineFile getCurrentDownloadingFile() {
        return mOfflineFileDao.getCurrentDownloadingFile();
    }

    public void insert(OfflineFile offlineFile) {
        mOfflineFileDao.insert(offlineFile);
    }

    public void update(OfflineFile offlineFile) {
        mOfflineFileDao.update(offlineFile);
    }

    public void delete(OfflineFile offlineFile) {
        mOfflineFileDao.delete(offlineFile);
    }

    public OfflineFile getFileWithDownloadId(long downloadId) {
        return mOfflineFileDao.getOfflineFileWithDownloadId(downloadId);
    }

    public OfflineFile getFileWithState(@OfflineFile.Types int state) {
        return mOfflineFileDao.getOfflineFileWithState(state);
    }

    public List<OfflineFile> getAllOfflineFiles() {
        return mOfflineFileDao.getAllOfflineFiles();
    }
}
