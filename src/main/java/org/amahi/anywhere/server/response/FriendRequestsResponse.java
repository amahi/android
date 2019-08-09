package org.amahi.anywhere.server.response;

import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.FriendRequestsLoadFailedEvent;
import org.amahi.anywhere.bus.FriendRequestsLoadedEvent;
import org.amahi.anywhere.server.model.FriendRequestResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.HttpException;
import retrofit2.Response;

public class FriendRequestsResponse implements Callback<FriendRequestResponse> {
    @Override
    public void onResponse(Call<FriendRequestResponse> call, Response<FriendRequestResponse> response) {
        if (response.isSuccessful()) {
            FriendRequestResponse friendRequestResponse = response.body();
            BusProvider.getBus().post(new FriendRequestsLoadedEvent(friendRequestResponse.getFriendRequests()));
        } else
            this.onFailure(call, new HttpException(response));

    }

    @Override
    public void onFailure(Call<FriendRequestResponse> call, Throwable t) {
        BusProvider.getBus().post(new FriendRequestsLoadFailedEvent());

    }
}
