package org.amahi.anywhere.model;

import android.support.annotation.IntDef;

public class FileOption {

    public static final int SHARE = 1;
    public static final int DOWNLOAD = 2;
    public static final int DELETE = 3;
    @Types
    private int type;

    public FileOption(@Types int type) {
        this.type = type;
    }

    @Types
    public int getType() {
        return type;
    }

    @IntDef({SHARE, DOWNLOAD, DELETE})
    public @interface Types {
    }
}
