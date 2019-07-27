package org.amahi.anywhere.bus;

public class DeleteFriendRequestCompletedEvent implements BusEvent {
    private Boolean success;
    private String message;

    public DeleteFriendRequestCompletedEvent(Boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public Boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
