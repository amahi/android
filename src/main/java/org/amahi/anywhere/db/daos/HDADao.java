package org.amahi.anywhere.db.daos;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import org.amahi.anywhere.db.entities.HDA;

import java.util.List;

@Dao
public interface HDADao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(HDA hda);

    @Query("SELECT * FROM hda_cache")
    List<HDA> getAllHDAs();
}
