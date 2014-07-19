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

import android.app.Application;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;

import org.amahi.anywhere.util.Time;

import java.io.File;
import java.io.IOException;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit.RestAdapter.Log;
import retrofit.RestAdapter.LogLevel;
import retrofit.client.Client;
import retrofit.client.OkClient;
import retrofit.converter.Converter;
import retrofit.converter.GsonConverter;

@Module(
	complete = false,
	library = true
)
public class ApiModule
{
	@Provides
	@Singleton
	ApiAdapter provideApiAdapter(Client client, ApiHeaders headers, Converter converter, Log log, LogLevel logLevel) {
		return new ApiAdapter(client, headers, converter, log, logLevel);
	}

	@Provides
	@Singleton
	Client provideClient(OkHttpClient httpClient) {
		return new OkClient(httpClient);
	}

	@Provides
	@Singleton
	OkHttpClient provideHttpClient() {
		return new OkHttpClient();
	}

	@Provides
	@Singleton
	Cache provideHttpCache(Application application) {
		try {
			File cacheDirectory = new File(application.getCacheDir(), "http-cache");
			int cacheSize = 5 * 1024 * 1024;

			return new Cache(cacheDirectory, cacheSize);
		} catch (IOException e) {
			return null;
		}
	}

	@Provides
	@Singleton
	ApiHeaders provideHeaders(Application application) {
		return new ApiHeaders(application);
	}

	@Provides
	@Singleton
	Converter provideJsonConverter(Gson json) {
		return new GsonConverter(json);
	}

	@Provides
	@Singleton
	Gson provideJson() {
		return new GsonBuilder().setDateFormat(Time.Format.RFC_1123).create();
	}

	@Provides
	@Singleton
	Log provideLog() {
		return new ApiLog();
	}

	@Provides
	@Singleton
	LogLevel provideLogLevel() {
		return LogLevel.HEADERS;
	}
}
