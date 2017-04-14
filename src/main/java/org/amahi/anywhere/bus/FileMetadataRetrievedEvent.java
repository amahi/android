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

package org.amahi.anywhere.bus;

import android.view.View;

import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerFileMetadata;

public class FileMetadataRetrievedEvent implements BusEvent {
    private final ServerFile file;
    private final ServerFileMetadata fileMetadata;
    private final View fileView;

    public FileMetadataRetrievedEvent(ServerFile file, ServerFileMetadata fileMetadata, View fileView) {
        this.file = file;
        this.fileMetadata = fileMetadata;
        this.fileView = fileView;
    }

    public ServerFile getFile() {
        return file;
    }

    public ServerFileMetadata getFileMetadata() {
        return fileMetadata;
    }

    public View getFileView() {
        return fileView;
    }
}
