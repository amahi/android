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

package org.amahi.anywhere.amahitv.server.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * File API resource.
 */
public class ServerFile implements Parcelable
{
	private ServerFile parentFile;

	@SerializedName("name")
	private String name;

	@SerializedName("mime_type")
	private String mime;

	@SerializedName("mtime")
	private Date modificationTime;

    @SerializedName("size")
    private long size;

    private ServerFileMetadata fileMetadata;

	private boolean isMetaDataFetched;

    public long getSize() {
            return size;
       }

    public void setParentFile(ServerFile parentFile) {
		this.parentFile = parentFile;
	}

	public ServerFile getParentFile() {
		return parentFile;
	}

	public String getName() {
		return name;
	}

	public String getMime() {
		return mime;
	}

	public Date getModificationTime() {
		return modificationTime;
	}

    public ServerFileMetadata getFileMetadata() {
        return fileMetadata;
    }

    public void setFileMetadata(ServerFileMetadata fileMetadata) {
        this.fileMetadata = fileMetadata;
    }

	public boolean isMetaDataFetched() {
		return isMetaDataFetched;
	}

	public void setMetaDataFetched(boolean metaDataFetched) {
		isMetaDataFetched = metaDataFetched;
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
		this.parentFile = parcel.readParcelable(ServerFile.class.getClassLoader());
		this.name = parcel.readString();
		this.mime = parcel.readString();
		this.modificationTime = new Date(parcel.readLong());
              this.size= parcel.readLong();
	}

	@Override
	public void writeToParcel(Parcel parcel, int flags) {
		parcel.writeParcelable(parentFile, flags);
		parcel.writeString(name);
		parcel.writeString(mime);
		parcel.writeLong(modificationTime.getTime());
              parcel.writeLong(size);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (object == null) {
			return false;
		}

		if (getClass() != object.getClass()) {
			return false;
		}

		ServerFile file = (ServerFile) object;

		if ((parentFile != null) && (!parentFile.equals(file.parentFile))) {
			return false;
		}

		if (!name.equals(file.name)) {
			return false;
		}

		if (!mime.equals(file.mime)) {
			return false;
		}

		if (!modificationTime.equals(file.modificationTime)) {
			return false;
		}

		return true;
	}
}
