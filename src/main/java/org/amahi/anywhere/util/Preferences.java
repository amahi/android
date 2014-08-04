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

package org.amahi.anywhere.util;

import android.content.Context;
import android.content.SharedPreferences;

public final class Preferences
{
	private static final class Locations
	{
		private Locations() {
		}

		public static final String COOKIE = "cookie";
	}

	private static final class Defaults
	{
		private Defaults() {
		}

		public static final String STRING = "";
	}

	private final SharedPreferences preferences;

	public static Preferences ofCookie(Context context) {
		return new Preferences(context, Locations.COOKIE);
	}

	private Preferences(Context context, String location) {
		this.preferences = context.getSharedPreferences(location, Context.MODE_PRIVATE);
	}

	public String getAppCookies(String appHost) {
		return getString(appHost);
	}

	private String getString(String key) {
		return preferences.getString(key, Defaults.STRING);
	}

	public void setAppCookies(String appHost, String appCookies) {
		setString(appHost, appCookies);
	}

	private void setString(String key, String value) {
		preferences.edit().putString(key, value).apply();
	}
}
