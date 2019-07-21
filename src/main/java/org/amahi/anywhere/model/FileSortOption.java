package org.amahi.anywhere.model;

import androidx.annotation.IntDef;

public class FileSortOption {

    public static final int NAME_ASC = 0;
    public static final int NAME_DES = 1;
    public static final int TIME_ASC = 2;
    public static final int TIME_DES = 3;
    public static final int SIZE_ASC = 4;
    public static final int SIZE_DES = 5;
    public static final int FILE_TYPE = 6;
    @Types
    private int type;

    public FileSortOption(@Types int type) {
        this.type = type;
    }

    @Types
    public int getType() {
        return type;
    }

    @IntDef({NAME_ASC, NAME_DES, TIME_ASC, TIME_DES, SIZE_ASC, SIZE_DES, FILE_TYPE})
    public @interface Types {
    }
}
