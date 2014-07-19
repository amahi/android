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
		@Header("Session") String session,
		Callback<List<ServerShare>> callback);

	@GET("/files")
	public void getFiles(
		@Header("Session") String session,
		@Query("s") String share,
		@Query("p") String path,
		Callback<List<ServerFile>> callback);

	@GET("/apps")
	public void getApps(
		@Header("Session") String session,
		Callback<List<ServerApp>> callback);
}
