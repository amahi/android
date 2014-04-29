package org.amahi.anywhere.server.client;

import org.amahi.anywhere.server.Api;
import org.amahi.anywhere.server.api.ProxyApi;
import org.amahi.anywhere.server.header.ApiHeaders;
import org.amahi.anywhere.server.model.Server;
import org.amahi.anywhere.server.model.ServerRoute;

import retrofit.RestAdapter;
import retrofit.client.Client;

class ProxyClient
{
	private final ProxyApi api;

	public ProxyClient(Client client, ApiHeaders headers) {
		this.api = buildApi(client, headers);
	}

	private ProxyApi buildApi(Client client, ApiHeaders headers) {
		RestAdapter apiAdapter = new RestAdapter.Builder()
			.setEndpoint(Api.getProxyUrl())
			.setClient(client)
			.setRequestInterceptor(headers)
			.build();

		return apiAdapter.create(ProxyApi.class);
	}

	public ServerRoute getRoute(Server server) {
		return api.getServerRoute(server.getSession());
	}
}
