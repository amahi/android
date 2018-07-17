/*
 * Copyright (c) 2014 Amahi
 *
 * This file is part of Amahi.
 *
 * Amahi is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Amahi is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Amahi. If not, see <http ://www.gnu.org/licenses/>.
 */

package org.amahi.anywhere.fragment;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OnAccountsUpdateListener;
import android.accounts.OperationCanceledException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.R;
import org.amahi.anywhere.account.AmahiAccount;
import org.amahi.anywhere.activity.IntroductionActivity;
import org.amahi.anywhere.adapter.NavigationDrawerAdapter;
import org.amahi.anywhere.adapter.ServersAdapter;
import org.amahi.anywhere.bus.AppsSelectedEvent;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.OfflineFilesSelectedEvent;
import org.amahi.anywhere.bus.ServerAuthenticationCompleteEvent;
import org.amahi.anywhere.bus.ServerAuthenticationStartEvent;
import org.amahi.anywhere.bus.ServerConnectedEvent;
import org.amahi.anywhere.bus.ServerConnectionChangedEvent;
import org.amahi.anywhere.bus.ServersLoadFailedEvent;
import org.amahi.anywhere.bus.ServersLoadedEvent;
import org.amahi.anywhere.bus.SettingsSelectedEvent;
import org.amahi.anywhere.bus.SharesSelectedEvent;
import org.amahi.anywhere.server.client.AmahiClient;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.Server;
import org.amahi.anywhere.tv.activity.MainTVActivity;
import org.amahi.anywhere.util.CheckTV;
import org.amahi.anywhere.util.Intents;
import org.amahi.anywhere.util.Preferences;
import org.amahi.anywhere.util.RecyclerItemClickListener;
import org.amahi.anywhere.util.ViewDirector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import static org.amahi.anywhere.activity.NavigationActivity.PIN_REQUEST_CODE;

/**
 * Navigation fragments. Shows main application sections and servers list as well.
 */
public class NavigationFragment extends Fragment implements AccountManagerCallback<Bundle>,
    OnAccountsUpdateListener {
    @Inject
    AmahiClient amahiClient;
    @Inject
    ServerClient serverClient;
    View view;
    ServersAdapter serversAdapter;
    boolean mServerTitleClicked;
    private Intent tvIntent;
    private Server server;

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        view = layoutInflater.inflate(R.layout.fragment_navigation, container, false);
        return view;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        launchIntro();

        setUpInjections();

        setUpSettingsMenu();

        setUpAuthenticationListener();

        setUpContentRefreshing();

        setUpServers(savedInstanceState);

        setServerTitleClicked(false);


    }

    private void launchIntro() {
        if (Preferences.getFirstRun(getContext()) && !CheckTV.isATV(getContext())) {
            Preferences.setFirstRun(getContext());
            startActivity(new Intent(getContext(), IntroductionActivity.class));
        }
    }

    private void setUpInjections() {
        AmahiApplication.from(getActivity()).inject(this);
    }

    private void setUpSettingsMenu() {
        setHasOptionsMenu(true);
    }

    private void setUpAuthenticationListener() {
        getAccountManager().addOnAccountsUpdatedListener(this, null, false);
    }

    private AccountManager getAccountManager() {
        return AccountManager.get(getActivity());
    }

    @Override
    public void onAccountsUpdated(Account[] accounts) {
        if (isVisible()) {
            return;
        }

        if (getAccounts().isEmpty()) {
            setUpAccount();
        }
    }

    private void setUpContentRefreshing() {
        SwipeRefreshLayout refreshLayout = getRefreshLayout();

        refreshLayout.setColorSchemeResources(
            android.R.color.holo_blue_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_green_light,
            android.R.color.holo_red_light);

        refreshLayout.setOnRefreshListener(() -> {
            ViewDirector.of(getActivity(), R.id.animator_content).show(R.id.empty_view);
            setUpServers(new Bundle());
        });
    }

    private boolean getServerTitleClicked() {
        return this.mServerTitleClicked;
    }

    private void setServerTitleClicked(boolean option) {
        this.mServerTitleClicked = option;
    }

    private List<Account> getAccounts() {
        return Arrays.asList(getAccountManager().getAccountsByType(AmahiAccount.TYPE));
    }

    private void setUpAccount() {
        getAccountManager().addAccount(AmahiAccount.TYPE, AmahiAccount.TYPE_TOKEN, null, null, getActivity(), this, null);
    }

    private void setUpAuthenticationToken() {
        if (!getAccounts().isEmpty()) {
            Account account = getAccounts().get(0);
            getAccountManager().getAuthToken(account, AmahiAccount.TYPE, null, getActivity(), this, null);
        }
    }

    @Override
    public void run(AccountManagerFuture<Bundle> accountManagerFuture) {
        try {
            Bundle accountManagerResult = accountManagerFuture.getResult();

            String authenticationToken = accountManagerResult.getString(AccountManager.KEY_AUTHTOKEN);

            if (authenticationToken != null) {
                Account[] accounts = getAccountManager().getAccounts();
                Account account = accounts[0];

                String isLocalUser = getAccountManager().getUserData(account, "is_local");
                String ip = getAccountManager().getUserData(account, "ip");
                if (isLocalUser.equals("F")) {
                    setUpServers(authenticationToken);
                } else {
                    setUpLocalServerApi(authenticationToken, ip);
                    BusProvider.getBus().post(new SharesSelectedEvent());
                    setUpLocalNavigation();
                }
            } else {
                setUpAuthenticationToken();
            }
        } catch (OperationCanceledException e) {
            tearDownActivity();
        } catch (IOException | AuthenticatorException e) {
            throw new RuntimeException(e);
        }
    }

    private void setUpLocalNavigation() {
        setUpNavigation();
        getRefreshLayout().setRefreshing(false);
        showContent();
    }

    private void tearDownActivity() {
        getActivity().finish();
    }

    private void setUpServers(Bundle state) {
        getRefreshLayout().setRefreshing(true);
        setUpServersAdapter();
        setUpServersContent(state);
    }

    private void setUpServersAdapter() {
        if (!areServersLoaded())
            serversAdapter = new ServersAdapter(getActivity());
    }

    private void setUpServersContent(Bundle state) {
        if (isServersStateValid(state)) {
            setUpServersState(state);
            setUpNavigation();

            List<Server> servers = state.getParcelableArrayList(State.SERVERS);
            selectSavedServer(servers);
        } else {
            setUpAuthentication();
        }
    }

    private boolean isServersStateValid(Bundle state) {
        return (state != null) && state.containsKey(State.SERVERS);
    }

    private void setUpServersState(Bundle state) {
        List<Server> servers = state.getParcelableArrayList(State.SERVERS);

        setUpServersContent(servers);

        showContent();
    }

    private void setUpServersContent(List<Server> servers) {
        if (!CheckTV.isATV(getContext()))
            getServersAdapter().replaceWith(filterActiveServers(servers));
        else {
            List<Server> serverList = filterActiveServers(servers);
            String serverName = Preferences.getPreference(getContext()).getString(getString(R.string.pref_server_select_key), servers.get(0).getName());

            if (serverList.get(0).getName().matches(serverName))
                getServersAdapter().replaceWith(serverList);

            else {
                int index = findTheServer(serverList);
                getServersAdapter().replaceWith(swappedServers(index, serverList));
            }
        }
    }

    private int findTheServer(List<Server> serverList) {
        String serverName = Preferences.getPreference(getContext()).getString(getString(R.string.pref_server_select_key), serverList.get(0).getName());

        for (int i = 0; i < serverList.size(); i++) {
            if (serverName.matches(serverList.get(i).getName()))
                return i;
        }
        return 0;
    }

    private List<Server> swappedServers(int index, List<Server> serverList) {
        Server firstServer = serverList.get(0);
        serverList.set(0, serverList.get(index));
        serverList.set(index, firstServer);
        return serverList;
    }

    private ServersAdapter getServersAdapter() {
        return serversAdapter;
    }

    private List<Server> filterActiveServers(List<Server> servers) {
        List<Server> activeServers = new ArrayList<Server>();

        for (Server server : servers) {
            if (server.isActive()) {
                activeServers.add(server);
            }
        }

        return activeServers;
    }

    private void showContent() {
        getRefreshLayout().setRefreshing(false);
        ViewDirector.of(this, R.id.animator_content).show(R.id.layout_content);
    }

    private void setUpAuthentication() {
        if (getAccounts().isEmpty()) {
            setUpAccount();
        } else {
            setUpAuthenticationToken();
        }
    }

    private void setUpServers(String authenticationToken) {
        setUpServersAdapter();
        setUpServersContent(authenticationToken);
    }

    private void setUpServersContent(String authenticationToken) {
        amahiClient.getServers(getContext(), authenticationToken);
    }

    @Subscribe
    public void onServersLoaded(ServersLoadedEvent event) {
        setUpServersContent(event.getServers());

        setUpNavigation();

        selectSavedServer(event.getServers());

        showContent();

        tvIntent = new Intent(getContext(), MainTVActivity.class);

        tvIntent.putParcelableArrayListExtra(getString(R.string.intent_servers), new ArrayList<>(filterActiveServers(event.getServers())));
    }

    private SwipeRefreshLayout getRefreshLayout() {
        return (SwipeRefreshLayout) getView().findViewById(R.id.layout_refresh);
    }

    private void setUpNavigation() {

        getNavigationListView().setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        setUpNavigationAdapter();

        setUpNavigationListener();

        setUpServerSelectListener();
    }

    private void setUpNavigationAdapter() {
        //Setting the layout of a vertical list dynamically.

        if (!serverClient.isConnected()) {
            getNavigationListView().setAdapter(NavigationDrawerAdapter.newRemoteAdapter(getActivity()));
            return;
        }

        if (serverClient.isConnectedLocal()) {
            getNavigationListView().setAdapter(NavigationDrawerAdapter.newLocalAdapter(getActivity()));
        } else {
            getNavigationListView().setAdapter(NavigationDrawerAdapter.newRemoteAdapter(getActivity()));
        }
    }

    private RecyclerView getNavigationListView() {
        return (RecyclerView) getView().findViewById(R.id.list_navigation);
    }

    private LinearLayout getOfflineFilesLayout() {
        return getView().findViewById(R.id.offline_files_layout);
    }

    private LinearLayout getLinearLayoutSelectedServer() {
        return (LinearLayout) getView().findViewById(R.id.server_select_LinearLayout);
    }

    private TextView getServerNameTextView() {
        return (TextView) getView().findViewById(R.id.server_name);
    }

    private void setUpNavigationListener() {
        getNavigationListView().addOnItemTouchListener(new RecyclerItemClickListener(getContext(), (view, position) -> {
            getNavigationListView().dispatchSetActivated(false);

            view.setActivated(true);

            if (!getServerTitleClicked()) {
                selectedServerListener(position);
            } else {
                selectServerListener(position);
            }
        }));

        getOfflineFilesLayout().setOnClickListener(view -> {
            showOfflineFiles();
        });
    }

    private void selectedServerListener(int position) {

        switch (position) {
            case NavigationDrawerAdapter.NavigationItems.SHARES:
                BusProvider.getBus().post(new SharesSelectedEvent());
                break;

            case NavigationDrawerAdapter.NavigationItems.APPS:
                BusProvider.getBus().post(new AppsSelectedEvent());
                break;

            default:
                break;
        }
    }

    private void selectSavedServer(List<Server> servers) {

        String session = getServerSession();
        if (session != null) {
            List<Server> activeServers = filterActiveServers(servers);
            for (int i = 0; i < activeServers.size(); i++) {
                if (activeServers.get(i).getSession().equals(session)) {

                    getServerNameTextView().setText(getServersAdapter().getItem(i).getName());

                    setServerTitleClicked(false);

                    getServerNameTextView().setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.nav_arrow_down, 0);

                    changeNavigationAdapter();

                    setUpServerConnection(getServersAdapter().getItem(i));
                    break;
                }
            }
        }
    }

    private void selectServerListener(int position) {
        //Changing the Title Server Name
        getServerNameTextView().setText(getServersAdapter().getItem(position).getName());

        //changing serverTitleClicked to false
        setServerTitleClicked(false);

        //set Arrow down
        getServerNameTextView().setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.nav_arrow_down, 0);

        changeNavigationAdapter();

        setupServer(position);
    }

    public void changeNavigationAdapter() {
        getNavigationListView().setAdapter(null);
        setUpServerNavigation();
    }

    public void setupServer(int position) {
        //Setting up server
        server = getServersAdapter().getItem(position);
        setUpServerConnection(server);
    }

    private void setUpServerSelectListener() {
        if (!getServersAdapter().isEmpty()) {
            getLinearLayoutSelectedServer().setOnClickListener(view -> {
                setServerTitleClicked(true);

                getServerNameTextView().setCompoundDrawablesWithIntrinsicBounds(
                    0, 0, R.drawable.nav_arrow_up, 0);

                showServers();
            });
        }
    }

    @Subscribe
    public void onServersLoadFailed(ServersLoadFailedEvent event) {
        showError();
    }

    private void showError() {
        getRefreshLayout().setRefreshing(false);
        ViewDirector.of(this, R.id.animator_content).show(R.id.layout_error);
    }

    private void setUpServerConnection(Server server) {
        this.server = server;
        if (serverClient.isConnected(server)) {
            setUpServerConnection();
        } else {
            serverClient.connect(getContext(), server);
        }
    }

    private void storeServerSession(Server server) {
        Preferences.setServerSession(getContext(), server.getSession());
    }

    private String getServerSession() {
        return Preferences.getServerSession(getContext());
    }

    @Subscribe
    public void onAuthenticationStart(ServerAuthenticationStartEvent event) {
        String authToken = Preferences.getServerToken(getActivity());
        String session = Preferences.getServerSession(getContext());

        if (server.getName().equals(getString(R.string.demo_server_name))
            || (authToken != null && server.getSession().equals(session))) {

            serverClient.onHdaAuthenticated(new ServerAuthenticationCompleteEvent(authToken));
            BusProvider.getBus().post(new SharesSelectedEvent());
            return;
        }

        authenticateHdaUser();
    }

    private void authenticateHdaUser() {
        startAuthenticationActivity();
    }

    private void startAuthenticationActivity() {
        getActivity().startActivityForResult(Intents.Builder.with(getActivity()).buildPINAuthenticationIntent(), PIN_REQUEST_CODE);
    }

    @Subscribe
    public void onServerConnected(ServerConnectedEvent event) {
        setUpServerConnection();
        setUpServerNavigation();
        if (CheckTV.isATV(getContext())) launchTV();
    }

    private void setUpServerConnection() {
        if (!isConnectionAvailable() || isConnectionAuto()) {
            serverClient.connectAuto();
            return;
        }

        if (isConnectionLocal()) {
            serverClient.connectLocal();
        } else {
            serverClient.connectRemote();
        }
    }

    private void launchTV() {
        startActivity(tvIntent);
    }

    private boolean isConnectionAvailable() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        return preferences.contains(getString(R.string.preference_key_server_connection));
    }

    private boolean isConnectionAuto() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String preferenceConnection = preferences.getString(getString(R.string.preference_key_server_connection), null);

        return preferenceConnection.equals(getString(R.string.preference_key_server_connection_auto));
    }

    private boolean isConnectionLocal() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String preferenceConnection = preferences.getString(getString(R.string.preference_key_server_connection), null);

        return preferenceConnection.equals(getString(R.string.preference_key_server_connection_local));
    }

    private void setUpServerNavigation() {
        setUpNavigationAdapter();
    }

    private void showOfflineFiles() {
        BusProvider.getBus().post(new OfflineFilesSelectedEvent());
    }

    private void setUpLocalServerApi(String auth, String ip) {
        serverClient.connectLocalServer(auth, ip);
    }

    @Subscribe
    public void onServerConnectionChanged(ServerConnectionChangedEvent event) {
        Preferences.setServerSession(getContext(), server.getSession());
        setUpServerNavigation();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater);

        menuInflater.inflate(R.menu.action_bar_navigation, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.menu_settings:
                BusProvider.getBus().post(new SettingsSelectedEvent());
                return true;

            default:
                return super.onOptionsItemSelected(menuItem);
        }
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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        tearDownServersState(outState);
    }

    private void tearDownServersState(Bundle state) {
        if (areServersLoaded()) {
            state.putParcelableArrayList(State.SERVERS, new ArrayList<Parcelable>(getServersAdapter().getItems()));
        }
    }

    private boolean areServersLoaded() {
        return getServersAdapter() != null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        tearDownAuthenticationListener();
    }

    private void tearDownAuthenticationListener() {
        getAccountManager().removeOnAccountsUpdatedListener(this);
    }

    /*Sets the adapter for navigation drawer after getting server names*/
    public void showServers() {
        getNavigationListView().setAdapter(null);
        getNavigationListView().setAdapter(new NavigationDrawerAdapter(getServerNames()));
    }

    public ArrayList<String> getServerNames() {
        ArrayList<String> serverArray = new ArrayList<>();
        for (int i = 0; i < getServersAdapter().getCount(); i++)
            serverArray.add(getServersAdapter().getItem(i).getName());
        return serverArray;
    }

    private static final class State {
        public static final String SERVERS = "servers";

        private State() {
        }
    }
}
