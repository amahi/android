package org.amahi.anywhere.server.api;

import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerShare;

import java.util.List;

import retrofit.http.GET;
import retrofit.http.Query;

public interface ServerApi
{
	@GET("/shares")
	public List<ServerShare> getShares(
		@Query("session") String session);

	@GET("/files")
	public List<ServerFile> getFiles(
		@Query("session") String session,
		@Query("s") String share,
		@Query("p") String path);
}
