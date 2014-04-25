package org.amahi.anywhere.server.model;

import com.google.gson.annotations.SerializedName;

public class Server
{
	@SerializedName("name")
	private String name;

	@SerializedName("session_token")
	private String session;

	@SerializedName("active")
	private boolean active;

	public String getName() {
		return name;
	}

	public String getSession() {
		return session;
	}

	public boolean isActive() {
		return active;
	}
}
