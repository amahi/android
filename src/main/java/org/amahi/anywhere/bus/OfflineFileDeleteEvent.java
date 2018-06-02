package org.amahi.anywhere.bus;

import org.amahi.anywhere.server.model.ServerFile;

public class OfflineFileDeleteEvent implements BusEvent {
    private ServerFile file;

    public OfflineFileDeleteEvent(ServerFile file) {
        this.file = file;
    }

    public ServerFile getFile() {
        return file;
    }
}

