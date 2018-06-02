package org.amahi.anywhere.model;

import android.support.annotation.IntDef;

public class FileOption {

    public static final int OPEN = 0;
    public static final int SHARE = 1;
    public static final int DOWNLOAD = 2;
    public static final int DELETE = 3;
    public static final int OFFLINE_DISABLED = 4;
    public static final int OFFLINE_ENABLED = 5;
    @Types
    private int type;

    public FileOption(@Types int type) {
        this.type = type;
    }

    @Types
    public int getType() {
        return type;
    }

    @IntDef({OPEN, SHARE, DOWNLOAD, DELETE, OFFLINE_DISABLED, OFFLINE_ENABLED})
    public @interface Types {
    }
}
