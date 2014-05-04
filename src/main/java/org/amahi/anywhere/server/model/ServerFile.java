package org.amahi.anywhere.server.model;

import com.google.gson.annotations.SerializedName;

import org.amahi.anywhere.util.Time;

import java.util.Date;

public class ServerFile
{
	@SerializedName("name")
	private String name;

	@SerializedName("mtime")
	private String modificationTime;

	@SerializedName("mime_type")
	private String mime;

	@SerializedName("size")
	private long size;

	public String getName() {
		return name;
	}

	public Date getModificationTime() {
		return Time.parseRfc1123(modificationTime);
	}

	public String getMime() {
		return mime;
	}

	public long getSize() {
		return size;
	}
}
