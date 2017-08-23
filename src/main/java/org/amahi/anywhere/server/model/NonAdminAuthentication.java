package org.amahi.anywhere.server.model;

import com.google.gson.annotations.SerializedName;

public class NonAdminAuthentication {
    @SerializedName("session_token")
    private String sessionToken;

    @SerializedName("server_name")
    private String serverName;

    @SerializedName("server_address")
    private String serverAddress;

    public String getServerName() {
        return serverName;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public String getSessionToken() {
        return sessionToken;
    }
}
