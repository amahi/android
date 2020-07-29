package org.amahi.anywhere.db.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "file_info_table")
public class FileInfo {

    @PrimaryKey
    @NonNull
    private String uniqueKey;

    private String lastOpened;

    public FileInfo(String uniqueKey, String lastOpened) {
        this.uniqueKey = uniqueKey;
        this.lastOpened = lastOpened;
    }

    public String getUniqueKey() {
        return uniqueKey;
    }

    public void setUniqueKey(String uniqueKey) {
        this.uniqueKey = uniqueKey;
    }

    public String getLastOpened() {
        return lastOpened;
    }

    public void setLastOpened(String lastOpened) {
        this.lastOpened = lastOpened;
    }
}
