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

import org.amahi.anywhere.server.model.NonAdminAuthentication;
import org.amahi.anywhere.server.model.NonAdminPublicKey;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * NonAdminApi API declaration
 */
public interface NonAdminApi {
    @GET("/key")
    Call<NonAdminPublicKey> getNonAdminPublicKey(
    );

    @FormUrlEncoded
    @POST("/api2/oauth/token?grant_type=password")
    Call<NonAdminAuthentication> authenticate(
        @Field("client_id") String clientId,
        @Field("client_secret") String clientSecret,
        @Field("username") String username,
        @Field("password") String password);
}
