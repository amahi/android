package org.amahi.anywhere.server.model;

import android.text.format.Time;

import com.google.gson.annotations.SerializedName;

public class ServerShare
{
	@SerializedName("name")
	private String name;

	@SerializedName("mtime")
	private String modificationTime;

	public String getName() {
		return name;
	}

	public Time getModificationTime() {
		Time time = new Time();

		time.parse3339(modificationTime);

		return time;
	}
}
