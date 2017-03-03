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

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jakewharton.byteunits.BinaryByteUnit;

import org.amahi.anywhere.util.Time;

import java.io.File;
import java.io.IOException;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Converter;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * API dependency injection module. Provides resources such as HTTP client and JSON converter
 * for possible consumers.
 */
@Module(
	complete = false,
	library = true
)
public class ApiModule
{
	@Provides
	@Singleton
	OkHttpClient provideHttpClient(Cache httpCache, ApiHeaders headers, HttpLoggingInterceptor logging) {
		OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
		clientBuilder.addInterceptor(headers);
		clientBuilder.addInterceptor(logging);
		clientBuilder.cache(httpCache);
		return clientBuilder.build();
	}

	@Provides
	@Singleton
	Cache provideHttpCache(Context context) {
		File cacheDirectory = new File(context.getCacheDir(), "http-cache");
		long cacheSize = BinaryByteUnit.MEBIBYTES.toBytes(10);
		return new Cache(cacheDirectory, cacheSize);
	}

	@Provides
	@Singleton
	ApiHeaders provideHeaders(Context context) {
		return new ApiHeaders(context);
	}

	@Provides
	@Singleton
	Converter.Factory provideJsonConverterFactory(Gson json) {
		return GsonConverterFactory.create(json);
	}

	@Provides
	@Singleton
	Gson provideJson() {
		return new GsonBuilder().setDateFormat(Time.Format.RFC_1123).create();
	}

	@Provides
	@Singleton
	HttpLoggingInterceptor provideLogging() {
		return new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.HEADERS);
	}
}
