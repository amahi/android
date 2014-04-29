package org.amahi.anywhere.server.response;

import org.amahi.anywhere.bus.BusEvent;
import org.amahi.anywhere.bus.ConnectionNotAuthorizedEvent;
import org.amahi.anywhere.bus.ConnectionTimeoutEvent;

import java.net.HttpURLConnection;

import retrofit.RetrofitError;

class ApiResponse
{
	protected BusEvent getFailureEvent(RetrofitError error) {
		switch (error.getResponse().getStatus()) {
			case HttpURLConnection.HTTP_UNAUTHORIZED:
				return new ConnectionNotAuthorizedEvent();

			case HttpURLConnection.HTTP_CLIENT_TIMEOUT:
				return new ConnectionTimeoutEvent();

			default:
				return null;
		}
	}
}
