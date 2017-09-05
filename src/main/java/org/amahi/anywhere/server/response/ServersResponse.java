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

package org.amahi.anywhere.server.response;

import android.content.Context;

import org.amahi.anywhere.BuildConfig;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.ServersLoadFailedEvent;
import org.amahi.anywhere.bus.ServersLoadedEvent;
import org.amahi.anywhere.server.model.Server;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.HttpException;
import retrofit2.Response;

import static org.amahi.anywhere.util.Android.loadServersFromAsset;

/**
 * Servers response proxy. Consumes API callback and posts it via {@link com.squareup.otto.Bus}
 * as {@link org.amahi.anywhere.bus.BusEvent}.
 */
public class ServersResponse implements Callback<List<Server>>
{
	private Context context;

	public ServersResponse(Context context) {
		this.context = context;
	}

	private List<Server> getLocalServers() {
		List<Server> servers = new ArrayList<>();
		try {
			JSONArray jsonArray = new JSONArray(loadServersFromAsset(context));
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				Server server = new Server(i, jsonObject.getString("name"),
						jsonObject.getString("session_token"));
				servers.add(server);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return servers;
	}


	@Override
	public void onResponse(Call<List<Server>> call, Response<List<Server>> response) {
		if (response.isSuccessful()) {
			List<Server> servers = response.body();
			if (BuildConfig.DEBUG) {
				servers.addAll(getLocalServers());
			}
			BusProvider.getBus().post(new ServersLoadedEvent(servers));
		} else
			this.onFailure(call, new HttpException(response));
	}

	@Override
	public void onFailure(Call<List<Server>> call, Throwable t) {
		BusProvider.getBus().post(new ServersLoadFailedEvent());
	}
}
