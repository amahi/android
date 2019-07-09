package org.amahi.anywhere.server.model;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class PrimaryUser {
    @SerializedName("id")
    private int id;

    @SerializedName("created_at")
    private Date createdDate;

    @SerializedName("amahi_user")
    private FriendUser friendUser;

    public int getId() {
        return id;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public FriendUser getFriendUser() {
        return friendUser;
    }
}

