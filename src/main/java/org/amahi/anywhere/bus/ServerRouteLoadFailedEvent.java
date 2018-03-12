package org.amahi.anywhere.bus;

/**
 * Created by mohit on 12/3/18.
 */

public class ServerRouteLoadFailedEvent implements BusEvent {
    private String errorMessage;

    public ServerRouteLoadFailedEvent(){}

    public ServerRouteLoadFailedEvent(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
