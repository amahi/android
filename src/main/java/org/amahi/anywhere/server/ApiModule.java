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

import org.amahi.anywhere.BuildConfig;
import org.amahi.anywhere.util.Time;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
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
public class ApiModule {

    /**
     * After enabling the chuck dependencies modify the function definition below to pass ChuckInterceptor:
     * provideHttpClient(ApiHeaders headers, HttpLoggingInterceptor logging, ChuckInterceptor chuck)
     * <p>
     * Add the Chuck interceptor when building OkHttpClient, using:
     * clientBuilder.addInterceptor(chuck);
     */

    @Provides
    @Singleton
    OkHttpClient provideHttpClient(ApiHeaders headers, HttpLoggingInterceptor logging) {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();

        clientBuilder.addInterceptor(headers);
        clientBuilder.addInterceptor(logging);

        return clientBuilder.build();
    }

    @Provides
    @Singleton
    ApiHeaders provideHeaders(Context context) {
        return new ApiHeaders(context);
    }

    /**
     * Creating an instance for ChuckInterceptor and providing it with context
     * Uncomment the code below if using Chuck Interceptor for logging
     */
/*
    @Provides
    @Singleton
    ChuckInterceptor provideChuckInterceptor(Context context) {
        return new ChuckInterceptor(context);
    }
*/
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
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();

        if (BuildConfig.DEBUG) {
            // This level can be decreased to Level.HEADERS or Level.BASIC to reduce the details shown
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        } else {
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.NONE);
        }

        return loggingInterceptor;
    }
}
