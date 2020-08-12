package org.amahi.anywhere.bus;

/**
 * Created by arihant on 14/3/18.
 */

public class AudioControlChangeEvent implements BusEvent {

    private final int position;

    public AudioControlChangeEvent(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }
}
