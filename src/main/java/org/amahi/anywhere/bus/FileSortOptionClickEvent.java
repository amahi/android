package org.amahi.anywhere.bus;

import org.amahi.anywhere.model.FileSortOption;

public class FileSortOptionClickEvent {
    private int sortOption;

    public FileSortOptionClickEvent(@FileSortOption.Types int sortOption) {
        this.sortOption = sortOption;

    }

    @FileSortOption.Types
    public int getSortOption() {
        return sortOption;
    }
}
