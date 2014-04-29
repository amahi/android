package org.amahi.anywhere.server.api;

import org.amahi.anywhere.server.model.Authentication;
import org.amahi.anywhere.server.model.Server;

import java.util.List;

import retrofit.Callback;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Query;

public interface AmahiApi
{
	@FormUrlEncoded
	@POST("/api2/oauth/token?grant_type=password")
	public void authenticate(
		@Field("client_id") String clientId,
		@Field("client_secret") String clientSecret,
		@Field("username") String username,
		@Field("password") String password,
		Callback<Authentication> callback);

	@GET("/api2/servers")
	public void getServers(
		@Query("access_token") String authenticationToken,
		Callback<List<Server>> callback);
}
