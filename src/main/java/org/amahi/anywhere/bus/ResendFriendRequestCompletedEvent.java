package org.amahi.anywhere.bus;

public class ResendFriendRequestCompletedEvent {
    private String message;
    private Boolean success;

    public ResendFriendRequestCompletedEvent(Boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public Boolean isSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }
}
