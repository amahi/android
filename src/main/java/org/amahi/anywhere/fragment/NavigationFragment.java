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
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.squareup.otto.Subscribe;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.R;
import org.amahi.anywhere.account.AmahiAccount;
import org.amahi.anywhere.adapter.NavigationDrawerAdapter;
import org.amahi.anywhere.bus.AppsSelectedEvent;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.OfflineFilesSelectedEvent;
import org.amahi.anywhere.bus.RecentFilesSelectedEvent;
import org.amahi.anywhere.bus.ServerConnectedEvent;
import org.amahi.anywhere.bus.ServerConnectionChangedEvent;
import org.amahi.anywhere.bus.ServerConnectionFailedEvent;
import org.amahi.anywhere.bus.ServersLoadFailedEvent;
import org.amahi.anywhere.bus.ServersLoadedEvent;
import org.amahi.anywhere.bus.SettingsSelectedEvent;
import org.amahi.anywhere.bus.SharesSelectedEvent;
import org.amahi.anywhere.server.client.AmahiClient;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.Server;
import org.amahi.anywhere.tv.activity.MainTVActivity;
import org.amahi.anywhere.util.CheckTV;
import org.amahi.anywhere.util.MultiSwipeRefreshLayout;
import org.amahi.anywhere.util.Preferences;
import org.amahi.anywhere.util.RecyclerItemClickListener;
import org.amahi.anywhere.util.ViewDirector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

/**
 * Navigation fragments. Shows main application sections and servers list as well.
 */
public class NavigationFragment extends Fragment implements AccountManagerCallback<Bundle>, OnAccountsUpdateListener {

    @Inject
    AmahiClient amahiClient;

    @Inject
    ServerClient serverClient;

    View view;

    private Intent tvIntent;
    private Context mContext;
    private Activity mActivity;

    private boolean areServersVisible;
    private List<Server> serversList;

    public static final String TAG = NavigationFragment.class.getSimpleName();

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;

        lodNavig("onattach");

        if (context instanceof Activity) {
            mActivity = (Activity) context;
        }
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        lodNavig("oncreateview");
        view = layoutInflater.inflate(R.layout.fragment_navigation, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        lodNavig("onact create");

        setUpInjections();

        setUpSettingsMenu();

        setUpAuthenticationListener();

        setUpContentRefreshing();

        setUpServers(savedInstanceState);

    }

    private void setUpInjections() {
        AmahiApplication.from(mActivity).inject(this);
    }

    private void setUpSettingsMenu() {
        setHasOptionsMenu(true);
    }

    private void setUpAuthenticationListener() {
        getAccountManager().addOnAccountsUpdatedListener(this, null, false);
    }

    private AccountManager getAccountManager() {
        return AccountManager.get(mContext);
    }

    @Override
    public void onAccountsUpdated(Account[] accounts) {
        lodNavig("onacc update");
        if (getAccounts().isEmpty()) {
            setUpAccount();
        }
    }

    private void setUpContentRefreshing() {
        lodNavig("content refres");
        SwipeRefreshLayout refreshLayout = getRefreshLayout();

        refreshLayout.setColorSchemeResources(
            android.R.color.holo_blue_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_green_light,
            android.R.color.holo_red_light);

        refreshLayout.setOnRefreshListener(() -> {
            ViewDirector.of(mActivity, R.id.animator_content).show(R.id.empty_view);
            setUpServers(new Bundle());
        });
    }

    private List<Account> getAccounts() {
        return Arrays.asList(getAccountManager().getAccountsByType(AmahiAccount.TYPE));
    }

    private void setUpAccount() {
        lodNavig("set account");
        getAccountManager().addAccount(AmahiAccount.TYPE, AmahiAccount.TYPE_TOKEN, null, null, mActivity, this, null);
    }

    private void setUpAuthenticationToken() {
        lodNavig("setauthtoken");
        Account account = getAccounts().get(0);

        getAccountManager().getAuthToken(account, AmahiAccount.TYPE, null, mActivity, this, null);
    }

    @Override
    public void run(AccountManagerFuture<Bundle> accountManagerFuture) {
        try {
            Bundle accountManagerResult = accountManagerFuture.getResult();
            Account account = getAccountManager().getAccounts()[0];

            String isLocalUser = checkIfLocalUser();
            String ip = getAccountManager().getUserData(account, "ip");

            String authenticationToken = accountManagerResult.getString(AccountManager.KEY_AUTHTOKEN);

            lodNavig("run");

            if (authenticationToken != null) {
                lodNavig(isLocalUser + " " + authenticationToken);
                if (isLocalUser.equals("F")) {
                    setUpServers(authenticationToken);
                } else {
                    BusProvider.getBus().post(new SharesSelectedEvent());
                    setUpLocalServerApi(authenticationToken, ip);
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

    private String checkIfLocalUser() {
        lodNavig("check local");
        Account account = getAccountManager().getAccounts()[0];
        lodNavig(getAccountManager().getUserData(account, "is_local"));
        return getAccountManager().getUserData(account, "is_local");
    }

    private void tearDownActivity() {
        Log.v(TAG, "Tearing Down Activity");
        mActivity.finish();
    }

    private void setUpServers(Bundle state) {
        lodNavig("setserver");
        getRefreshLayout().setRefreshing(true);
        setUpNavigationState(state);
        setUpServersContent(state);

        setUpNavigationListener();
    }

    private void setUpServersContent(Bundle state) {
        lodNavig("setservercontent");
        this.serversList = new ArrayList<>();

        if (isServersStateValid(state)) {
            setUpServersState(state);
            setUpNavigation();

            this.serversList = state.getParcelableArrayList(State.SERVERS);
            selectSavedServer(serversList);
        } else {
            setUpAuthentication();
        }
    }

    private boolean isServersStateValid(Bundle state) {
        return (state != null) && state.containsKey(State.SERVERS);
    }

    private void setUpServersState(Bundle state) {
        lodNavig("setserverstate");
        List<Server> servers = state.getParcelableArrayList(State.SERVERS);

        setUpServersContent(servers);

        showContent();
    }

    private void setUpServersContent(List<Server> servers) {
        if (!CheckTV.isATV(mContext)) {
            replaceServersList(filterActiveServers(servers));
        } else {
            serversList = filterActiveServers(servers);
            String serverName = Preferences.getPreference(mContext).getString(getString(R.string.pref_server_select_key), serversList.get(0).getName());

            if (serversList.get(0).getName().matches(serverName))
                replaceServersList(serversList);

            else {
                int index = findTheServer(serversList);
                replaceServersList(swappedServers(index, serversList));
            }
        }
    }

    private void replaceServersList(List<Server> servers) {
        this.serversList.clear();
        this.serversList.addAll(servers);
    }

    private int findTheServer(List<Server> serverList) {
        lodNavig("findserver");
        String serverName = Preferences.getPreference(mContext).getString(getString(R.string.pref_server_select_key), serverList.get(0).getName());

        for (int i = 0; i < serverList.size(); i++) {
            if (serverName.matches(serverList.get(i).getName()))
                lodNavig(serverList.get(i).getName());
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

    private List<Server> getServersList() {
        return serversList;
    }

    private List<Server> filterActiveServers(List<Server> servers) {
        List<Server> activeServers = new ArrayList<>();

        for (Server server : servers) {
            if (server.isActive()) {
                activeServers.add(server);
            }
        }

        return activeServers;
    }

    private void showContent() {
        lodNavig("showcontent");
        getRefreshLayout().setRefreshing(false);
        ViewDirector.of(this, R.id.animator_content).show(R.id.layout_content);
    }

    private void setUpAuthentication() {
        lodNavig("setupauth");
        if (getAccounts().isEmpty()) {
            setUpAccount();
        } else {
            setUpAuthenticationToken();
        }
    }

    private void setUpNavigationState(Bundle state) {
        if (isServersStateValid(state)) {
            areServersVisible = state.getBoolean(State.SERVERS_VISIBLE, false);
        }
    }

    private void setUpServers(String authenticationToken) {
        setUpServersContent(authenticationToken);
    }

    private void setUpServersContent(String authenticationToken) {
        lodNavig("setservercontent");
        lodNavig("auth - " + authenticationToken);
        amahiClient.getServers(mContext, authenticationToken);
    }

    @Subscribe
    public void onServersLoaded(ServersLoadedEvent event) {
        lodNavig("onserverloaded");
        setUpServersContent(event.getServers());

        setUpNavigation();

        selectSavedServer(event.getServers());

        showContent();

        tvIntent = new Intent(mContext, MainTVActivity.class);

        tvIntent.putParcelableArrayListExtra("INTENT_SERVERS", new ArrayList<>(filterActiveServers(event.getServers())));
    }

    private SwipeRefreshLayout getRefreshLayout() {
        MultiSwipeRefreshLayout multiSwipeRefreshLayout = getView().findViewById(R.id.layout_refresh);
        multiSwipeRefreshLayout.setSwipeableChildren(R.id.layout_content);
        return multiSwipeRefreshLayout;
    }

    private void setUpNavigation() {
        setUpNavigationList();

        setUpServerSelectListener();
    }

    private void setUpNavigationList() {
        lodNavig("setupnavlist");
        getNavigationListView().setVisibility(View.VISIBLE);
        getNavigationListView().setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));

        if (!areServersVisible) {
            showNavigationItems();
        } else {
            showServers();
            hideOfflineLayout();
            hideRecentLayout();
        }

    }

    private void hideOfflineLayout() {
        getOfflineFilesLayout().setVisibility(View.GONE);
    }

    private void hideRecentLayout() {
        getRecentFilesLayout().setVisibility(View.GONE);
    }

    private void setUpNavigationAdapter() {
        //Setting the layout of a vertical list dynamically.

        if (!serverClient.isConnected()) {
            getNavigationListView().setAdapter(NavigationDrawerAdapter.newRemoteAdapter(mActivity));
            return;
        }

        if (serverClient.isConnectedLocal()) {
            getNavigationListView().setAdapter(NavigationDrawerAdapter.newLocalAdapter(mActivity));
        } else {
            getNavigationListView().setAdapter(NavigationDrawerAdapter.newRemoteAdapter(mActivity));
        }
    }

    private RecyclerView getNavigationListView() {
        return (RecyclerView) getView().findViewById(R.id.list_navigation);
    }

    private LinearLayout getOfflineFilesLayout() {
        return getView().findViewById(R.id.offline_files_layout);
    }

    private LinearLayout getRecentFilesLayout() {
        return getView().findViewById(R.id.recent_files_layout);
    }

    private LinearLayout getLinearLayoutSelectedServer() {
        return getView().findViewById(R.id.server_select_LinearLayout);
    }

    private TextView getServerNameTextView() {
        return getView().findViewById(R.id.server_name);
    }

    private void setUpNavigationListener() {
        if(checkIfLocalUser().equals("F")) {
            getNavigationListView().addOnItemTouchListener(new RecyclerItemClickListener(mContext, (view, position) -> {
                getNavigationListView().dispatchSetActivated(false);

                view.setActivated(true);

                if (!areServersVisible) {
                    selectedServerListener(position);
                } else {
                    serverClicked(position);
                }
            }));
        }

        getOfflineFilesLayout().setOnClickListener(view -> showOfflineFiles());

        getRecentFilesLayout().setOnClickListener(view -> showRecentFiles());
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
        String serverName = getServerName();

        List<Server> activeServers = filterActiveServers(servers);
        if (serverName != null) {
            for (int i = 0; i < activeServers.size(); i++) {
                if (activeServers.get(i).getName().equals(serverName)) {
                    getServerNameTextView().setText(serverName);
                    setUpServerConnection(activeServers.get(i));
                    storeServerName(activeServers.get(i));
                    return;
                }
            }
        }

        selectFirstServer(activeServers);
    }

    private void selectFirstServer(List<Server> activeServers) {
        if (!activeServers.isEmpty()) {
            getServerNameTextView().setText(activeServers.get(0).getName());
            setUpServerConnection(activeServers.get(0));
            storeServerName(activeServers.get(0));
        } else {
            String serverName = getServerName();
            if (serverName != null) {
                getServerNameTextView().setText(serverName);
            }
        }
    }

    private void serverClicked(int position) {
        setupServer(position);

        //Changing the Title Server Name
        getServerNameTextView().setText(getServersList().get(position).getName());

        showNavigationItems();

        storeServerName(getServersList().get(position));

        BusProvider.getBus().post(new SharesSelectedEvent());
    }

    public void setupServer(int position) {
        //Setting up server
        Server server = getServersList().get(position);
        setUpServerConnection(server);
    }

    private void setUpServerSelectListener() {
        getLinearLayoutSelectedServer().setOnClickListener(view -> {
            if (areServersVisible) {
                showNavigationItems();
            } else {
                showServers();
                hideOfflineLayout();
                hideRecentLayout();
            }
        });
    }

    private void showNavigationItems() {
        areServersVisible = false;
        setUpNavigationAdapter();

        if(checkIfLocalUser().equals("F")) {
            getServerNameTextView().setCompoundDrawablesWithIntrinsicBounds(
                0, 0, R.drawable.nav_arrow_down, 0);
        }

        getOfflineFilesLayout().setVisibility(View.VISIBLE);
        getRecentFilesLayout().setVisibility(View.VISIBLE);
    }

    @Subscribe
    public void onServersLoadFailed(ServersLoadFailedEvent event) {
        lodNavig("server load failed");
        showOfflineNavigation();
    }

    private void showOfflineNavigation() {
        lodNavig("offline-nav");
        getRefreshLayout().setRefreshing(false);

        String serverName = getServerName();
        if (serverName != null) {
            getServerNameTextView().setText(serverName);
        }

        getOfflineFilesLayout().setVisibility(View.VISIBLE);
        getRecentFilesLayout().setVisibility(View.VISIBLE);

        getOfflineFilesLayout().setOnClickListener(
            view -> showOfflineFiles()
        );

        areServersVisible = false;
        setUpNavigationList();
        getLinearLayoutSelectedServer().setOnClickListener((v) -> {
            Toast.makeText(mContext, R.string.message_connection_error, Toast.LENGTH_SHORT).show();
        });

        showContent();
        Toast.makeText(mContext, R.string.message_connection_error, Toast.LENGTH_SHORT).show();
    }

    @Subscribe
    public void onServerConnectionFailed(ServerConnectionFailedEvent event) {
        lodNavig("server connection failed");
        showOfflineNavigation();
    }

    private void setUpServerConnection(Server server) {
        if (serverClient.isConnected(server)) {
            setUpServerConnection();
        } else {
            serverClient.connect(mContext, server);
        }
    }

    private void storeServerName(Server server) {
        Preferences.setServerName(mContext, server.getName());
    }

    private String getServerName() {
        return Preferences.getServerName(mContext);
    }

    @Subscribe
    public void onServerConnected(ServerConnectedEvent event) {
        lodNavig("servers connected");
        setUpServerConnection();
        setUpNavigationList();
        showContent();

        if (CheckTV.isATV(mContext)) launchTV();
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
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mActivity);

        return preferences.contains(getString(R.string.preference_key_server_connection));
    }

    private boolean isConnectionAuto() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
        String preferenceConnection = preferences.getString(getString(R.string.preference_key_server_connection), null);

        return preferenceConnection.equals(getString(R.string.preference_key_server_connection_auto));
    }

    private boolean isConnectionLocal() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
        String preferenceConnection = preferences.getString(getString(R.string.preference_key_server_connection), null);

        return preferenceConnection.equals(getString(R.string.preference_key_server_connection_local));
    }

    private void showOfflineFiles() {
        BusProvider.getBus().post(new OfflineFilesSelectedEvent());
    }

    private void showRecentFiles() {
        BusProvider.getBus().post(new RecentFilesSelectedEvent());
    }

    private void setUpLocalServerApi(String auth, String ip) {
        serverClient.connectLocalServer(auth, ip);
        setUpServersContent(auth);
    }

    @Subscribe
    public void onServerConnectionChanged(ServerConnectionChangedEvent event) {
        lodNavig("server connection changed");
        areServersVisible = false;
        setUpNavigationList();
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
            state.putParcelableArrayList(State.SERVERS, new ArrayList<Parcelable>(getServersList()));
        }

        state.putBoolean(State.SERVERS_VISIBLE, areServersVisible);
    }

    private boolean areServersLoaded() {
        return getServersList() != null;
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
        areServersVisible = true;
        getNavigationListView().setAdapter(null);
        getNavigationListView().setAdapter(new NavigationDrawerAdapter(mContext, getServerNames()));
        getServerNameTextView().setCompoundDrawablesWithIntrinsicBounds(
            0, 0, R.drawable.nav_arrow_up, 0);
    }

    public ArrayList<String> getServerNames() {
        ArrayList<String> serverArray = new ArrayList<>();
        for (int i = 0; i < getServersList().size(); i++)
            serverArray.add(getServersList().get(i).getName());
        return serverArray;
    }

    private static final class State {
        static final String SERVERS = "servers";
        static final String SERVERS_VISIBLE = "servers_visible";

        private State() {
        }
    }

    private void lodNavig(String msg) {
        Log.v("navig-nau", msg);
    }
}
