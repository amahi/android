package org.amahi.anywhere.bus;

import android.util.Log;

import org.amahi.anywhere.server.model.FriendUserItem;

import java.util.List;

public class FriendUsersLoadedEvent implements BusEvent {
    private final List<FriendUserItem> friendUsers;

    public FriendUsersLoadedEvent(List<FriendUserItem> friendUsers) {
        this.friendUsers = friendUsers;

    }

    public List<FriendUserItem> getFriendUsers() {
        Log.d("entered", "getLEFriendUsrs:");
        return friendUsers;
    }
}
