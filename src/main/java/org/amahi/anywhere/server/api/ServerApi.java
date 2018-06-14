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

import org.amahi.anywhere.server.model.HdaAuthBody;
import org.amahi.anywhere.server.model.HdaAuthResponse;
import org.amahi.anywhere.server.model.ServerApp;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerFileMetadata;
import org.amahi.anywhere.server.model.ServerShare;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

/**
 * Server API declaration.
 */
public interface ServerApi {
    @POST("/auth")
    Call<HdaAuthResponse> authenticate(
        @Header("Session") String session,
        @Body HdaAuthBody authBody);

    @GET("/shares")
    Call<List<ServerShare>> getShares(
        @Header("Session") String session,
        @Header("Authorization") String authToken);

    @GET("/files")
    Call<List<ServerFile>> getFiles(
        @Header("Session") String session,
        @Header("Authorization") String authToken,
        @Query("s") String share,
        @Query("p") String path);

    @DELETE("/files")
    Call<Void> deleteFile(
        @Header("Session") String session,
        @Header("Authorization") String authToken,
        @Query("s") String share,
        @Query("p") String path);

    @Multipart
    @POST("/files")
    Call<ResponseBody> uploadFile(
        @Header("Session") String session,
        @Header("Authorization") String authToken,
        @Query("s") String share,
        @Query("p") String path,
        @Part MultipartBody.Part file);

    @GET("/md")
    Call<ServerFileMetadata> getFileMetadata(
        @Header("Session") String session,
        @Query("f") String fileName,
        @Query("h") String hint);

    @GET("/apps")
    Call<List<ServerApp>> getApps(
        @Header("Session") String session);
}
