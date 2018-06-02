package org.amahi.anywhere.bus;

import org.amahi.anywhere.model.FileOption;

public class FileOptionClickEvent {
    private int fileOption;

    public FileOptionClickEvent(@FileOption.Types int fileOption) {
        this.fileOption = fileOption;
    }

    @FileOption.Types
    public int getFileOption() {
        return fileOption;
    }
}
