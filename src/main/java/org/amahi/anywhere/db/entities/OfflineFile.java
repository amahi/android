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
    private String path;
    @NonNull
    private String name;
    private String fileUri;
    private long timeStamp;
    @Types
    private int state;
    private long downloadId;

    public OfflineFile(@NonNull String path, @NonNull String name) {
        this.path = path;
        this.name = name;
    }

    @NonNull
    public String getPath() {
        return path;
    }

    public void setPath(@NonNull String path) {
        this.path = path;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    public String getFileUri() {
        return fileUri;
    }

    public void setFileUri(String fileUri) {
        this.fileUri = fileUri;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public long getDownloadId() {
        return downloadId;
    }

    public void setDownloadId(long downloadId) {
        this.downloadId = downloadId;
    }

    @IntDef({DOWNLOADING, DOWNLOADED, OUT_OF_DATE})
    public @interface Types {
    }
}
