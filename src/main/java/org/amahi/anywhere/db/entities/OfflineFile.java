package org.amahi.anywhere.db.entities;

import android.arch.persistence.room.Entity;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

@Entity(tableName = "offline_table", primaryKeys = {"path", "name"})
public class OfflineFile {

    public static final int DOWNLOADING = 1;
    public static final int DOWNLOADED = 2;
    public static final int OUT_OF_DATE = 3;

    @NonNull
    public String path;
    @NonNull
    public String name;
    public Long timeStamp;

    @Types
    public int state;
    public long downloadID;

    @IntDef({DOWNLOADING, DOWNLOADED, OUT_OF_DATE})
    public @interface Types {
    }
}
