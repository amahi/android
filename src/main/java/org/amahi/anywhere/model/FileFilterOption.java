package org.amahi.anywhere.model;

import androidx.annotation.IntDef;

public class FileFilterOption {

    //Specifying positions to be on the Filtering selection dialog
    public static final int All = 0;
    public static final int DOCS = 1;
    public static final int VID = 2;
    public static final int AUD = 3;
    public static final int PICS = 4;

    @Types
    private int type;

    public FileFilterOption(@Types int type) {
        this.type = type;
    }

    @Types
    public int getType() {
        return type;
    }

    @IntDef({All, DOCS, VID, AUD, PICS})
    public @interface Types {
    }
}
