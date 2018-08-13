package org.amahi.anywhere.db.entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.net.Uri;
import android.support.annotation.NonNull;

@Entity(tableName = "recent_file_table")
public class RecentFile {

    @PrimaryKey
    @NonNull
    private String uniqueKey;

    private String uri;
    private String serverName;

    private long visitTime;
    private long size;
    @NonNull
    private String mime;
    private long modificationTime;

    public RecentFile(@NonNull String uniqueKey, String uri, String serverName, long visitTime, long size,
                      @NonNull String mime, long modificationTime) {
        this.uniqueKey = uniqueKey;
        this.uri = uri;
        this.serverName = serverName;
        this.visitTime = visitTime;
        this.size = size;
        this.mime = mime;
        this.modificationTime = modificationTime;
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

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getName() {
        Uri uri = Uri.parse(getUri());
        return Uri.parse(uri.getQueryParameter("p")).getLastPathSegment();
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getShareName() {
        Uri uri = Uri.parse(getUri());
        return uri.getQueryParameter("s");
    }

    public String getPath() {
        Uri uri = Uri.parse(getUri());
        return uri.getQueryParameter("p");
    }

    public String getMime() {
        return mime;
    }

    public void setMime(String mimeType) {
        this.mime = mime;
    }

    public long getModificationTime() {
        return modificationTime;
    }

    public void setModificationTime(long modificationTime) {
        this.modificationTime = modificationTime;
    }
}
