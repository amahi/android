package org.amahi.anywhere.server.client;

import com.squareup.okhttp.OkHttpClient;

import org.amahi.anywhere.server.Api;
import org.amahi.anywhere.server.api.AmahiApi;
import org.amahi.anywhere.server.header.ApiHeaders;
import org.amahi.anywhere.server.model.Server;

import java.util.List;

import retrofit.RestAdapter;
import retrofit.client.OkClient;

public class AmahiClient
{
	private final AmahiApi api;

	public AmahiClient() {
		this.api = buildApi();
	}

	private AmahiApi buildApi() {
		RestAdapter apiAdapter = new RestAdapter.Builder()
			.setEndpoint(Api.getAmahiUrl())
			.setClient(new OkClient(new OkHttpClient()))
			.setRequestInterceptor(new ApiHeaders())
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
