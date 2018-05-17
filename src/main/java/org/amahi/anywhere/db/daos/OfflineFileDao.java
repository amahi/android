package org.amahi.anywhere.db.daos;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import org.amahi.anywhere.db.entities.OfflineFile;

import java.util.List;

@Dao
public interface OfflineFileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(OfflineFile offlineFile);

    @Query("DELETE FROM offline_table")
    void deleteAll();

    @Query("SELECT * from offline_table where path = :path")
    List<OfflineFile> getAllOfflineFilesWithPath(String path);

    @Query("SELECT * from offline_table where path = :path and name = :name")
    OfflineFile getOfflineFileWithPathAndName(String path, String name);

    @Delete
    void delete(OfflineFile offlineFile);
}
