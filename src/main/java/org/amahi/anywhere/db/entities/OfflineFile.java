package org.amahi.anywhere.db.entities;

import android.arch.persistence.room.Entity;
import android.support.annotation.NonNull;

@Entity(tableName = "offline_table", primaryKeys = {"path", "name"})
public class OfflineFile {

    @NonNull
    public String path;
    @NonNull
    public String name;
    public Long timeStamp;
    public String state;
}
