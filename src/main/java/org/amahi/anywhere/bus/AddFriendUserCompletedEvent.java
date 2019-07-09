package org.amahi.anywhere.bus;

public class AddFriendUserCompletedEvent {
    public boolean isSuccessful;

    public boolean isSuccessful() {
        return isSuccessful;
    }

    public AddFriendUserCompletedEvent(boolean isSuccessful) {
        this.isSuccessful = isSuccessful;
    }


}
