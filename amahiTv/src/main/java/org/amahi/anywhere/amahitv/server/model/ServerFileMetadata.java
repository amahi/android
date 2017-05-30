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

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class ServerFileMetadata implements Parcelable
{
	@SerializedName("title")
	private String title;

	@SerializedName("artwork")
	private String artworkUrl;

	public String getTitle() {
		return title;
	}

	public String getArtworkUrl() {
		return artworkUrl;
	}

	public static final Creator<ServerFileMetadata> CREATOR = new Creator<ServerFileMetadata>()
	{
		@Override
		public ServerFileMetadata createFromParcel(Parcel parcel) {
			return new ServerFileMetadata(parcel);
		}

		@Override
		public ServerFileMetadata[] newArray(int size) {
			return new ServerFileMetadata[size];
		}
	};

	private ServerFileMetadata(Parcel parcel) {
		this.title = parcel.readString();
		this.artworkUrl = parcel.readString();
	}

	@Override
	public void writeToParcel(Parcel parcel, int flags) {
		parcel.writeString(title);
		parcel.writeString(artworkUrl);
	}

	@Override
	public int describeContents() {
		return 0;
	}
}
