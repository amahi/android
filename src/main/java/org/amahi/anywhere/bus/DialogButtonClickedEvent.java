package org.amahi.anywhere.bus;

import android.support.annotation.IntDef;

public class DialogButtonClickedEvent implements BusEvent {
    public static final int YES = 1;
    public static final int NO = 2;

    @Types
    private int buttonId;

    public DialogButtonClickedEvent(@Types int buttonId) {
        this.buttonId = buttonId;
    }

    public int getButtonId() {
        return buttonId;
    }

    @IntDef({YES, NO})
    public @interface Types {
    }
}
