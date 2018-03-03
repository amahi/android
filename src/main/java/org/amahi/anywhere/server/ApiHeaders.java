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

import org.amahi.anywhere.util.Identifier;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;


/**
 * API headers accessor.
 */
class ApiHeaders implements Interceptor {
    private final String acceptHeader;
    private final String userAgentHeader;

    public ApiHeaders(Context context) {
        this.acceptHeader = getAcceptHeader();
        this.userAgentHeader = getUserAgentHeader(context);
    }

    private String getAcceptHeader() {
        return "application/json";
    }

    private String getUserAgentHeader(Context context) {
        return Identifier.getUserAgent(context);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        Request request = original.newBuilder()
            .addHeader(HeaderFields.ACCEPT, acceptHeader)
            .addHeader(HeaderFields.USER_AGENT, userAgentHeader)
            .method(original.method(), original.body())
            .build();

        return chain.proceed(request);
    }

    private static final class HeaderFields {
        public static final String ACCEPT = "Accept";
        public static final String USER_AGENT = "User-Agent";

        private HeaderFields() {
        }
    }
}
