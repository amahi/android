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

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.annotation.Nullable;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.R;
import org.amahi.anywhere.server.client.ServerClient;

import javax.inject.Inject;

/**
 * Upload Settings fragment. Shows upload settings.
 */
public class UploadSettingsFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener {

	@Inject
	ServerClient serverClient;

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
		getAutoUploadSwitchPreference().setTitle(getAutoUploadTitle());
	}

	private boolean isUploadEnabled() {
		PreferenceManager preferenceManager = getPreferenceManager();
		return preferenceManager.getSharedPreferences()
				.getBoolean(getString(R.string.preference_key_upload_switch), false);
	}

	private String getAutoUploadTitle() {
		return isUploadEnabled() ? "Disable" : "Enable";
	}

	private void setUpSettingsListeners() {
		getAutoUploadSwitchPreference().setOnPreferenceClickListener(this);
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		if (preference.getKey().equals(getString(R.string.preference_key_upload_switch))) {
			toggleUploadSettings(isUploadEnabled());
			preference.setTitle(getAutoUploadTitle());
		}
		return true;
	}

	private void toggleUploadSettings(boolean isUploadEnabled) {
		getHdaPreference().setEnabled(isUploadEnabled);
		getSharePreference().setEnabled(isUploadEnabled);
		getPathPreference().setEnabled(isUploadEnabled);
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

}
