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

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class ServerApp implements Parcelable
{
	@SerializedName("name")
	private String name;

	@SerializedName("logo")
	private String logoUrl;

	public String getName() {
		return name;
	}

	public String getLogoUrl() {
		return logoUrl;
	}

	public static final Creator<ServerApp> CREATOR = new Creator<ServerApp>()
	{
		@Override
		public ServerApp createFromParcel(Parcel parcel) {
			return new ServerApp(parcel);
		}

		@Override
		public ServerApp[] newArray(int size) {
			return new ServerApp[size];
		}
	};

	private ServerApp(Parcel parcel) {
		this.name = parcel.readString();
		this.logoUrl = parcel.readString();
	}

	@Override
	public void writeToParcel(Parcel parcel, int flags) {
		parcel.writeString(name);
	}

	@Override
	public int describeContents() {
		return 0;
	}
}
