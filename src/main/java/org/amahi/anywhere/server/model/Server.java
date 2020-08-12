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

import java.util.ArrayList;
import java.util.List;

/**
 * Server API resource.
 */
public class Server implements Parcelable {
    public static final Creator<Server> CREATOR = new Creator<Server>() {
        @Override
        public Server createFromParcel(Parcel parcel) {
            return new Server(parcel);
        }

        @Override
        public Server[] newArray(int size) {
            return new Server[size];
        }
    };
    @SerializedName("name")
    private String name;
    @SerializedName("session_token")
    private String session;
    @SerializedName("active")
    private boolean active;
    private boolean debug = false;
    private int index;

    public Server(int index, String name, String session) {
        this.index = index;
        this.name = name;
        this.session = session;
        this.active = true;
        this.debug = true;
    }

    public Server(String session) {
        this.session = session;
    }

    public Server(Parcel parcel) {
        this.name = parcel.readString();
        this.session = parcel.readString();
        this.active = Boolean.valueOf(parcel.readString());
    }

    public static List<Server> filterActiveServers(List<Server> servers) {
        List<Server> activeServers = new ArrayList<>();
        for (Server server : servers) {
            if (server.isActive()) {
                activeServers.add(server);
            }
        }
        return activeServers;
    }

    public String getName() {
        return name;
    }

    public String getSession() {
        return session;
    }

    public int getIndex() {
        return index;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isDebug() {
        return debug;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(name);
        parcel.writeString(session);
        parcel.writeString(String.valueOf(active));
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
