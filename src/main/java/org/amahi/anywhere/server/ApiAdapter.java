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

import javax.inject.Inject;

import retrofit.RestAdapter.Builder;
import retrofit.RestAdapter.Log;
import retrofit.RestAdapter.LogLevel;
import retrofit.client.Client;

public class ApiAdapter
{
	private final Builder apiBuilder;

	@Inject
	public ApiAdapter(Client client, ApiHeaders headers, Log log, LogLevel logLevel) {
		this.apiBuilder = buildApiBuilder(client, headers, log, logLevel);
	}

	private Builder buildApiBuilder(Client client, ApiHeaders headers, Log log, LogLevel logLevel) {
		return new Builder()
			.setClient(client)
			.setRequestInterceptor(headers)
			.setLog(log)
			.setLogLevel(logLevel);
	}

	public <T> T create(Class<T> api, String apiUrl) {
		return apiBuilder.setEndpoint(apiUrl).build().create(api);
	}
}
