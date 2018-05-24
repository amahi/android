package org.amahi.anywhere.bus;

import java.io.File;

public class FileMovedEvent implements BusEvent{
    private File targetLocation;

    public FileMovedEvent(File targetLocation) {
        this.targetLocation = targetLocation;
    }

    public File getTargetLocation() {
        return targetLocation;
    }
}
