package org.amahi.anywhere.server.api;

import org.amahi.anywhere.server.model.ServerRoute;

import retrofit.http.PUT;
import retrofit.http.Query;

public interface ProxyApi
{
	@PUT("/client")
	public ServerRoute getServerRoute(
		@Query("session") String session);
}
