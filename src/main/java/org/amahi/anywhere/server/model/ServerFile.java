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
import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

/**
 * File API resource.
 */
public class ServerFile implements Parcelable {
    public static final Creator<ServerFile> CREATOR = new Creator<ServerFile>() {
        @Override
        public ServerFile createFromParcel(Parcel parcel) {
            return new ServerFile(parcel);
        }

        @Override
        public ServerFile[] newArray(int size) {
            return new ServerFile[size];
        }
    };
    private ServerFile parentFile;
    private ServerShare parentShare;
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
    private boolean isOffline;

    public ServerFile(String name, long timeStamp, String mime) {
        this.name = name;
        this.modificationTime = new Date(timeStamp);
        this.mime = mime;
    }

    private ServerFile(Parcel parcel) {
        this.parentFile = parcel.readParcelable(ServerFile.class.getClassLoader());
        this.name = parcel.readString();
        this.mime = parcel.readString();
        this.modificationTime = new Date(parcel.readLong());
        this.size = parcel.readLong();
    }

    public static int compareByName(ServerFile a, ServerFile b) {
        return a.getName().compareTo(b.getName());
    }

    public static int compareByModificationTime(ServerFile a, ServerFile b) {
        return -a.getModificationTime().compareTo(b.getModificationTime());
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public ServerShare getParentShare() {
        return parentShare;
    }

    public void setParentShare(ServerShare parentShare) {
        this.parentShare = parentShare;
    }

    public ServerFile getParentFile() {
        return parentFile;
    }

    public void setParentFile(ServerFile parentFile) {
        this.parentFile = parentFile;
    }

    public String getName() {
        return name;
    }

    public String getNameOnly() {
        return name.replace("." + getExtension(), "");
    }

    public String getMime() {
        return mime;
    }

    public void setMime(String mime) {
        this.mime = mime;
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

    public boolean isOffline() {
        return isOffline;
    }

    public void setOffline(boolean offline) {
        isOffline = offline;
    }

    @Nullable
    public String getUniqueKey() {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(getName().getBytes());
            md.update(getModificationTime().toString().getBytes());
            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte aDigest : digest) {
                sb.append(Integer.toHexString((aDigest & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
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

    public String getExtension() {
        String[] splitString = name.split("\\.");
        if (splitString.length > 1) {
            return splitString[splitString.length - 1];
        } else {
            return "";
        }
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

        return modificationTime.equals(file.modificationTime);
    }
}
