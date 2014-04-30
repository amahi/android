package org.amahi.anywhere.server.model;

import com.google.gson.annotations.SerializedName;

import org.amahi.anywhere.util.Time;

import java.util.Date;

public class ServerShare
{
	@SerializedName("name")
	private String name;

	@SerializedName("mtime")
	private String modificationTime;

	public String getName() {
		return name;
	}

	public Date getModificationTime() {
		return Time.parseRfc1123(modificationTime);
	}
}
