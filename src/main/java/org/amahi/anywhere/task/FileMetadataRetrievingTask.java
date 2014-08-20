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
import android.view.View;

import org.amahi.anywhere.adapter.ServerFilesMetadataAdapter;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerFileMetadata;
import org.amahi.anywhere.server.model.ServerShare;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

public class FileMetadataRetrievingTask extends AsyncTask<Void, Void, ServerFileMetadata>
{
	private final ServerClient serverClient;

	private final Reference<View> fileViewReference;

	private final ServerShare share;
	private final ServerFile file;

	public static void execute(ServerClient serverClient, View fileView) {
		new FileMetadataRetrievingTask(serverClient, fileView).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	private FileMetadataRetrievingTask(ServerClient serverClient, View fileView) {
		this.serverClient = serverClient;

		this.fileViewReference = new WeakReference<View>(fileView);

		this.share = (ServerShare) fileView.getTag(ServerFilesMetadataAdapter.Tags.SHARE);
		this.file = (ServerFile) fileView.getTag(ServerFilesMetadataAdapter.Tags.FILE);
	}

	@Override
	protected ServerFileMetadata doInBackground(Void... parameters) {
		return serverClient.getFileMetadata(share, file);
	}

	@Override
	protected void onPostExecute(ServerFileMetadata fileMetadata) {
		super.onPostExecute(fileMetadata);

		View fileView = fileViewReference.get();

		if (fileView == null) {
			return;
		}

		if (!file.equals(fileView.getTag(ServerFilesMetadataAdapter.Tags.FILE))) {
			return;
		}

		ServerFilesMetadataAdapter.bindView(file, fileMetadata, fileView);
	}
}
