package org.amahi.anywhere.server.response;

import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.ServerFilesLoadedEvent;
import org.amahi.anywhere.server.model.ServerFile;

import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ServerFilesResponse extends ApiResponse implements Callback<List<ServerFile>>
{
	@Override
	public void success(List<ServerFile> serverFiles, Response response) {
		BusProvider.getBus().post(new ServerFilesLoadedEvent(serverFiles));
	}

	@Override
	public void failure(RetrofitError error) {
		BusProvider.getBus().post(getFailureEvent(error));
	}
}
