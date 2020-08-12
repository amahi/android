package org.amahi.anywhere.bus;

import org.amahi.anywhere.model.FileOption;

public class FileOptionClickEvent {
    private int fileOption;
    private String uniqueKey;

    public FileOptionClickEvent(@FileOption.Types int fileOption, String uniqueKey) {
        this.fileOption = fileOption;
        this.uniqueKey = uniqueKey;
    }

    @FileOption.Types
    public int getFileOption() {
        return fileOption;
    }

    public String getFileUniqueKey() {
        return uniqueKey;
    }
}
