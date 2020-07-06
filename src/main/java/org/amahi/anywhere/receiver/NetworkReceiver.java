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

package org.amahi.anywhere.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.NetworkChangedEvent;
import org.amahi.anywhere.service.DownloadService;
import org.amahi.anywhere.service.UploadService;
import org.amahi.anywhere.util.NetworkUtils;

/**
 * Network system events receiver. Proxies system network events such as changing network connection
 * to the local {@link com.squareup.otto.Bus} as {@link org.amahi.anywhere.bus.BusEvent}.
 */
public class NetworkReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            handleNetworkChangeEvent(context);
        }
    }

    private void handleNetworkChangeEvent(Context context) {
        NetworkUtils networkUtils = new NetworkUtils(context);
        NetworkInfo network = networkUtils.getNetwork();
        if (networkUtils.isNetworkConnected(network)) {
            BusProvider.getBus().post(new NetworkChangedEvent(network.getType()));
            startDownloadService(context);
        }

        if (networkUtils.isUploadAllowed()) {
            startUploadService(context);
        } else {
            stopUploadService(context);
        }
    }

    private void startUploadService(Context context) {
        Intent uploadService = new Intent(context, UploadService.class);
        context.startService(uploadService);
    }

    private void stopUploadService(Context context) {
        Intent uploadService = new Intent(context, UploadService.class);
        context.stopService(uploadService);
    }

    private void startDownloadService(Context context) {
        Intent downloadService = new Intent(context, DownloadService.class);
        downloadService.setAction(ConnectivityManager.CONNECTIVITY_ACTION);
        context.startService(downloadService);
    }

}
