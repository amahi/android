package org.amahi.anywhere.model;

import androidx.annotation.IntDef;

import org.amahi.anywhere.util.Mimes;

public class FileFilterOption {

    //Specifying positions to be on the Filtering selection dialog
    public static final int All = 0;
    public static final int DOCS = Mimes.Type.DOCUMENT;
    public static final int VID = Mimes.Type.VIDEO;
    public static final int AUD = Mimes.Type.AUDIO;
    public static final int PICS = Mimes.Type.IMAGE;

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
