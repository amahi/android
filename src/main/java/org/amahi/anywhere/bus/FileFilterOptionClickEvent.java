package org.amahi.anywhere.bus;


import org.amahi.anywhere.model.FileFilterOption;

public class FileFilterOptionClickEvent {
    private int filterOption;

    public FileFilterOptionClickEvent(@FileFilterOption.Types int filterOption) {
        this.filterOption = this.filterOption;

    }

    @FileFilterOption.Types
    public int getFilterOption() {
        return filterOption;
    }
}
