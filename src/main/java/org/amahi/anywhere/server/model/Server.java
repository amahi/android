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

/**
 * Server API resource.
 */
public class Server implements Parcelable
{
	@SerializedName("name")
	private String name;

	@SerializedName("session_token")
	private String session;

	@SerializedName("active")
	private boolean active;

	@SerializedName("server_address")
	private String serverAddress;

	public Server(String name, String session, String serverAddress, boolean active) {
		this.name = name;
		this.session = session;
		this.serverAddress = serverAddress;
		this.active = active;
	}

	public String getName() {
		return name;
	}

	public String getSession() {
		return session;
	}

	public boolean isActive() {
		return active;
	}

	public String getServerAddress() {
		return serverAddress;
	}

	public static final Creator<Server> CREATOR = new Creator<Server>()
	{
		@Override
		public Server createFromParcel(Parcel parcel) {
			return new Server(parcel);
		}

		@Override
		public Server[] newArray(int size) {
			return new Server[size];
		}
	};

	private Server(Parcel parcel) {
		this.name = parcel.readString();
		this.session = parcel.readString();
		this.active = Boolean.valueOf(parcel.readString());
		this.serverAddress = parcel.readString();
	}

	@Override
	public void writeToParcel(Parcel parcel, int flags) {
		parcel.writeString(name);
		parcel.writeString(session);
		parcel.writeString(String.valueOf(active));
		parcel.writeString(serverAddress);
	}

	@Override
	public int describeContents() {
		return 0;
	}
}
