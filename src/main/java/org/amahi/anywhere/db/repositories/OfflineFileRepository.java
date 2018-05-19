package org.amahi.anywhere.db.repositories;

import android.content.Context;
import android.os.AsyncTask;

import org.amahi.anywhere.db.AppDatabase;
import org.amahi.anywhere.db.daos.OfflineFileDao;
import org.amahi.anywhere.db.entities.OfflineFile;

public class OfflineFileRepository {

    private OfflineFileDao mOfflineFileDao;

    public OfflineFileRepository(Context context) {
        AppDatabase db = AppDatabase.getDatabase(context);
        mOfflineFileDao = db.offlineFileDao();
    }

    public OfflineFile getFileWithPathAndName(String path, String name) {
        return mOfflineFileDao.getOfflineFileWithPathAndName(path, name);
    }

    public void insert (OfflineFile offlineFile) {
        new insertAsyncTask(mOfflineFileDao).execute(offlineFile);
    }

    public void update(OfflineFile offlineFile) {
        new updateAsyncTask(mOfflineFileDao).execute(offlineFile);
    }

    public void delete (OfflineFile offlineFile) {
        new deleteAsyncTask(mOfflineFileDao).execute(offlineFile);
    }

    public OfflineFile getFileWithDownloadId(long downloadId) {
        return mOfflineFileDao.getOfflineFileWithDownloadId(downloadId);
    }

    private static class insertAsyncTask extends AsyncTask<OfflineFile, Void, Void> {

        private OfflineFileDao mAsyncTaskDao;

        insertAsyncTask(OfflineFileDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final OfflineFile... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

    private static class updateAsyncTask extends AsyncTask<OfflineFile, Void, Void> {

        private OfflineFileDao mAsyncTaskDao;

        updateAsyncTask(OfflineFileDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final OfflineFile... params) {
            mAsyncTaskDao.update(params[0]);
            return null;
        }
    }

    private static class deleteAsyncTask extends AsyncTask<OfflineFile, Void, Void> {

        private OfflineFileDao mAsyncTaskDao;

        deleteAsyncTask(OfflineFileDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final OfflineFile... params) {
            mAsyncTaskDao.delete(params[0]);
            return null;
        }
    }
}
