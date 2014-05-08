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

import org.amahi.anywhere.server.Api;
import org.amahi.anywhere.server.api.AmahiApi;
import org.amahi.anywhere.server.header.ApiHeaders;
import org.amahi.anywhere.server.response.AuthenticationResponse;
import org.amahi.anywhere.server.response.ServersResponse;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit.RestAdapter;
import retrofit.client.Client;

@Singleton
public class AmahiClient
{
	private final AmahiApi api;

	@Inject
	public AmahiClient(Client client, ApiHeaders headers) {
		this.api = buildApi(client, headers);
	}

	private AmahiApi buildApi(Client client, ApiHeaders headers) {
		RestAdapter apiAdapter = new RestAdapter.Builder()
			.setEndpoint(Api.getAmahiUrl())
			.setClient(client)
			.setRequestInterceptor(headers)
			.build();

		return apiAdapter.create(AmahiApi.class);
	}

	public void getAuthenticationToken(String username, String password) {
		api.authenticate(Api.getClientId(), Api.getClientSecret(), username, password, new AuthenticationResponse());
	}

	public void getServers(String authenticationToken) {
		api.getServers(Api.getClientToken(), new ServersResponse());
	}
}
