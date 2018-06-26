package org.amahi.anywhere.db.daos;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import org.amahi.anywhere.db.entities.RecentFile;

import java.util.List;

@Dao
public interface RecentFileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(RecentFile recentFile);

    @Update
    void update(RecentFile recentFile);

    @Query("DELETE FROM recent_file_table")
    void deleteAll();

    @Query("DELETE FROM recent_file_table where uniqueKey = :uniqueKey")
    void delete(String uniqueKey);

    @Query("SELECT * from recent_file_table where uniqueKey = :uniqueKey")
    RecentFile getRecentFile(String uniqueKey);

    @Query("SELECT * from recent_file_table where serverName = :serverName ORDER BY visitTime DESC")
    List<RecentFile> getRecentFiles(String serverName);

    @Query("SELECT * FROM recent_file_table ORDER BY visitTime DESC LIMIT 1")
    RecentFile getLastInsertFile();

    @Query("SELECT * FROM recent_file_table ORDER BY visitTime ASC LIMIT 1")
    RecentFile getOldestFile();
}
