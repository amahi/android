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

import android.view.View;

import org.amahi.anywhere.adapter.ServerFilesMetadataAdapter;
import org.amahi.anywhere.bus.BusEvent;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.FileMetadataRetrievedEvent;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerFileMetadata;
import org.amahi.anywhere.server.model.ServerShare;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FileMetadataRetrievingTask implements Callback<ServerFileMetadata> {

    private final Reference<View> fileViewReference;

    private final ServerShare share;
    private final ServerFile file;
    private final ServerClient serverClient;

    public FileMetadataRetrievingTask(ServerClient serverClient, View fileView) {
        this.serverClient = serverClient;
        this.fileViewReference = new WeakReference<>(fileView);

        this.share = (ServerShare) fileView.getTag(ServerFilesMetadataAdapter.Tags.SHARE);
        this.file = (ServerFile) fileView.getTag(ServerFilesMetadataAdapter.Tags.FILE);
    }

    public void execute() {
        serverClient.getFileMetadata(share, file, this);
    }

    @Override
    public void onResponse(Call<ServerFileMetadata> call, Response<ServerFileMetadata> response) {

        View fileView = fileViewReference.get();

        if (fileView == null) {
            return;
        }

        if (!file.equals(fileView.getTag(ServerFilesMetadataAdapter.Tags.FILE))) {
            return;
        }

        BusEvent busEvent = new FileMetadataRetrievedEvent(file, response.body(), fileView);
        BusProvider.getBus().post(busEvent);
    }

    @Override
    public void onFailure(Call<ServerFileMetadata> call, Throwable t) {

    }
}
