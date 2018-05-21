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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

import org.amahi.anywhere.R;

/**
 * Network utility methods to check the current connected network
 */
public class NetworkUtils {

    private Context context;

    public NetworkUtils(Context context) {
        this.context = context;
    }

    public NetworkInfo getNetwork() {
        return getNetworkManager().getActiveNetworkInfo();
    }

    private ConnectivityManager getNetworkManager() {
        return (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    public boolean isNetworkConnected(NetworkInfo network) {
        return (network != null) && network.isConnected();
    }

    public boolean isUploadAllowed() {
        NetworkInfo network = getNetwork();
        return isNetworkConnected(network) &&
            (network.getType() != ConnectivityManager.TYPE_MOBILE ||
                isUploadAllowedOnMobileData());
    }

    private boolean isUploadAllowedOnMobileData() {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(context.getString(R.string.preference_key_upload_data), false);
    }

    public boolean isNetworkAvailable() {
        NetworkInfo network = getNetwork();
        return isNetworkConnected(network);
    }
}
