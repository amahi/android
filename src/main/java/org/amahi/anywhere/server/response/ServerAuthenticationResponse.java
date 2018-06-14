package org.amahi.anywhere.server.response;

import android.support.annotation.NonNull;

import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.ServerAuthenticationCompleteEvent;
import org.amahi.anywhere.server.model.HdaAuthResponse;

import retrofit2.Call;
import retrofit2.HttpException;
import retrofit2.Response;

/**
 * HDA User Authentication response proxy. Consumes API callback and posts it via {@link com.squareup.otto.Bus}
 * as {@link org.amahi.anywhere.bus.BusEvent}.
 */
public class ServerAuthenticationResponse implements retrofit2.Callback<HdaAuthResponse> {
    @Override
    public void onResponse(@NonNull Call<HdaAuthResponse> call, @NonNull Response<HdaAuthResponse> response) {
        if (response.isSuccessful())
            BusProvider.getBus().post(new ServerAuthenticationCompleteEvent(response.body().getAuthToken()));
        else
            this.onFailure(call, new HttpException(response));
    }

    @Override
    public void onFailure(@NonNull Call<HdaAuthResponse> call, @NonNull Throwable t) {
        // Todo indicate authentication failure to user
    }
}
