package org.amahi.anywhere.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.R;
import org.amahi.anywhere.activity.AuthenticationActivity;
import org.amahi.anywhere.activity.IntroductionActivity;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.NonAdminPublicKeySucceedEvent;
import org.amahi.anywhere.bus.ServerConnectedEvent;
import org.amahi.anywhere.bus.ServerSharesLoadedEvent;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.NonAdminPublicKey;
import org.amahi.anywhere.server.model.Server;
import org.amahi.anywhere.util.CheckTV;
import org.amahi.anywhere.util.Preferences;

import javax.inject.Inject;

import static android.content.Context.MODE_PRIVATE;

public class NonAdminNavigationFragment extends Fragment{

    @Inject
    ServerClient serverClient;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpInjections();
        String sessionToken = getContext().getSharedPreferences(getString(R.string.preference), MODE_PRIVATE).getString("session_token", "");
        String serverName = getContext().getSharedPreferences(getString(R.string.preference), MODE_PRIVATE).getString("server_name", "");
        String serverAddress = getContext().getSharedPreferences(getString(R.string.preference), MODE_PRIVATE).getString("server_address", "");

        if(sessionToken.isEmpty() || serverName.isEmpty() || serverAddress.isEmpty()) {
            startActivity(new Intent(getContext(), AuthenticationActivity.class));
        }

        Server server = new Server(serverName, sessionToken, serverAddress, true);
        serverClient.connecttoNonadmin(server);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_navigation, container, false);
        Spinner spinner = (Spinner)rootView.findViewById(R.id.spinner_servers);
        spinner.setVisibility(View.GONE);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        launchIntro();
    }

    private void launchIntro(){
        if(Preferences.getFirstRun(getContext()) && !CheckTV.isATV(getContext())){
            Preferences.setFirstRun(getContext());
            startActivity(new Intent(getContext(), IntroductionActivity.class));
        }
    }

    private void setUpInjections() {
        AmahiApplication.from(getActivity()).inject(this);
    }


    @Subscribe
    public void onServerConnected(ServerConnectedEvent event) {
        Toast.makeText(getContext(), event.getServer().getName(), Toast.LENGTH_SHORT).show();
        serverClient.getShares();
    }

    @Subscribe
    public void onSharesLoaded(ServerSharesLoadedEvent event) {
        Toast.makeText(getContext(), event.getServerShares().get(0).getName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();

        BusProvider.getBus().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        BusProvider.getBus().unregister(this);
    }
}
