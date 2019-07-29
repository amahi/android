package org.amahi.anywhere.server.response;

import androidx.annotation.NonNull;

import org.amahi.anywhere.bus.AuthenticationConnectionFailedEvent;
import org.amahi.anywhere.bus.AuthenticationFailedEvent;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.ServerAuthenticationCompleteEvent;
import org.amahi.anywhere.server.model.HdaAuthResponse;

import retrofit2.Call;
import retrofit2.Response;

/**
 * HDA User Authentication response proxy. Consumes API callback and posts it via {@link com.squareup.otto.Bus}
 * as {@link org.amahi.anywhere.bus.BusEvent}.
 */
public class ServerAuthenticationResponse implements retrofit2.Callback<HdaAuthResponse> {
    @Override
    public void onResponse(@NonNull Call<HdaAuthResponse> call, @NonNull Response<HdaAuthResponse> response) {
        switch (response.code()) {
            case 200:
                BusProvider.getBus().post(new ServerAuthenticationCompleteEvent(response.body().getAuthToken()));
                break;
            case 401:
                BusProvider.getBus().post(new AuthenticationFailedEvent());
                break;
            case 403:
                BusProvider.getBus().post(new AuthenticationFailedEvent());
                break;
            default:
                BusProvider.getBus().post(new AuthenticationConnectionFailedEvent());
        }
    }

    @Override
    public void onFailure(@NonNull Call<HdaAuthResponse> call, @NonNull Throwable t) {
        BusProvider.getBus().post(new AuthenticationConnectionFailedEvent());
    }
}
