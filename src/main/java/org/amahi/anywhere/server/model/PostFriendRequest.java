package org.amahi.anywhere.server.model;

import com.google.gson.annotations.SerializedName;

public class PostFriendRequest {

    @SerializedName("email")
    private String email;

    @SerializedName("pin")
    private int pin;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getPin() {
        return pin;
    }

    public void setPin(int pin) {
        this.pin = pin;
    }
}
