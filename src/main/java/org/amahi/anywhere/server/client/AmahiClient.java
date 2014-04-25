package org.amahi.anywhere.server.client;

import com.squareup.okhttp.OkHttpClient;

import org.amahi.anywhere.server.Api;
import org.amahi.anywhere.server.api.AmahiApi;
import org.amahi.anywhere.server.header.ApiHeaders;
import org.amahi.anywhere.server.model.Server;

import java.util.List;

import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;

public class AmahiClient
{
	private final AmahiApi api;

	private final String authenticationToken;

	public static AmahiClient with(String authenticationToken) {
		return new AmahiClient(authenticationToken);
	}

	private AmahiClient(String authenticationToken) {
		this.api = buildApi();

		this.authenticationToken = authenticationToken;
	}

	private AmahiApi buildApi() {
		RestAdapter apiAdapter = new RestAdapter.Builder()
			.setEndpoint(Api.getAmahiUrl())
			.setClient(new OkClient(new OkHttpClient()))
			.setRequestInterceptor(new ApiHeaders())
			.build();

		return apiAdapter.create(AmahiApi.class);
	}

	public List<Server> getServers() {
		try {
			return api.getServers(authenticationToken);
		} catch (RetrofitError e) {
			throw new RuntimeException(e);
		}
	}
}
