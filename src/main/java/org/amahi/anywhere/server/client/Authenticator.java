package org.amahi.anywhere.server.client;

import android.content.Context;

import com.squareup.okhttp.OkHttpClient;

import org.amahi.anywhere.server.Api;
import org.amahi.anywhere.server.api.AmahiApi;
import org.amahi.anywhere.server.header.ApiHeaders;

import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;

public class Authenticator
{
	private final AmahiApi api;

	public static Authenticator with(Context context) {
		return new Authenticator(context);
	}

	private Authenticator(Context context) {
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

	public String authenticate(String username, String password) {
		try {
			return api.authenticate(Api.getClientId(), Api.getClientSecret(), username, password).getToken();
		} catch (RetrofitError e) {
			throw new RuntimeException(e);
		}
	}
}
