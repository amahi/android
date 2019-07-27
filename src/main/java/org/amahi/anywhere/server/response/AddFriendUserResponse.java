package org.amahi.anywhere.server.response;


import org.amahi.anywhere.bus.AddFriendUserCompletedEvent;
import org.amahi.anywhere.bus.AddFriendUserFailedEvent;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.server.model.NewFriendRequestResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddFriendUserResponse implements Callback<NewFriendRequestResponse> {
    @Override
    public void onResponse(Call<NewFriendRequestResponse> call, Response<NewFriendRequestResponse> response) {
        if (response.isSuccessful()) {
            NewFriendRequestResponse requestResponse = response.body();
            if (requestResponse.isSuccess()) {
                BusProvider.getBus().post(new AddFriendUserCompletedEvent(requestResponse.isSuccess(), requestResponse.getMessage()));
            } else {
                BusProvider.getBus().post(new AddFriendUserFailedEvent());
            }
        } else {
            BusProvider.getBus().post(new AddFriendUserFailedEvent());
        }


    }

    @Override
    public void onFailure(Call<NewFriendRequestResponse> call, Throwable t) {
        BusProvider.getBus().post(new AddFriendUserFailedEvent());

    }
}
