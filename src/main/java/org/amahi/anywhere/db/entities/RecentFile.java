package org.amahi.anywhere.db.entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "recent_file_table")
public class RecentFile {

    @PrimaryKey
    @NonNull
    private String uniqueKey;

    private String uri;

    private long visitTime;

    public RecentFile(@NonNull String uniqueKey, String uri, long visitTime) {
        this.uniqueKey = uniqueKey;
        this.uri = uri;
        this.visitTime = visitTime;
    }

    @NonNull
    public String getUniqueKey() {
        return uniqueKey;
    }

    public void setUniqueKey(@NonNull String uniqueKey) {
        this.uniqueKey = uniqueKey;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public long getVisitTime() {
        return visitTime;
    }

    public void setVisitTime(long visitTime) {
        this.visitTime = visitTime;
    }
}
