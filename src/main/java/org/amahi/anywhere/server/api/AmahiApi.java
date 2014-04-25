package org.amahi.anywhere.server.api;

import org.amahi.anywhere.server.model.Authentication;
import org.amahi.anywhere.server.model.Server;

import java.util.List;

import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Query;

public interface AmahiApi
{
	@FormUrlEncoded
	@POST("/api2/oauth/token?grant_type=password")
	public Authentication authenticate(
		@Field("client_id") String clientId,
		@Field("client_secret") String clientSecret,
		@Field("username") String username,
		@Field("password") String password);

	@GET("/api2/servers")
	public List<Server> getServers(
		@Query("access_token") String authenticationToken);
}
