package org.amahi.anywhere.server.client;

import com.squareup.okhttp.OkHttpClient;

import org.amahi.anywhere.server.Api;
import org.amahi.anywhere.server.api.ProxyApi;
import org.amahi.anywhere.server.header.ApiHeaders;
import org.amahi.anywhere.server.model.Server;
import org.amahi.anywhere.server.model.ServerRoute;

import retrofit.RestAdapter;
import retrofit.client.OkClient;

class ProxyClient
{
	private final ProxyApi api;

	public ProxyClient() {
		this.api = buildApi();
	}

	private ProxyApi buildApi() {
		RestAdapter apiAdapter = new RestAdapter.Builder()
			.setEndpoint(Api.getProxyUrl())
			.setClient(new OkClient(new OkHttpClient()))
			.setRequestInterceptor(new ApiHeaders())
			.build();

		return apiAdapter.create(ProxyApi.class);
	}

	public ServerRoute getRoute(Server server) {
		return api.getServerRoute(server.getSession());
	}
}
