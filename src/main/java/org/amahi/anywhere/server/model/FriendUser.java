package org.amahi.anywhere.server.model;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class FriendUser {
    @SerializedName("id")
    private int id;

    @SerializedName("created_at")
    private String createdDate;

    @SerializedName("email")
    private String email;


    public int getId() {
        return id;
    }

    public String getCreatedDate() {
        return createdDate;
    }


    public String getEmail() {
        return email;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }
}
