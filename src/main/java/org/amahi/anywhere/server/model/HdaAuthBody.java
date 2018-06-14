package org.amahi.anywhere.server.model;

import com.google.gson.annotations.SerializedName;

public class HdaAuthBody {
    @SerializedName("pin")
    private String pin;

    public HdaAuthBody(String pin) {
        this.pin = pin;
    }
}
