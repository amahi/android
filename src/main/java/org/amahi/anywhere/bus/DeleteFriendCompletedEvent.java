package org.amahi.anywhere.bus;

public class DeleteFriendCompletedEvent implements BusEvent {
    private String message;
    private Boolean success;

    public DeleteFriendCompletedEvent(Boolean success, String message) {
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
