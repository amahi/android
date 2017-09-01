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
import org.amahi.anywhere.bus.NonAdminPublicKeySucceedEvent;
import org.amahi.anywhere.server.model.NonAdminPublicKey;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.HttpException;
import retrofit2.Response;


public class NonAdminPublicKeyResponse implements Callback<NonAdminPublicKey> {
    @Override
    public void onResponse(Call<NonAdminPublicKey> call, Response<NonAdminPublicKey> response) {
        if (response.isSuccessful()) {
            BusProvider.getBus().post(new NonAdminPublicKeySucceedEvent(response.body()));
        } else {
            this.onFailure(call, new HttpException(response));
        }
    }

    @Override
    public void onFailure(Call<NonAdminPublicKey> call, Throwable t) {
        //Note -@octacode: For debugging assumed a success even on a fail and used mock data in the latter activities.
        //BusProvider.getBus().post(new NonAdminPublicKeySucceedEvent(new NonAdminPublicKey()));
    }
}
