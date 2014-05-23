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

import org.amahi.anywhere.util.Android;

import java.util.Locale;

import retrofit.RequestInterceptor;

class ApiHeaders implements RequestInterceptor
{
	private static final class HeaderFields
	{
		private HeaderFields() {
		}

		public static final String ACCEPT = "Accept";
		public static final String USER_AGENT = "User-Agent";
	}

	private static final class HeaderValues
	{
		private HeaderValues() {
		}

		public static final String ACCEPT = "application/json";
		public static final String USER_AGENT = "AmahiAnywhere/%s (Android %s; %s) Size/%.1f Resolution/%dx%d";
	}

	private final String acceptHeader;
	private final String userAgentHeader;

	public ApiHeaders(Context context) {
		this.acceptHeader = getAcceptHeader();
		this.userAgentHeader = getUserAgentHeader(context);
	}

	private String getAcceptHeader() {
		return HeaderValues.ACCEPT;
	}

	private String getUserAgentHeader(Context context) {
		return String.format(Locale.US, HeaderValues.USER_AGENT,
			Android.getApplicationVersion(),
			Android.getVersion(),
			Android.getDeviceName(),
			Android.getDeviceScreenSize(context),
			Android.getDeviceScreenHeight(context),
			Android.getDeviceScreenWidth(context));
	}

	@Override
	public void intercept(RequestFacade request) {
		request.addHeader(HeaderFields.ACCEPT, acceptHeader);
		request.addHeader(HeaderFields.USER_AGENT, userAgentHeader);
	}
}
