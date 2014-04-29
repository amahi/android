package org.amahi.anywhere.server.client;

import org.amahi.anywhere.server.Api;
import org.amahi.anywhere.server.api.AmahiApi;
import org.amahi.anywhere.server.header.ApiHeaders;
import org.amahi.anywhere.server.model.Server;

import java.util.List;

import javax.inject.Inject;

import retrofit.RestAdapter;
import retrofit.client.Client;

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

	public String getAuthenticationToken(String username, String password) {
		return api.authenticate(Api.getClientId(), Api.getClientSecret(), username, password).getToken();
	}

	public List<Server> getServers(String authenticationToken) {
		return api.getServers(authenticationToken);
	}
}
