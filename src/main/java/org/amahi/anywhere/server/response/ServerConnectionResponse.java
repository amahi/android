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
import org.amahi.anywhere.bus.ServerConnectionChosenEvent;
import org.amahi.anywhere.server.model.ServerRoute;
import org.amahi.anywhere.server.model.ServerShare;

import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ServerConnectionResponse extends ApiResponse implements Callback<List<ServerShare>>
{
	private final ServerRoute serverRoute;

	public ServerConnectionResponse(ServerRoute serverRoute) {
		this.serverRoute = serverRoute;
	}

	@Override
	public void success(List<ServerShare> serverShares, Response response) {
		BusProvider.getBus().post(new ServerConnectionChosenEvent(serverRoute.getLocalAddress()));
	}

	@Override
	public void failure(RetrofitError error) {
		BusProvider.getBus().post(new ServerConnectionChosenEvent(serverRoute.getRemoteAddress()));
	}
}
