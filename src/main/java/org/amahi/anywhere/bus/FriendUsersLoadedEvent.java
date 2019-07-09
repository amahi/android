package org.amahi.anywhere.bus;

import org.amahi.anywhere.server.model.PrimaryUser;

import java.util.List;

public class FriendUsersLoadedEvent implements BusEvent {
    private final List<PrimaryUser> primaryUsers;

    public FriendUsersLoadedEvent(List<PrimaryUser> primaryUsers) {
        this.primaryUsers = primaryUsers;
    }

    public List<PrimaryUser> getPrimaryUsers() {
        return primaryUsers;
    }
}
