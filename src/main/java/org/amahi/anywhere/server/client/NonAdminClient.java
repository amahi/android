package org.amahi.anywhere.server.client;

import org.amahi.anywhere.server.Api;
import org.amahi.anywhere.server.ApiAdapter;
import org.amahi.anywhere.server.api.NonAdminApi;
import org.amahi.anywhere.server.response.NonAdminAuthenticationResponse;
import org.amahi.anywhere.server.response.NonAdminPublicKeyResponse;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class NonAdminClient {
    private final NonAdminApi api;

    @Inject
    public NonAdminClient(ApiAdapter apiAdapter) {this.api = buildApi(apiAdapter);}

    private NonAdminApi buildApi(ApiAdapter apiAdapter) {
        return apiAdapter.create(NonAdminApi.class, "http://google.com");
    }

    public void authenticate(String username, String password) {
        //Fetch the public key stored in the sharedpref here.
        //Then hash the password using the public key here.
        api.authenticate(Api.getClientId(), Api.getClientSecret(), username, password).enqueue(new NonAdminAuthenticationResponse());
    }

    public void getPublicKey() {
        api.getNonAdminPublicKey().enqueue(new NonAdminPublicKeyResponse());
    }
}
