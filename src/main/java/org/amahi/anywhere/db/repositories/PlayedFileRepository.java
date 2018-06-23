package org.amahi.anywhere.db.repositories;

import android.content.Context;

import org.amahi.anywhere.db.AppDatabase;
import org.amahi.anywhere.db.daos.PlayedFileDao;
import org.amahi.anywhere.db.entities.PlayedFile;

public class PlayedFileRepository {

    private PlayedFileDao mPlayedFileDao;

    public PlayedFileRepository(Context context) {
        AppDatabase db = AppDatabase.getDatabase(context);
        mPlayedFileDao = db.playedFileDao();
    }

    public PlayedFile getPlayedFile(String uniqueKey) {
        return mPlayedFileDao.getPlayedFile(uniqueKey);
    }

    public void insert(PlayedFile playedFile) {
        mPlayedFileDao.insert(playedFile);
    }

    public void update(PlayedFile playedFile) {
        mPlayedFileDao.update(playedFile);
    }

    public void delete(String uniqueKey) {
        mPlayedFileDao.delete(uniqueKey);
    }
}
