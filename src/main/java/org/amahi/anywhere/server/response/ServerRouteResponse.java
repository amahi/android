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

import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.ServerConnectionFailedEvent;
import org.amahi.anywhere.bus.ServerRouteLoadedEvent;
import org.amahi.anywhere.server.model.ServerRoute;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ServerRouteResponse implements Callback<ServerRoute>
{
	@Override
	public void success(ServerRoute serverRoute, Response response) {
		BusProvider.getBus().post(new ServerRouteLoadedEvent(serverRoute));
	}

	@Override
	public void failure(RetrofitError error) {
		BusProvider.getBus().post(new ServerConnectionFailedEvent());
	}
}
