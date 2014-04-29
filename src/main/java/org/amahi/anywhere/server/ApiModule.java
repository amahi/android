package org.amahi.anywhere.server;

import com.squareup.okhttp.OkHttpClient;

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
}
