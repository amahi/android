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
import org.amahi.anywhere.bus.ServerRouteLoadFailedEvent;
import org.amahi.anywhere.bus.ServerRouteLoadedEvent;
import org.amahi.anywhere.server.model.ServerRoute;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.HttpException;
import retrofit2.Response;

/**
 * Server route response proxy. Consumes API callback and posts it via {@link com.squareup.otto.Bus}
 * as {@link org.amahi.anywhere.bus.BusEvent}.
 */
public class ServerRouteResponse implements Callback<ServerRoute> {
    @Override
    public void onResponse(Call<ServerRoute> call, Response<ServerRoute> response) {
        if (response.isSuccessful())
            BusProvider.getBus().post(new ServerRouteLoadedEvent(response.body()));
        else
            this.onFailure(call, new HttpException(response));
    }

    @Override
    public void onFailure(Call<ServerRoute> call, Throwable t) {
        BusProvider.getBus().post(new ServerRouteLoadFailedEvent(t.getMessage()));
    }
}
