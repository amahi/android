package org.amahi.anywhere.db.daos;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import org.amahi.anywhere.db.entities.PlayedFile;

@Dao
public interface PlayedFileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(PlayedFile playedFile);

    @Update
    void update(PlayedFile playedFile);

    @Query("DELETE FROM play_file_table")
    void deleteAll();

    @Query("SELECT * from play_file_table where uniqueKey = :uniqueKey")
    PlayedFile getPlayedFile(String uniqueKey);

    @Query("DELETE FROM play_file_table where uniqueKey = :uniqueKey")
    void delete(String uniqueKey);
}
