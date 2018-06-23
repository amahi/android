package org.amahi.anywhere.db.entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "play_file_table")
public class PlayedFile {

    @PrimaryKey
    @NonNull
    private String uniqueKey;
    private long position;

    public PlayedFile(String uniqueKey, long position) {
        this.uniqueKey = uniqueKey;
        this.position = position;
    }

    public String getUniqueKey() {
        return uniqueKey;
    }

    public void setUniqueKey(String uniqueKey) {
        this.uniqueKey = uniqueKey;
    }

    public long getPosition() {
        return position;
    }

    public void setPosition(long position) {
        this.position = position;
    }
}
