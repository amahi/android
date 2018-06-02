package org.amahi.anywhere.db.daos;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import org.amahi.anywhere.db.entities.OfflineFile;

import java.util.List;

@Dao
public interface OfflineFileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(OfflineFile offlineFile);

    @Update
    void update(OfflineFile offlineFile);

    @Query("DELETE FROM offline_table")
    void deleteAll();

    @Query("SELECT * from offline_table where share = :share and path = :path and name = :name")
    OfflineFile getOfflineFile(String share, String path, String name);

    @Query("SELECT * from offline_table where downloadId != -1")
    OfflineFile getCurrentDownloadingFile();

    @Delete
    void delete(OfflineFile offlineFile);

    @Query("SELECT * from offline_table where downloadId = :downloadId")
    OfflineFile getOfflineFileWithDownloadId(long downloadId);

    @Query("SELECT * from offline_table where state = :state")
    OfflineFile getOfflineFileWithState(int state);

    @Query("SELECT * from offline_table")
    List<OfflineFile> getAllOfflineFiles();

    @Query("SELECT * from offline_table where name = :name and timeStamp = :timeStamp")
    OfflineFile getOfflineFile(String name, long timeStamp);
}
