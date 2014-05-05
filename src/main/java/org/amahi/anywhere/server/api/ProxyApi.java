package org.amahi.anywhere.server.api;

import org.amahi.anywhere.server.model.ServerRoute;

import retrofit.Callback;
import retrofit.http.Header;
import retrofit.http.PUT;

public interface ProxyApi
{
	@PUT("/client")
	public void getServerRoute(
		@Header("Session") String session,
		Callback<ServerRoute> callback);
}
