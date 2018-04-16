package org.amahi.anywhere.bus;

public class ServerRouteLoadFailedEvent implements BusEvent {
    private String errorMessage;

    public ServerRouteLoadFailedEvent() {
    }

    public ServerRouteLoadFailedEvent(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
