package org.amahi.anywhere.db.daos;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import org.amahi.anywhere.db.entities.HDA;

import java.util.List;

@Dao
public interface HDADao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(HDA hda);

    @Query("SELECT * FROM hda_cache")
    List<HDA> getAllHDAs();
}
