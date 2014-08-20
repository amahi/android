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

package org.amahi.anywhere.task;

import android.os.AsyncTask;

import org.amahi.anywhere.bus.BusEvent;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.ServerConnectionDetectedEvent;
import org.amahi.anywhere.server.ApiConnectionDetector;
import org.amahi.anywhere.server.model.ServerRoute;

/**
 * Async wrapper for server connection detecting.
 * The detecting itself is done via {@link org.amahi.anywhere.server.ApiConnectionDetector}.
 */
public class ServerConnectionDetectingTask extends AsyncTask<Void, Void, BusEvent>
{
	private final ServerRoute serverRoute;

	public static void execute(ServerRoute serverRoute) {
		new ServerConnectionDetectingTask(serverRoute).execute();
	}

	private ServerConnectionDetectingTask(ServerRoute serverRoute) {
		this.serverRoute = serverRoute;
	}

	@Override
	protected BusEvent doInBackground(Void... parameters) {
		return new ServerConnectionDetectedEvent(new ApiConnectionDetector().detect(serverRoute));
	}

	@Override
	protected void onPostExecute(BusEvent busEvent) {
		super.onPostExecute(busEvent);

		BusProvider.getBus().post(busEvent);
	}
}
