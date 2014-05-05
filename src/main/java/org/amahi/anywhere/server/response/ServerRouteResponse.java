package org.amahi.anywhere.server.response;

import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.ServerRouteLoadedEvent;
import org.amahi.anywhere.server.model.ServerRoute;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ServerRouteResponse extends ApiResponse implements Callback<ServerRoute>
{
	@Override
	public void success(ServerRoute serverRoute, Response response) {
		BusProvider.getBus().post(new ServerRouteLoadedEvent(serverRoute));
	}

	@Override
	public void failure(RetrofitError error) {
		BusProvider.getBus().post(getFailureEvent(error));
	}
}
