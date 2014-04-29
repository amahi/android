package org.amahi.anywhere.server.response;

import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.ServersLoadedEvent;
import org.amahi.anywhere.server.model.Server;

import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ServersResponse extends ApiResponse implements Callback<List<Server>>
{
	@Override
	public void success(List<Server> servers, Response response) {
		BusProvider.getBus().post(new ServersLoadedEvent(servers));
	}

	@Override
	public void failure(RetrofitError error) {
		BusProvider.getBus().post(getFailureEvent(error));
	}
}
