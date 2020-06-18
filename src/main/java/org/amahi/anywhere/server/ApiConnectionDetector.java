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

import android.content.res.Resources;
import android.net.Uri;

import org.amahi.anywhere.R;
import org.amahi.anywhere.server.model.ServerRoute;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

/**
 * API connection guesser. Tries to connect to the server address to determine if it is available
 * and returns it if succeed or another one otherwise.
 */
public class ApiConnectionDetector {
    private OkHttpClient httpClient;

    public ApiConnectionDetector() {
        this.httpClient = buildHttpClient();
    }

    private OkHttpClient buildHttpClient() {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        clientBuilder.connectTimeout(Connection.TIMEOUT, TimeUnit.SECONDS);
        httpClient = clientBuilder.build();
        return httpClient;
    }

    public String detect(ServerRoute serverRoute) {
        Timber.tag(Resources.getSystem().getString(R.string.connection));

        try {
            Request httpRequest = new Request.Builder()
                .url(getConnectionUrl(serverRoute.getLocalAddress()))
                .build();

            Response httpResponse = httpClient
                .newCall(httpRequest)
                .execute();

            httpResponse.body().close();

            Timber.d("Using local address.");

            return serverRoute.getLocalAddress();
        } catch (IOException e) {
            Timber.d("Using remote address.");

            return serverRoute.getRemoteAddress();
        }
    }

    private URL getConnectionUrl(String serverAddress) throws IOException {
        return new URL(Uri.parse(serverAddress).buildUpon().appendPath(Resources.getSystem().getString(R.string.shares)).build().toString());
    }

    private static final class Connection {
        static final int TIMEOUT = 1;

        private Connection() {
        }
    }
}
