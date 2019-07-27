package org.amahi.anywhere.server.response;

import org.amahi.anywhere.bus.BusEvent;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.DeleteFriendRequestCompletedEvent;
import org.amahi.anywhere.bus.DeleteFriendRequestFailedEvent;
import org.amahi.anywhere.server.model.DeleteFriendRequestResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.HttpException;
import retrofit2.Response;

public class FriendRequestDeleteResponse implements Callback<DeleteFriendRequestResponse> {
    @Override
    public void onResponse(Call<DeleteFriendRequestResponse> call, Response<DeleteFriendRequestResponse> response) {
        if (response.isSuccessful()) {
            DeleteFriendRequestResponse deleteFriendRequestResponse = response.body();
            BusProvider.getBus().post(new DeleteFriendRequestCompletedEvent(deleteFriendRequestResponse.isSuccess(), deleteFriendRequestResponse.getMessage()));
        } else {
            this.onFailure(call, new HttpException(response));
        }
    }

    @Override
    public void onFailure(Call<DeleteFriendRequestResponse> call, Throwable t) {
        BusProvider.getBus().post(new DeleteFriendRequestFailedEvent());
    }
}
