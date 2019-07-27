package org.amahi.anywhere.server.response;

import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.DeleteFriendFailedEvent;
import org.amahi.anywhere.bus.DeleteFriendCompletedEvent;
import org.amahi.anywhere.server.model.DeleteFriendResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.HttpException;
import retrofit2.Response;

public class FriendUserDeleteResponse implements Callback<DeleteFriendResponse> {
    @Override
    public void onResponse(Call<DeleteFriendResponse> call, Response<DeleteFriendResponse> response) {
        DeleteFriendResponse deleteFriendResponse = response.body();
        if (response.isSuccessful()) {
            BusProvider.getBus().post(new DeleteFriendCompletedEvent(deleteFriendResponse.isSuccess(), deleteFriendResponse.getMessage()));

        } else {
            this.onFailure(call, new HttpException(response));
        }

    }

    @Override
    public void onFailure(Call<DeleteFriendResponse> call, Throwable t) {
        BusProvider.getBus().post(new DeleteFriendFailedEvent());

    }
}
