package org.amahi.anywhere.bus;

import org.amahi.anywhere.model.FileOption;
import org.amahi.anywhere.server.model.ServerFile;

public class FileOptionClickEvent {
    private int fileOption;
    private String uniqueKey;
    private ServerFile file;

    public FileOptionClickEvent(@FileOption.Types int fileOption, String uniqueKey) {
        this.fileOption = fileOption;
        this.uniqueKey = uniqueKey;
    }

    public FileOptionClickEvent(@FileOption.Types int fileOption, String uniqueKey, ServerFile file) {
        this.fileOption = fileOption;
        this.uniqueKey = uniqueKey;
        this.file = file;
    }

    @FileOption.Types
    public int getFileOption() {
        return fileOption;
    }

    public String getFileUniqueKey() {
        return uniqueKey;
    }

    public ServerFile getServerFile() {
        return file;
    }

}
