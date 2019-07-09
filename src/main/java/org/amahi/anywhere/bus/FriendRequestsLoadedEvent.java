package org.amahi.anywhere.bus;


import org.amahi.anywhere.server.model.FriendRequest;

import java.util.List;

public class FriendRequestsLoadedEvent implements BusEvent {
    private final List<FriendRequest> friendRequests;

    public FriendRequestsLoadedEvent(List<FriendRequest> friendRequests) {
        this.friendRequests = friendRequests;
    }

    public List<FriendRequest> getFriendRequests() {
        return friendRequests;
    }
}
