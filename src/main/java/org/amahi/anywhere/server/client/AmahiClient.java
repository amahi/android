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
		api.getServers(authenticationToken, new ServersResponse());
	}
}
