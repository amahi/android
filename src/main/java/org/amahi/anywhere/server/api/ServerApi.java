package org.amahi.anywhere.server.api;

import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerShare;

import java.util.List;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Query;

public interface ServerApi
{
	@GET("/shares")
	public void getShares(
		@Header("session") String session,
		Callback<List<ServerShare>> callback);

	@GET("/files")
	public void getFiles(
		@Header("Session") String session,
		@Query("s") String share,
		@Query("p") String path,
		Callback<List<ServerFile>> callback);
}
