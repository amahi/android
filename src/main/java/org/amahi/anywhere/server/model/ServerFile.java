package org.amahi.anywhere.server.model;

import android.text.format.Time;

import com.google.gson.annotations.SerializedName;

public class ServerFile
{
	@SerializedName("name")
	private String name;

	@SerializedName("mtime")
	private String modificationTime;

	@SerializedName("mime_type")
	private String mime;

	@SerializedName("size")
	private String size;

	public String getName() {
		return name;
	}

	public Time getModificationTime() {
		Time time = new Time();

		time.parse3339(modificationTime);

		return time;
	}

	public String getMime() {
		return mime;
	}

	public String getSize() {
		return size;
	}
}
