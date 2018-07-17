package org.amahi.anywhere.db.repositories;

import android.content.Context;

import org.amahi.anywhere.db.AppDatabase;
import org.amahi.anywhere.db.daos.HDADao;
import org.amahi.anywhere.db.entities.HDA;

import java.util.List;

public class HDARepository {

    private HDADao mDao;

    public HDARepository(Context context) {
        AppDatabase db = AppDatabase.getDatabase(context);
        mDao = db.hdaDao();
    }

    public void insert(HDA hda) {
        mDao.insert(hda);
    }

    public List<HDA> getAllHDAs() {
        return mDao.getAllHDAs();
    }
}
