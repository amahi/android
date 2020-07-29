package org.amahi.anywhere.db.repositories;

import android.content.Context;

import org.amahi.anywhere.db.AppDatabase;
import org.amahi.anywhere.db.daos.FileInfoDao;
import org.amahi.anywhere.db.entities.FileInfo;

public class FileInfoRepository {

    private FileInfoDao fileInfoDao;

    public FileInfoRepository(Context context) {
        AppDatabase db = AppDatabase.getDatabase(context);
        fileInfoDao = db.fileInfoDao();

    }

    public FileInfo getFileInfo(String uniqueKey) {
        return fileInfoDao.getFileInfo(uniqueKey);
    }

    public void insert(FileInfo fileInfo) {
        fileInfoDao.insert(fileInfo);
    }

    public void update(FileInfo fileInfo) {
        fileInfoDao.update(fileInfo);
    }


}
