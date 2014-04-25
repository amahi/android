package org.amahi.anywhere.server.model;

import com.google.gson.annotations.SerializedName;

public class Authentication
{
	@SerializedName("access_token")
	private String token;

	public String getToken() {
		return token;
	}
}
