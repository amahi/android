package org.amahi.anywhere.server.response;


import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.FriendUsersLoadFailedEvent;
import org.amahi.anywhere.bus.FriendUsersLoadedEvent;
import org.amahi.anywhere.server.model.FriendUserResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.HttpException;
import retrofit2.Response;


public class FriendUsersResponse implements Callback<FriendUserResponse> {
    @Override
    public void onResponse(Call<FriendUserResponse> call, Response<FriendUserResponse> response) {
        if (response.isSuccessful()) {
            FriendUserResponse friendUserResponse = response.body();
            BusProvider.getBus().post(new FriendUsersLoadedEvent(friendUserResponse.getFriendUsers()));
        } else {
            this.onFailure(call, new HttpException(response));
        }

    }

    @Override
    public void onFailure(Call<FriendUserResponse> call, Throwable t) {
        BusProvider.getBus().post(new FriendUsersLoadFailedEvent());
    }
}
