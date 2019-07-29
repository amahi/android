package org.amahi.anywhere.db.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "hda_cache")
public class HDA {

    @PrimaryKey
    @NonNull
    private String ip;

    public HDA(@NonNull String ip) {
        this.ip = ip;
    }

    @NonNull
    public String getIp() {
        return ip;
    }

    public void setIp(@NonNull String ip) {
        this.ip = ip;
    }
}
