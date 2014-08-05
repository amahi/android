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

import android.net.Uri;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.amahi.anywhere.server.model.ServerRoute;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

public class ApiConnectionDetector
{
	private static final class Connection
	{
		private Connection() {
		}

		public static final int TIMEOUT = 1;
	}

	private final OkHttpClient httpClient;

	public ApiConnectionDetector() {
		this.httpClient = buildHttpClient();
	}

	private OkHttpClient buildHttpClient() {
		OkHttpClient httpClient = new OkHttpClient();

		httpClient.setConnectTimeout(Connection.TIMEOUT, TimeUnit.SECONDS);

		return httpClient;
	}

	public String detect(ServerRoute serverRoute) {
		Timber.tag("CONNECTION");

		try {
			Request httpRequest = new Request.Builder()
				.url(getConnectionUrl(serverRoute.getLocalAddress()))
				.build();

			Response httpResponse = httpClient
				.newCall(httpRequest)
				.execute();

			Timber.d("Using local address.");

			return serverRoute.getLocalAddress();
		} catch (IOException e) {
			Timber.d("Using remote address.");

			return serverRoute.getRemoteAddress();
		}
	}

	private URL getConnectionUrl(String serverAddress) throws IOException {
		return new URL(Uri.parse(serverAddress).buildUpon().appendPath("shares").build().toString());
	}
}
