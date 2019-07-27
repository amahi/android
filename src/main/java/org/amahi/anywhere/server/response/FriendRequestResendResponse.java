package org.amahi.anywhere.server.response;

import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.ResendFriendFailedEvent;
import org.amahi.anywhere.bus.ResendFriendRequestCompletedEvent;
import org.amahi.anywhere.server.model.ResendFriendRequestResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.HttpException;
import retrofit2.Response;

public class FriendRequestResendResponse implements Callback<ResendFriendRequestResponse> {
    @Override
    public void onResponse(Call<ResendFriendRequestResponse> call, Response<ResendFriendRequestResponse> response) {

        if (response.isSuccessful()) {
            ResendFriendRequestResponse resendFriendRequestResponse = response.body();
            BusProvider.getBus().post(new ResendFriendRequestCompletedEvent(resendFriendRequestResponse.isSuccess(),
                resendFriendRequestResponse.getMessage()));
        } else {
            this.onFailure(call, new HttpException(response));
        }

    }

    @Override
    public void onFailure(Call<ResendFriendRequestResponse> call, Throwable t) {
        BusProvider.getBus().post(new ResendFriendFailedEvent());

    }
}
