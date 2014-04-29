package org.amahi.anywhere.server.response;

import org.amahi.anywhere.bus.AuthenticationDoneEvent;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.server.model.Authentication;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class AuthenticationResponse extends ApiResponse implements Callback<Authentication>
{
	@Override
	public void success(Authentication authentication, Response response) {
		BusProvider.getBus().post(new AuthenticationDoneEvent(authentication));
	}

	@Override
	public void failure(RetrofitError error) {
		BusProvider.getBus().post(getFailureEvent(error));
	}
}
