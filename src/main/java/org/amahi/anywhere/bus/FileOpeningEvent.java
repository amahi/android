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

import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerShare;

import java.util.List;

public class FileOpeningEvent implements BusEvent {
    private final ServerShare share;
    private final List<ServerFile> files;
    private final ServerFile file;

    public FileOpeningEvent(ServerShare share, List<ServerFile> files, ServerFile file) {
        this.share = share;
        this.files = files;
        this.file = file;
    }

    public ServerShare getShare() {
        return share;
    }

    public List<ServerFile> getFiles() {
        return files;
    }

    public ServerFile getFile() {
        return file;
    }
}
