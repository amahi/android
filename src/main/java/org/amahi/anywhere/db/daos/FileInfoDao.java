package org.amahi.anywhere.db.daos;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import org.amahi.anywhere.db.entities.FileInfo;

@Dao
public interface FileInfoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(FileInfo fileInfo);

    @Update
    void update(FileInfo fileInfo);

    @Query("SELECT * FROM file_info_table where uniqueKey = :uniqueKey")
    FileInfo getFileInfo(String uniqueKey);


}
