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

package org.amahi.anywhere.server.client;

import android.content.Context;

import org.amahi.anywhere.server.Api;
import org.amahi.anywhere.server.ApiAdapter;
import org.amahi.anywhere.server.api.AmahiApi;
import org.amahi.anywhere.server.response.AuthenticationResponse;
import org.amahi.anywhere.server.response.ServersResponse;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Amahi API implementation. Wraps {@link org.amahi.anywhere.server.api.AmahiApi}.
 */
@Singleton
public class AmahiClient {
    private final AmahiApi api;

    @Inject
    public AmahiClient(ApiAdapter apiAdapter) {
        this.api = buildApi(apiAdapter);
    }

    private AmahiApi buildApi(ApiAdapter apiAdapter) {
        return apiAdapter.create(AmahiApi.class, Api.getAmahiUrl());
    }

    public void authenticate(String username, String password) {
        api.authenticate(Api.getClientId(), Api.getClientSecret(), username, password).enqueue(new AuthenticationResponse());
    }

    public void authenticate(String pin) {
        //api.authenticate(Api.getClientId(), Api.getClientSecret(), pin).enqueue(new AuthenticationResponse());
        // TODO check if any previous HDAs and authenticate by pin id
        // TODO save auth_token
        // TODO logout
    }

    public void getServers(Context context, String authenticationToken) {
        api.getServers(authenticationToken).enqueue(new ServersResponse(context));
    }
}
