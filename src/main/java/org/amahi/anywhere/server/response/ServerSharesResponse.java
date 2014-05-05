package org.amahi.anywhere.server.response;

import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.ServerSharesLoadedEvent;
import org.amahi.anywhere.server.model.ServerShare;

import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ServerSharesResponse extends ApiResponse implements Callback<List<ServerShare>>
{
	@Override
	public void success(List<ServerShare> serverShares, Response response) {
		BusProvider.getBus().post(new ServerSharesLoadedEvent(serverShares));
	}

	@Override
	public void failure(RetrofitError error) {
		BusProvider.getBus().post(getFailureEvent(error));
	}
}
