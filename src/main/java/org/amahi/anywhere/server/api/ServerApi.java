/*
 * Copyright (c) 2014 Amahi
 *
 * This file is part of Amahi.
 *
 * Amahi is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Amahi is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Amahi. If not, see <http ://www.gnu.org/licenses/>.
 */

package org.amahi.anywhere.server.api;

import org.amahi.anywhere.server.model.ServerApp;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerFileMetadata;
import org.amahi.anywhere.server.model.ServerShare;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

/**
 * Server API declaration.
 */
public interface ServerApi
{
	@GET("/shares")
	Call<List<ServerShare>> getShares(
		@Header("Session") String session);

	@GET("/files")
	Call<List<ServerFile>> getFiles(
		@Header("Session") String session,
		@Query("s") String share,
		@Query("p") String path);

	@GET("/md")
	Call<ServerFileMetadata> getFileMetadata(
		@Header("Session") String session,
		@Query("f") String fileName,
		@Query("h") String hint);

	@GET("/apps")
	Call<List<ServerApp>> getApps(
		@Header("Session") String session);
}
