package org.amahi.anywhere.bus;


import org.amahi.anywhere.model.FileFilterOption;

public class FileFilterOptionClickEvent {

    @FileFilterOption.Types
    private int filterOption;

    public FileFilterOptionClickEvent(@FileFilterOption.Types int filterOption) {
        this.filterOption = filterOption;

    }

    @FileFilterOption.Types
    public int getFilterOption() {
        return filterOption;
    }
}
