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

import org.amahi.anywhere.bus.AuthExpiredEvent;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.ForbiddenAccessEvent;
import org.amahi.anywhere.bus.ServerSharesLoadFailedEvent;
import org.amahi.anywhere.bus.ServerSharesLoadedEvent;
import org.amahi.anywhere.server.model.ServerShare;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.HttpException;
import retrofit2.Response;

/**
 * Shares response proxy. Consumes API callback and posts it via {@link com.squareup.otto.Bus}
 * as {@link org.amahi.anywhere.bus.BusEvent}.
 */
public class ServerSharesResponse implements Callback<List<ServerShare>> {
    @Override
    public void onResponse(Call<List<ServerShare>> call, Response<List<ServerShare>> response) {
        switch (response.code()) {
            case 200:
                BusProvider.getBus().post(new ServerSharesLoadedEvent(response.body()));
                break;
            case 401:
                BusProvider.getBus().post(new AuthExpiredEvent());
                break;
            case 403:
                BusProvider.getBus().post(new ForbiddenAccessEvent());
                break;
            default:
                this.onFailure(call, new HttpException(response));
        }
    }

    @Override
    public void onFailure(Call<List<ServerShare>> call, Throwable t) {
        BusProvider.getBus().post(new ServerSharesLoadFailedEvent());
    }
}
