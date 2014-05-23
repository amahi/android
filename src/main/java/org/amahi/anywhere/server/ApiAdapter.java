package org.amahi.anywhere.server;

import javax.inject.Inject;

import retrofit.RestAdapter.Builder;
import retrofit.RestAdapter.Log;
import retrofit.RestAdapter.LogLevel;
import retrofit.client.Client;

public class ApiAdapter
{
	private final Builder apiBuilder;

	@Inject
	public ApiAdapter(Client client, ApiHeaders headers, Log log, LogLevel logLevel) {
		this.apiBuilder = buildApiBuilder(client, headers, log, logLevel);
	}

	private Builder buildApiBuilder(Client client, ApiHeaders headers, Log log, LogLevel logLevel) {
		return new Builder()
			.setClient(client)
			.setRequestInterceptor(headers)
			.setLog(log)
			.setLogLevel(logLevel);
	}

	public <T> T create(Class<T> api, String apiUrl) {
		return apiBuilder.setEndpoint(apiUrl).build().create(api);
	}
}
