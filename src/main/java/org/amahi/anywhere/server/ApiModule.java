package org.amahi.anywhere.server;

import com.squareup.okhttp.OkHttpClient;

import org.amahi.anywhere.server.client.AmahiClient;
import org.amahi.anywhere.server.client.ProxyClient;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.header.ApiHeaders;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit.client.Client;
import retrofit.client.OkClient;

@Module(
	library = true
)
public class ApiModule
{
	@Provides
	@Singleton
	OkHttpClient provideHttpClient() {
		return new OkHttpClient();
	}

	@Provides
	@Singleton
	Client provideClient(OkHttpClient httpClient) {
		return new OkClient(httpClient);
	}

	@Provides
	@Singleton
	ApiHeaders provideHeaders() {
		return new ApiHeaders();
	}

	@Provides
	@Singleton
	AmahiClient provideAmahiClient(Client client, ApiHeaders headers) {
		return new AmahiClient(client, headers);
	}

	@Provides
	@Singleton
	ProxyClient provideProxyClient(Client client, ApiHeaders headers) {
		return new ProxyClient(client, headers);
	}

	@Provides
	@Singleton
	ServerClient provideServerClient(ProxyClient proxyClient, Client client, ApiHeaders headers) {
		return new ServerClient(proxyClient, client, headers);
	}
}
