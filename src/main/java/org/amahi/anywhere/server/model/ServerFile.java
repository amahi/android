/*
 * Copyright (c) 2014 Amahi
 *
 * This file is part of Amahi.
 *
 * Amahi is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Amahi is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Amahi. If not, see <http ://www.gnu.org/licenses/>.
 */

package org.amahi.anywhere.server.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import org.amahi.anywhere.util.Time;

import java.util.Date;

public class ServerFile implements Parcelable
{
	private ServerFile parentFile;

	@SerializedName("name")
	private String name;

	@SerializedName("mtime")
	private String modificationTime;

	@SerializedName("mime_type")
	private String mime;

	@SerializedName("size")
	private long size;

	public void setParentFile(ServerFile parentFile) {
		this.parentFile = parentFile;
	}

	public ServerFile getParentFile() {
		return parentFile;
	}

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

	public String getPath() {
		Uri.Builder uri = new Uri.Builder();

		if (parentFile != null) {
			uri.appendPath(parentFile.getPath());
		}

		uri.appendPath(name);

		return uri.build().getPath();
	}

	public static final Creator<ServerFile> CREATOR = new Creator<ServerFile>()
	{
		@Override
		public ServerFile createFromParcel(Parcel parcel) {
			return new ServerFile(parcel);
		}

		@Override
		public ServerFile[] newArray(int size) {
			return new ServerFile[size];
		}
	};

	private ServerFile(Parcel parcel) {
		this.parentFile = parcel.readParcelable(this.getClass().getClassLoader());
		this.name = parcel.readString();
		this.modificationTime = parcel.readString();
		this.mime = parcel.readString();
		this.size = parcel.readLong();
	}

	@Override
	public void writeToParcel(Parcel parcel, int flags) {
		parcel.writeParcelable(parentFile, flags);
		parcel.writeString(name);
		parcel.writeString(modificationTime);
		parcel.writeString(mime);
		parcel.writeLong(size);
	}

	@Override
	public int describeContents() {
		return 0;
	}
}
