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

package org.amahi.anywhere.server;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.amahi.anywhere.server.model.Server;
import org.amahi.anywhere.server.model.ServerApp;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ApiResourceFetcher
{
	private final OkHttpClient httpClient;

	@Inject
	public ApiResourceFetcher(OkHttpClient httpClient) {
		this.httpClient = httpClient;
	}

	public ApiResource fetch(Server server, ServerApp serverApp, String appResourceUrl) throws IOException {
		Request apiRequest = new Request.Builder()
			.url(appResourceUrl)
			.addHeader("Session", server.getSession())
			.addHeader("Vhost", serverApp.getHost())
			.build();

		Response apiResponse = httpClient
			.newCall(apiRequest)
			.execute();

		return new ApiResource(
			apiResponse.body().byteStream(),
			String.format(Locale.US, "%s/%s",
				apiResponse.body().contentType().type(),
				apiResponse.body().contentType().subtype()),
			apiResponse.body().contentType().charset(Charset.defaultCharset()).name());
	}
}
