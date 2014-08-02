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
import org.amahi.anywhere.bus.ServerFilesMetadataLoadedEvent;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerFileMetadata;
import org.amahi.anywhere.server.model.ServerShare;

import java.util.ArrayList;
import java.util.List;

public class FileMetadataRetrievingTask extends AsyncTask<Void, Void, BusEvent>
{
	private final ServerClient serverClient;

	private final ServerShare share;
	private final List<ServerFile> files;

	public static void execute(ServerClient serverClient, ServerShare share, List<ServerFile> files) {
		new FileMetadataRetrievingTask(serverClient, share, files).execute();
	}

	private FileMetadataRetrievingTask(ServerClient serverClient, ServerShare share, List<ServerFile> files) {
		this.serverClient = serverClient;

		this.share = share;
		this.files = files;
	}

	@Override
	protected BusEvent doInBackground(Void... parameters) {
		List<ServerFileMetadata> filesMetadata = new ArrayList<ServerFileMetadata>();

		for (ServerFile file : files) {
			filesMetadata.add(serverClient.getFileMetadata(share, file));
		}

		return new ServerFilesMetadataLoadedEvent(filesMetadata);
	}

	@Override
	protected void onPostExecute(BusEvent busEvent) {
		super.onPostExecute(busEvent);

		BusProvider.getBus().post(busEvent);
	}
}
