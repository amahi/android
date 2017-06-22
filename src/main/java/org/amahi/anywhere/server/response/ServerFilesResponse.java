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

package org.amahi.anywhere.server.response;

import android.util.Log;

import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.ServerFilesLoadFailedEvent;
import org.amahi.anywhere.bus.ServerFilesLoadedEvent;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerShare;

import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.HttpException;
import retrofit2.Response;


/**
 * Files response proxy. Consumes API callback and posts it via {@link com.squareup.otto.Bus}
 * as {@link org.amahi.anywhere.bus.BusEvent}.
 */
public class ServerFilesResponse implements Callback<List<ServerFile>>
{
	private ServerFile serverDirectory;
	private ServerShare serverShare;

	public ServerFilesResponse(ServerFile serverDirectory) {
		this.serverDirectory = serverDirectory;
	}

	public ServerFilesResponse(ServerShare serverShare){
		this.serverShare = serverShare;
	}

	@Override
	public void onResponse(Call<List<ServerFile>> call, Response<List<ServerFile>> response) {
		if (response.isSuccessful()) {
			List<ServerFile> serverFiles = response.body();
			if (serverFiles == null) {
				serverFiles = Collections.emptyList();
			}

			for (ServerFile serverFile : serverFiles) {
				serverFile.setParentFile(serverDirectory);
				serverFile.setParentShare(serverShare);
			}

			BusProvider.getBus().post(new ServerFilesLoadedEvent(serverFiles));
		}
		else
			this.onFailure(call, new HttpException(response));
	}

	@Override
	public void onFailure(Call<List<ServerFile>> call, Throwable t) {
		BusProvider.getBus().post(new ServerFilesLoadFailedEvent());
	}
}
