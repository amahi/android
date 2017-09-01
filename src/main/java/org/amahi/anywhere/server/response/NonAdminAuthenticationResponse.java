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

import org.amahi.anywhere.bus.AuthenticationConnectionFailedEvent;
import org.amahi.anywhere.bus.AuthenticationFailedEvent;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.NonAdminAuthSucceedEvent;
import org.amahi.anywhere.server.model.NonAdminAuthentication;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.HttpException;
import retrofit2.Response;

public class NonAdminAuthenticationResponse implements Callback<NonAdminAuthentication> {
    @Override
    public void onResponse(Call<NonAdminAuthentication> call, Response<NonAdminAuthentication> response) {
        if (response.isSuccessful()) {
            BusProvider.getBus().post(new NonAdminAuthSucceedEvent(response.body()));
        } else {
            this.onFailure(call, new HttpException(response));
        }
    }

    @Override
    public void onFailure(Call<NonAdminAuthentication> call, Throwable t) {
        if (t instanceof IOException) {
            BusProvider.getBus().post(new AuthenticationConnectionFailedEvent());
        } else {
            BusProvider.getBus().post(new AuthenticationFailedEvent());
        }
    }
}
