package org.amahi.anywhere.server.model;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class FriendUser {
    @SerializedName("id")
    private int id;

    @SerializedName("created_at")
    private Date createdDate;

    @SerializedName("email")
    private String email;


    public int getId() {
        return id;
    }

    public Date getCreatedDate() {
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

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }
}
