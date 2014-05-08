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

import org.amahi.anywhere.BuildConfig;

public final class Api
{
	private Api() {
	}

	public static String getAmahiUrl() {
		return BuildConfig.API_URL_AMAHI;
	}

	public static String getProxyUrl() {
		return BuildConfig.API_URL_PROXY;
	}

	public static String getClientId() {
		return BuildConfig.API_CLIENT_ID;
	}

	public static String getClientSecret() {
		return BuildConfig.API_CLIENT_SECRET;
	}

	public static String getClientToken() {
		return BuildConfig.API_CLIENT_TOKEN;
	}
}
