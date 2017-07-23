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

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.annotation.Nullable;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

/**
 * Upload Settings fragment. Shows upload settings.
 */
public class UploadSettingsFragment extends PreferenceFragment implements
		Preference.OnPreferenceChangeListener,
		AccountManagerCallback<Bundle> {

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
		ArrayList<Server> activeServers = filterActiveServers(servers);
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

	private ArrayList<Server> filterActiveServers(List<Server> servers) {
		ArrayList<Server> activeServers = new ArrayList<>();

		for (Server server : servers) {
			if (server.isActive()) {
				activeServers.add(server);
			}
		}

		return activeServers;
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
		getAutoUploadSwitchPreference().setOnPreferenceChangeListener(this);
		getHdaPreference().setOnPreferenceChangeListener(this);
		getSharePreference().setOnPreferenceChangeListener(this);
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		String key = preference.getKey();
		if (key.equals(getString(R.string.preference_key_upload_switch))) {
			boolean isUploadEnabled = (boolean) newValue;
			toggleUploadSettings(isUploadEnabled);
			preference.setTitle(getAutoUploadTitle(isUploadEnabled));
		} else if (key.equals(getString(R.string.preference_key_upload_hda))) {
			setUpServer(String.valueOf(newValue));
		} else if (key.equals(getString(R.string.preference_key_upload_share))) {
			getPathPreference().setEnabled(true);
		}
		return true;
	}

	private void setUpServer(String session) {
		getSharePreference().setEnabled(false);
		getPathPreference().setEnabled(false);

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
}
