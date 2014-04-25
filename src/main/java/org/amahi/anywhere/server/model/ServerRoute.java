package org.amahi.anywhere.server.model;

import com.google.gson.annotations.SerializedName;

public class ServerRoute
{
	@SerializedName("local_addr")
	private String localAddress;

	@SerializedName("relay_addr")
	private String remoteAddress;

	public String getLocalAddress() {
		return localAddress;
	}

	public String getRemoteAddress() {
		return remoteAddress;
	}
}
