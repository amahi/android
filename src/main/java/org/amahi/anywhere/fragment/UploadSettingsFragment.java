/*
 * Copyright (c) 2015 Amahi
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

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;

import com.squareup.otto.Subscribe;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.R;
import org.amahi.anywhere.account.AmahiAccount;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.ServerConnectedEvent;
import org.amahi.anywhere.bus.ServerConnectionChangedEvent;
import org.amahi.anywhere.bus.ServerSharesLoadedEvent;
import org.amahi.anywhere.bus.ServersLoadedEvent;
import org.amahi.anywhere.server.client.AmahiClient;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.Server;
import org.amahi.anywhere.server.model.ServerShare;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

import static org.amahi.anywhere.server.model.Server.filterActiveServers;

/**
 * Upload Settings fragment. Shows upload settings.
 */
public class UploadSettingsFragment extends PreferenceFragment implements
    AccountManagerCallback<Bundle>,
    EasyPermissions.PermissionCallbacks {

    private static final int READ_PERMISSIONS = 110;
    @Inject
    AmahiClient amahiClient;

    @Inject
    ServerClient serverClient;

    private String authenticationToken;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setUpInjections();

        setUpTitle();

        setUpSettings();
    }

    private void setUpInjections() {
        AmahiApplication.from(getActivity()).inject(this);
    }

    private void setUpTitle() {
        getActivity().setTitle(R.string.preference_title_upload_settings);
    }

    private void setUpSettings() {
        setUpSettingsContent();
        setUpSettingsTitle();
        toggleUploadSettings(isUploadEnabled());
        setUpSettingsListeners();
    }

    private void setUpSettingsContent() {
        addPreferencesFromResource(R.xml.upload_settings);
    }

    private void setUpSettingsTitle() {
        getAutoUploadSwitchPreference().setTitle(getAutoUploadTitle(isUploadEnabled()));
    }

    private AccountManager getAccountManager() {
        return AccountManager.get(getActivity());
    }

    private List<Account> getAccounts() {
        return Arrays.asList(getAccountManager().getAccountsByType(AmahiAccount.TYPE));
    }

    private void setUpAuthenticationToken() {
        if (authenticationToken != null) {
            setUpServersContent(authenticationToken);
        } else {
            Account account = getAccounts().get(0);
            getAccountManager().getAuthToken(account, AmahiAccount.TYPE, null, getActivity(), this, null);
        }

    }

    @Override
    public void run(AccountManagerFuture<Bundle> future) {
        try {
            Bundle accountManagerResult = future.getResult();

            authenticationToken = accountManagerResult.getString(AccountManager.KEY_AUTHTOKEN);

            setUpAuthenticationToken();

        } catch (OperationCanceledException e) {
            tearDownActivity();
        } catch (IOException | AuthenticatorException e) {
            throw new RuntimeException(e);
        }
    }

    private void setUpServersContent(String authenticationToken) {
        amahiClient.getServers(getActivity(), authenticationToken);
    }

    @Subscribe
    public void onServersLoaded(ServersLoadedEvent event) {
        setUpServersContent(event.getServers());
    }

    private void setUpServersContent(List<Server> servers) {
        List<Server> activeServers = filterActiveServers(servers);
        String[] serverNames = new String[activeServers.size()];
        String[] serverSessions = new String[activeServers.size()];

        for (int i = 0; i < activeServers.size(); i++) {
            Server activeServer = activeServers.get(i);
            serverNames[i] = activeServer.getName();
            serverSessions[i] = activeServer.getSession();
        }

        getHdaPreference().setEntries(serverNames);
        getHdaPreference().setEntryValues(serverSessions);
        getHdaPreference().setEnabled(true);

        String session = getHdaPreference().getValue();
        if (session != null) {
            setUpServer(session);
        }
    }

    private boolean isUploadEnabled() {
        PreferenceManager preferenceManager = getPreferenceManager();
        return preferenceManager.getSharedPreferences()
            .getBoolean(getString(R.string.preference_key_upload_switch), false);
    }

    private String getAutoUploadTitle(boolean isUploadEnabled) {
        return isUploadEnabled ? "Disable" : "Enable";
    }

    private void setUpSettingsListeners() {
        getAutoUploadSwitchPreference().setOnPreferenceChangeListener((preference, newValue) -> {
            boolean isUploadEnabled = (boolean) newValue;
            if (isUploadEnabled) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    checkReadPermissions();
                    return false;
                }
            }
            toggleUploadSettings(isUploadEnabled);
            preference.setTitle(getAutoUploadTitle(isUploadEnabled));
            return true;
        });
        getHdaPreference().setOnPreferenceChangeListener((preference, newValue) -> {
            setUpServer(String.valueOf(newValue));
            return true;
        });
        getSharePreference().setOnPreferenceChangeListener((preference, newValue) -> {
            getPathPreference().setEnabled(true);
            getAllowOnDataPreference().setEnabled(true);
            return true;
        });
    }

    private void setUpServer(String session) {
        getSharePreference().setEnabled(false);
        getPathPreference().setEnabled(false);
        getAllowOnDataPreference().setEnabled(false);

        Server server = new Server(session);
        setUpServerConnection(server);
    }

    @Subscribe
    public void onServerConnected(ServerConnectedEvent event) {
        setUpServerConnection();
    }

    private void setUpServerConnection(Server server) {
        if (serverClient.isConnected(server)) {
            setUpServerConnection();
        } else {
            serverClient.connect(getActivity(), server);
        }
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


    @Subscribe
    public void onServerConnectionChanged(ServerConnectionChangedEvent event) {
        setUpSharesContent();
    }

    private void setUpSharesContent() {
        if (serverClient.isConnected()) {
            serverClient.getShares();
        }
    }

    @Subscribe
    public void onSharesLoaded(ServerSharesLoadedEvent event) {
        setUpSharesContent(event.getServerShares());
    }

    private void setUpSharesContent(List<ServerShare> shares) {
        String[] shareNames = new String[shares.size()];
        for (int i = 0; i < shares.size(); i++) {
            shareNames[i] = shares.get(i).getName();
        }
        getSharePreference().setEntries(shareNames);
        getSharePreference().setEntryValues(shareNames);
        getSharePreference().setEnabled(true);

        String selectedShare = getSharePreference().getValue();
        if (selectedShare != null) {
            getPathPreference().setEnabled(true);
            getAllowOnDataPreference().setEnabled(true);
        }
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

    private void toggleUploadSettings(boolean isUploadEnabled) {
        if (isUploadEnabled) {
            setUpAuthenticationToken();
        } else {
            getHdaPreference().setEnabled(false);
            getSharePreference().setEnabled(false);
            getPathPreference().setEnabled(false);
            getAllowOnDataPreference().setEnabled(false);
        }
    }

    private Preference getPreference(int id) {
        return findPreference(getString(id));
    }

    private SwitchPreference getAutoUploadSwitchPreference() {
        return (SwitchPreference) getPreference(R.string.preference_key_upload_switch);
    }

    private ListPreference getHdaPreference() {
        return (ListPreference) getPreference(R.string.preference_key_upload_hda);
    }

    private ListPreference getSharePreference() {
        return (ListPreference) getPreference(R.string.preference_key_upload_share);
    }

    private EditTextPreference getPathPreference() {
        return (EditTextPreference) getPreference(R.string.preference_key_upload_path);
    }

    private SwitchPreference getAllowOnDataPreference() {
        return (SwitchPreference) getPreference(R.string.preference_key_upload_data);
    }

    private void tearDownActivity() {
        getActivity().finish();
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        if (requestCode == READ_PERMISSIONS) {
            toggleUploadSettings(true);
            getAutoUploadSwitchPreference().setTitle(getAutoUploadTitle(true));
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        if (requestCode == READ_PERMISSIONS) {
            showPermissionSnackBar(getString(R.string.file_upload_permission_denied));
        }
    }

    private void showPermissionSnackBar(String message) {
        Snackbar.make(getView(), message, Snackbar.LENGTH_LONG)
            .setAction(R.string.menu_settings, v -> new AppSettingsDialog.Builder(UploadSettingsFragment.this).build().show())
            .show();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkReadPermissions() {
        String[] perms = {Manifest.permission.READ_EXTERNAL_STORAGE};
        if (!EasyPermissions.hasPermissions(getContext(), perms)) {
            EasyPermissions.requestPermissions(this, getString(R.string.file_upload_permission),
                READ_PERMISSIONS, perms);
        }
    }
}
