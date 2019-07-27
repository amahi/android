package org.amahi.anywhere.bus;


import org.amahi.anywhere.server.model.FriendRequestItem;

import java.util.List;

public class FriendRequestsLoadedEvent implements BusEvent {
    private final List<FriendRequestItem> friendRequests;

    public FriendRequestsLoadedEvent(List<FriendRequestItem> friendRequests) {
        this.friendRequests = friendRequests;
    }

    public List<FriendRequestItem> getFriendRequests() {
        return friendRequests;
    }
}
