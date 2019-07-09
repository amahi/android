package org.amahi.anywhere.server.response;

import com.squareup.otto.Bus;

import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.FriendUsersLoadFailedEvent;
import org.amahi.anywhere.bus.FriendUsersLoadedEvent;
import org.amahi.anywhere.bus.ServerFilesLoadedEvent;
import org.amahi.anywhere.server.model.FriendUser;
import org.amahi.anywhere.server.model.PrimaryUser;

import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.HttpException;
import retrofit2.Response;


public class FriendUsersResponse implements Callback<List<PrimaryUser>> {
    @Override
    public void onResponse(Call<List<PrimaryUser>> call, Response<List<PrimaryUser>> response) {
        if (response.isSuccessful()) {
            List<PrimaryUser> primaryUsers = response.body();
            if (primaryUsers == null) {
                primaryUsers = Collections.emptyList();
            }
            BusProvider.getBus().post(new FriendUsersLoadedEvent(primaryUsers));
        } else
            this.onFailure(call, new HttpException(response));

    }

    @Override
    public void onFailure(Call<List<PrimaryUser>> call, Throwable t) {
        BusProvider.getBus().post(new FriendUsersLoadFailedEvent());

    }
}
