package org.amahi.anywhere.server.response;

import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.FriendRequestsLoadFailedEvent;
import org.amahi.anywhere.bus.FriendRequestsLoadedEvent;
import org.amahi.anywhere.server.model.FriendRequest;

import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.HttpException;
import retrofit2.Response;

public class FriendRequestsResponse implements Callback<List<FriendRequest>> {
    @Override
    public void onResponse(Call<List<FriendRequest>> call, Response<List<FriendRequest>> response) {
        if (response.isSuccessful()) {
            List<FriendRequest> friendRequests = response.body();
            if (friendRequests == null) {
                friendRequests = Collections.emptyList();
            }
            BusProvider.getBus().post(new FriendRequestsLoadedEvent(friendRequests));
        } else
            this.onFailure(call, new HttpException(response));

    }

    @Override
    public void onFailure(Call<List<FriendRequest>> call, Throwable t) {
        BusProvider.getBus().post(new FriendRequestsLoadFailedEvent());

    }
}
