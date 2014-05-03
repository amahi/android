package org.amahi.anywhere.server;

import android.app.Application;

import com.squareup.okhttp.HttpResponseCache;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.OkResponseCache;

import org.amahi.anywhere.server.header.ApiHeaders;

import java.io.File;
import java.io.IOException;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit.client.Client;
import retrofit.client.OkClient;

@Module(
	complete = false,
	library = true
)
public class ApiModule
{
	@Provides
	@Singleton
	Client provideClient(OkHttpClient httpClient) {
		return new OkClient(httpClient);
	}

	@Provides
	@Singleton
	OkHttpClient provideHttpClient(OkResponseCache httpCache) {
		OkHttpClient httpClient = new OkHttpClient();

		httpClient.setOkResponseCache(httpCache);

		return httpClient;
	}

	@Provides
	@Singleton
	OkResponseCache provideHttpCache(Application application) {
		try {
			File cacheDirectory = new File(application.getCacheDir(), "http-cache");
			int cacheSize = 5 * 1024 * 1024;

			return new HttpResponseCache(cacheDirectory, cacheSize);
		} catch (IOException e) {
			return null;
		}
	}

	@Provides
	@Singleton
	ApiHeaders provideHeaders() {
		return new ApiHeaders();
	}
}
