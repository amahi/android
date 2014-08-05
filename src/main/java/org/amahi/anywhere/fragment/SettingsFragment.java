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
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.R;
import org.amahi.anywhere.account.AmahiAccount;
import org.amahi.anywhere.server.ApiConnection;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.util.Android;
import org.amahi.anywhere.util.Intents;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener,
	SharedPreferences.OnSharedPreferenceChangeListener,
	AccountManagerCallback<Boolean>
{
	@Inject
	ServerClient serverClient;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setUpInjections();

		setUpSettings();
	}

	private void setUpInjections() {
		AmahiApplication.from(getActivity()).inject(this);
	}

	private void setUpSettings() {
		setUpSettingsContent();
		setUpSettingsSummary();
		setUpSettingsListeners();
	}

	private void setUpSettingsContent() {
		addPreferencesFromResource(R.xml.settings);
	}

	private void setUpSettingsSummary() {
		ListPreference serverConnection = getPreference(R.string.preference_key_server_connection);
		Preference applicationVersion = getPreference(R.string.preference_key_application_version);

		serverConnection.setSummary(getServerConnectionSummary());
		applicationVersion.setSummary(getApplicationVersionSummary());
	}

	@SuppressWarnings("unchecked")
	private <T extends Preference> T getPreference(int settingId) {
		return (T) findPreference(getString(settingId));
	}

	private String getServerConnectionSummary() {
		ListPreference serverConnection = getPreference(R.string.preference_key_server_connection);

		return String.format("%s", serverConnection.getEntry());
	}

	private String getApplicationVersionSummary() {
		return String.format(
			"Amahi for Android, Version %s\n © 2014, Amahi, https://www.amahi.org/android",
			Android.getApplicationVersion());
	}

	private void setUpSettingsListeners() {
		Preference accountSignOut = getPreference(R.string.preference_key_account_sign_out);
		Preference applicationVersion = getPreference(R.string.preference_key_application_version);
		Preference applicationFeedback = getPreference(R.string.preference_key_application_feedback);
		Preference applicationRating = getPreference(R.string.preference_key_application_rating);

		accountSignOut.setOnPreferenceClickListener(this);
		applicationVersion.setOnPreferenceClickListener(this);
		applicationFeedback.setOnPreferenceClickListener(this);
		applicationRating.setOnPreferenceClickListener(this);
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		if (preference.getKey().equals(getString(R.string.preference_key_account_sign_out))) {
			tearDownAccount();
		}

		if (preference.getKey().equals(getString(R.string.preference_key_application_version))) {
			setUpApplicationVersion();
		}

		if (preference.getKey().equals(getString(R.string.preference_key_application_feedback))) {
			setUpApplicationFeedback();
		}

		if (preference.getKey().equals(getString(R.string.preference_key_application_rating))) {
			setUpApplicationRating();
		}

		return true;
	}

	private void tearDownAccount() {
		Account account = getAccounts().get(0);

		getAccountManager().removeAccount(account, this, null);
	}

	private List<Account> getAccounts() {
		return Arrays.asList(getAccountManager().getAccountsByType(AmahiAccount.TYPE));
	}

	private AccountManager getAccountManager() {
		return AccountManager.get(getActivity());
	}

	@Override
	public void run(AccountManagerFuture<Boolean> accountManagerFuture) {
		tearDownActivity();
	}

	private void tearDownActivity() {
		getActivity().finish();
	}

	private void setUpApplicationVersion() {
		Intent intent = Intents.Builder.with(getActivity()).buildVersionIntent();
		startActivity(intent);
	}

	private void setUpApplicationFeedback() {
		Intent intent = Intents.Builder.with(getActivity()).buildFeedbackIntent();
		startActivity(intent);
	}

	private void setUpApplicationRating() {
		Intent intent = Intents.Builder.with(getActivity()).buildGooglePlayIntent();
		startActivity(intent);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals(getString(R.string.preference_key_server_connection))) {
			setUpSettingsSummary();

			setUpServerConnection();
		}
	}

	private void setUpServerConnection() {
		if (!serverClient.isConnected()) {
			return;
		}

		switch (getServerConnection()) {
			case AUTO:
				serverClient.connectAuto();
				break;

			case LOCAL:
				serverClient.connectLocal();
				break;

			case REMOTE:
				serverClient.connectRemote();
				break;

			default:
				break;
		}
	}

	private ApiConnection getServerConnection() {
		ListPreference serverConnection = getPreference(R.string.preference_key_server_connection);

		if (serverConnection.getValue().equals(getString(R.string.preference_key_server_connection_auto))) {
			return ApiConnection.AUTO;
		}

		if (serverConnection.getValue().equals(getString(R.string.preference_key_server_connection_local))) {
			return ApiConnection.LOCAL;
		}

		if (serverConnection.getValue().equals(getString(R.string.preference_key_server_connection_remote))) {
			return ApiConnection.REMOTE;
		}

		return ApiConnection.AUTO;
	}

	@Override
	public void onResume() {
		super.onResume();

		setUpSettingsPreferenceListener();
	}

	private void setUpSettingsPreferenceListener() {
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onPause() {
		super.onPause();

		tearDownSettingsPreferenceListener();
	}

	private void tearDownSettingsPreferenceListener() {
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}
}
