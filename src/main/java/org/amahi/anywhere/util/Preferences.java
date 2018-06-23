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
import android.preference.PreferenceManager;

import org.amahi.anywhere.R;
import org.amahi.anywhere.fragment.ServerFilesFragment;

import static android.content.Context.MODE_PRIVATE;

/**
 * Application {@link android.content.SharedPreferences} accessor.
 */
public final class Preferences {
    private final SharedPreferences preferences;

    private Preferences(Context context, String location) {
        this.preferences = context.getSharedPreferences(location, MODE_PRIVATE);
    }

    public static SharedPreferences getTVPreference(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static String getServerConnection(SharedPreferences preferences, Context context) {
        return preferences.getString(context.getString(R.string.preference_key_server_connection), context.getString(R.string.preference_entry_server_connection_auto));
    }

    public static void setPrefAuto(SharedPreferences preference, Context context) {
        preference.edit().putString(context.getString(R.string.preference_key_server_connection), context.getString(R.string.preference_entry_server_connection_auto)).apply();
    }

    public static void setPrefRemote(SharedPreferences preference, Context context) {
        preference.edit().putString(context.getString(R.string.preference_key_server_connection), context.getString(R.string.preference_entry_server_connection_remote)).apply();
    }

    public static void setPrefLocal(SharedPreferences preference, Context context) {
        preference.edit().putString(context.getString(R.string.preference_key_server_connection), context.getString(R.string.preference_entry_server_connection_local)).apply();
    }

    public static SharedPreferences getPreference(Context context) {
        return context.getSharedPreferences(context.getString(R.string.preference), Context.MODE_PRIVATE);
    }

    public static void setLight(Context context, SharedPreferences preferences) {
        preferences.edit().putString(context.getString(R.string.pref_key_theme), context.getString(R.string.pref_theme_light)).apply();
    }

    public static void setDark(Context context, SharedPreferences preferences) {
        preferences.edit().putString(context.getString(R.string.pref_key_theme), context.getString(R.string.pref_theme_dark)).apply();
    }

    public static void setServertoPref(String server, Context context, SharedPreferences sharedPref) {
        sharedPref.edit().putString(context.getString(R.string.pref_server_select_key), server).apply();
    }

    public static String getServerFromPref(Context context, SharedPreferences sharedPreferences) {
        return sharedPreferences.getString(context.getString(R.string.pref_server_select_key), null);
    }

    public static boolean getFirstRun(Context context) {
        return context.getSharedPreferences(context.getString(R.string.preference), MODE_PRIVATE).getBoolean(context.getString(R.string.is_first_run), true);
    }

    public static void setFirstRun(Context context) {
        context.getSharedPreferences(context.getString(R.string.preference), MODE_PRIVATE).edit().putBoolean(context.getString(R.string.is_first_run), false).apply();
    }

    public static void setServerName(Context context, String serverName) {
        context.getSharedPreferences(context.getString(R.string.preference), MODE_PRIVATE).edit().putString(context.getString(R.string.pref_server_select_key), serverName).apply();
    }

    public static String getServerName(Context context) {
        return context.getSharedPreferences(context.getString(R.string.preference), MODE_PRIVATE).getString(context.getString(R.string.pref_server_select_key), null);
    }

    public static Preferences ofCookie(Context context) {
        return new Preferences(context, Locations.COOKIE);
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

    public static int getSortOption(Context context) {
        return getPreference(context).getInt(Defaults.SORTING_OPTION, ServerFilesFragment.SORT_MODIFICATION_TIME);
    }

    public static void setSortOption(Context context, int filesSort) {
        getPreference(context).edit().putInt(Defaults.SORTING_OPTION, filesSort).apply();
    }

    private static final class Locations {
        public static final String COOKIE = "cookie";

        private Locations() {
        }
    }

    private static final class Defaults {
        public static final String STRING = "";
        public static final String SORTING_OPTION = "sorting_option";

        private Defaults() {
        }
    }
}
