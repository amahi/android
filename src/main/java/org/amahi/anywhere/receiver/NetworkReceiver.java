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

public class NetworkReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
			handleNetworkChangeEvent(context);
		}
	}

	private void handleNetworkChangeEvent(Context context) {
		NetworkInfo network = getNetwork(context);

		if (isNetworkConnected(network)) {
			BusProvider.getBus().post(new NetworkChangedEvent(network.getType()));
		}
	}

	private NetworkInfo getNetwork(Context context) {
		return getNetworkManager(context).getActiveNetworkInfo();
	}

	private ConnectivityManager getNetworkManager(Context context) {
		return (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	}

	private boolean isNetworkConnected(NetworkInfo network) {
		return (network != null) && network.isConnected();
	}
}
