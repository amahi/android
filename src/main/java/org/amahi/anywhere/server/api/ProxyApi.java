package org.amahi.anywhere.server.api;

import org.amahi.anywhere.server.model.ServerRoute;

import retrofit.http.PUT;

public interface ProxyApi
{
	@PUT("/client")
	public ServerRoute getServerRoute();
}
