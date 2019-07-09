package org.amahi.anywhere.server.response;



import org.amahi.anywhere.bus.AddFriendUserCompletedEvent;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.server.model.NewFriendRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.HttpException;
import retrofit2.Response;

public class AddFriendUserResponse implements Callback<NewFriendRequest> {
    @Override
    public void onResponse(Call<NewFriendRequest> call, Response<NewFriendRequest> response) {
        if (response.isSuccessful()) {
            BusProvider.getBus().post(new AddFriendUserCompletedEvent(true));
        } else
            this.onFailure(call, new HttpException(response));

    }

    @Override
    public void onFailure(Call<NewFriendRequest> call, Throwable t) {
        BusProvider.getBus().post(new AddFriendUserCompletedEvent(false));

    }
}
