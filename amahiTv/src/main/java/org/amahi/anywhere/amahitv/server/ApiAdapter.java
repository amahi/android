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

package org.amahi.anywhere.amahitv.server;

import javax.inject.Inject;
import javax.inject.Singleton;

import okhttp3.OkHttpClient;
import retrofit2.Converter.Factory;
import retrofit2.Retrofit;


/**
 * API adapter. Wraps {@link Retrofit}, building API implementations using
 * dependency injection provided components.
 */
@Singleton
public class ApiAdapter
{
	private final Retrofit.Builder apiBuilder;

	@Inject
	public ApiAdapter(OkHttpClient client, Factory factory) {
		this.apiBuilder = buildApiBuilder(client, factory);
	}

	private Retrofit.Builder buildApiBuilder(OkHttpClient client, Factory factory) {
		return new Retrofit.Builder()
			.client(client)
			.addConverterFactory(factory);
	}

	public <T> T create(Class<T> api, String apiUrl) {
		return apiBuilder.baseUrl(apiUrl).build().create(api);
	}
}
