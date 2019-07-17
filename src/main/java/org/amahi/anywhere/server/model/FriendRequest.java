package org.amahi.anywhere.server.model;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class FriendRequest {

    @SerializedName("id")
    private int id;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("status")
    private int status;

    @SerializedName("amahi_user")
    private FriendUser friendUser;

    @SerializedName("last_requested_at")
    private String lastRequestedAt;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public FriendUser getFriendUser() {
        return friendUser;
    }

    public void setFriendUser(FriendUser friendUser) {
        this.friendUser = friendUser;
    }

    public String getLastRequestedAt() {
        return lastRequestedAt;
    }

    public void setLastRequestedAt(String lastRequestedAt) {
        this.lastRequestedAt = lastRequestedAt;
    }


}
