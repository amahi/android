package org.amahi.anywhere.db.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

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
