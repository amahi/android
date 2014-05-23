package org.amahi.anywhere.server;

import javax.inject.Inject;

import retrofit.RestAdapter;
import retrofit.client.Client;

public class ApiAdapter
{
	private final RestAdapter.Builder apiBuilder;

	@Inject
	public ApiAdapter(Client client, ApiHeaders headers, RestAdapter.Log log, RestAdapter.LogLevel logLevel) {
		this.apiBuilder = buildApiBuilder(client, headers, log, logLevel);
	}

	private RestAdapter.Builder buildApiBuilder(Client client, ApiHeaders headers, RestAdapter.Log log, RestAdapter.LogLevel logLevel) {
		return new RestAdapter.Builder()
			.setClient(client)
			.setRequestInterceptor(headers)
			.setLog(log)
			.setLogLevel(logLevel);
	}

	public <T> T create(Class<T> api, String apiUrl) {
		return apiBuilder.setEndpoint(apiUrl).build().create(api);
	}
}
