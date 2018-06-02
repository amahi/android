package org.amahi.anywhere.bus;

import java.io.File;

public class FileCopiedEvent implements BusEvent {
    private File targetLocation;

    public FileCopiedEvent(File targetLocation) {
        this.targetLocation = targetLocation;
    }

    public File getTargetLocation() {
        return targetLocation;
    }
}
