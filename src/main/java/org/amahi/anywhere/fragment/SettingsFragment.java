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
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.google.android.material.snackbar.Snackbar;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.R;
import org.amahi.anywhere.account.AmahiAccount;
import org.amahi.anywhere.activity.NavigationActivity;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.UploadSettingsOpeningEvent;
import org.amahi.anywhere.server.ApiConnection;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.util.Android;
import org.amahi.anywhere.util.Constants;
import org.amahi.anywhere.util.Fragments;
import org.amahi.anywhere.util.Intents;
import org.amahi.anywhere.util.LocaleHelper;
import org.amahi.anywhere.util.Preferences;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

/**
 * Settings fragment. Shows application's settings.
 */
public class SettingsFragment extends PreferenceFragmentCompat implements
    SharedPreferences.OnSharedPreferenceChangeListener,
    AccountManagerCallback<Boolean>,
    AlertDialogFragment.SignOutDialogCallback {
    @Inject
    ServerClient serverClient;
    public static final int RESULT_THEME_UPDATED = 3;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpInjections();
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        setUpSettings();
    }

    private void setUpInjections() {
        AmahiApplication.from(getActivity()).inject(this);
    }

    private void setUpTitle() {
        getActivity().setTitle(R.string.title_settings);
    }

    private void setUpSettings() {
        setUpSettingsContent();
        setUpSettingsSummary();
        setUpSettingsListeners();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            getListView().setVerticalScrollbarThumbDrawable(ContextCompat.getDrawable(getContext(), R.drawable.scrollbar));
        }
    }

    private void setUpSettingsContent() {
        addPreferencesFromResource(R.xml.settings);
        setUpLanguageContent();
    }

    private void setUpLanguageContent() {
        ListPreference language = (ListPreference) getPreference(R.string.preference_key_language);
        HashMap<CharSequence, CharSequence> entryMap = new HashMap<>();
        CharSequence[] entries = language.getEntries();
        CharSequence[] values = language.getEntryValues();
        for (int i = 0; i < entries.length; i++) {
            entryMap.put(entries[i], values[i]);
        }
        Arrays.sort(entries, 1, entries.length);
        for (int i = 0; i < entries.length; i++) {
            values[i] = entryMap.get(entries[i]);
        }
        language.setEntries(entries);
        language.setEntryValues(values);
    }

    private void setUpSettingsSummary() {
        ListPreference serverConnection = (ListPreference) getPreference(R.string.preference_key_server_connection);
        ListPreference language = (ListPreference) getPreference(R.string.preference_key_language);
        Preference applicationVersion = getPreference(R.string.preference_key_about_version);
        Preference autoUpload = getPreference(R.string.preference_screen_key_upload);

        language.setSummary(getLanguageSummary());
        serverConnection.setSummary(getServerConnectionSummary());
        applicationVersion.setSummary(getApplicationVersionSummary());
        autoUpload.setSummary(getAutoUploadSummary());
    }

    private String getLanguageSummary() {
        ListPreference language = (ListPreference) getPreference(R.string.preference_key_language);

        return String.format("%s", language.getEntry());
    }

    private String getServerConnectionSummary() {
        ListPreference serverConnection = (ListPreference) getPreference(R.string.preference_key_server_connection);

        return String.format("%s", serverConnection.getEntry());
    }

    private Preference getPreference(int id) {
        return findPreference(getString(id));
    }

    private String getApplicationVersionSummary() {
        return String.format(
            "Amahi for Android %s\nwww.amahi.org/android",
            Android.getApplicationVersion());
    }

    private String getAutoUploadSummary() {
        return isUploadEnabled() ? "Enabled" : "Disabled";
    }

    private boolean isUploadEnabled() {
        PreferenceManager preferenceManager = getPreferenceManager();
        return preferenceManager.getSharedPreferences()
            .getBoolean(getString(R.string.preference_key_upload_switch), false);
    }

    private void setUpSettingsListeners() {
        Preference accountSignOut = getPreference(R.string.preference_key_account_sign_out);
        Preference applicationIntro = getPreference(R.string.preference_key_about_intro);
        Preference applicationVersion = getPreference(R.string.preference_key_about_version);
        Preference applicationFeedback = getPreference(R.string.preference_key_about_feedback);
        Preference applicationRating = getPreference(R.string.preference_key_about_rating);
        Preference shareApp = getPreference(R.string.preference_key_tell_a_friend);
        Preference autoUpload = getPreference(R.string.preference_screen_key_upload);
        Preference lightTheme = getPreference(R.string.pref_key_light_theme);

        accountSignOut.setOnPreferenceClickListener(preference -> {
            setUpSignOutDialogFragment();
            return true;
        });
        applicationIntro.setOnPreferenceClickListener(preference -> {
            setUpApplicationIntro();
            return true;
        });
        applicationVersion.setOnPreferenceClickListener(preference -> {
            setUpApplicationVersion();
            return true;
        });
        applicationFeedback.setOnPreferenceClickListener(preference -> {
            setUpApplicationFeedback();
            return true;
        });
        applicationRating.setOnPreferenceClickListener(preference -> {
            setUpApplicationRating();
            return true;
        });
        shareApp.setOnPreferenceClickListener(preference -> {
            sharedIntent();
            return true;
        });
        autoUpload.setOnPreferenceClickListener(preference -> {
            openUploadSettingsFragment();
            return true;
        });
        lightTheme.setOnPreferenceChangeListener((preference, newValue) -> {
            setUpTheme((Boolean) newValue);
            return true;
        });

    }

    private void setUpApplicationIntro() {
        Preferences.setFirstRun(getActivity());
        Intent intent = Intents.Builder.with(getActivity()).buildIntroductionIntent();
        startActivity(intent);
    }

    private void openUploadSettingsFragment() {
        BusProvider.getBus().post(new UploadSettingsOpeningEvent());
    }

    private void setUpTheme(Boolean isLightThemeEnabled) {
        getActivity().setResult(RESULT_THEME_UPDATED);
        if (isLightThemeEnabled) {
            AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_YES);
        }

        AmahiApplication.getInstance().setIsLightThemeEnabled(isLightThemeEnabled);
        getActivity().finish();
        startActivity(getActivity().getIntent());
    }

    private void setUpSignOutDialogFragment() {
        AlertDialogFragment signOutDialogFragment = new AlertDialogFragment();
        signOutDialogFragment.setTargetFragment(this, 1);
        Bundle bundle = new Bundle();
        bundle.putInt(Fragments.Arguments.DIALOG_TYPE, AlertDialogFragment.SIGN_OUT_DIALOG);
        signOutDialogFragment.setArguments(bundle);
        signOutDialogFragment.show(getFragmentManager(), "sign_out_dialog");
    }

    @Override
    public void dialogPositiveButtonOnClick() {
        tearDownAccount();
    }

    @Override
    public void dialogNegativeButtonOnClick() {

    }

    private void tearDownAccount() {
        if (!getAccounts().isEmpty()) {
            Account account = getAccounts().get(0);

            getAccountManager().removeAccount(account, this, null);
        } else {
            Toast.makeText(getActivity(), R.string.message_logout, Toast.LENGTH_SHORT).show();
            tearDownActivity();
        }
    }

    private List<Account> getAccounts() {
        return Arrays.asList(getAccountManager().getAccountsByType(AmahiAccount.TYPE));
    }

    private AccountManager getAccountManager() {
        return AccountManager.get(getActivity());
    }

    @Override
    public void run(AccountManagerFuture<Boolean> accountManagerFuture) {
        Toast.makeText(getActivity(), R.string.message_logout, Toast.LENGTH_SHORT).show();
        tearDownActivity();
    }

    private void tearDownActivity() {
        Intent myIntent = new Intent(getActivity().getApplicationContext(), NavigationActivity.class);
        myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(myIntent);
        getActivity().finish();
    }

    private void sharedIntent() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_subject));
        sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_message));
        sendIntent.setType(Constants.emailTextPlainType);
        startActivity(Intent.createChooser(sendIntent, getString(R.string.share_screen_title)));
    }

    private void setUpApplicationVersion() {
        Intent intent = Intents.Builder.with(getActivity()).buildVersionIntent(getActivity());
        startActivity(intent);
    }

    private void setUpApplicationFeedback() {
        Intent intent = Intents.Builder.with(getActivity()).buildFeedbackIntent();
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Snackbar.make(getView(), getString(R.string.application_not_found), Snackbar.LENGTH_SHORT).show();
        }
    }

    private void setUpApplicationRating() {
        Intent intent = Intents.Builder.with(getActivity()).buildGooglePlayIntent();
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Snackbar.make(getView(), getString(R.string.application_not_found), Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.preference_key_server_connection))) {
            setUpSettingsSummary();

            setUpServerConnection();
        } else if (key.equals(getString(R.string.preference_key_language))) {
            setUpSettingsSummary();

            setUpLanguage();
        }
    }

    private void setUpLanguage() {
        LocaleHelper.setLocale(getContext(), getLanguage());
        tearDownActivity();
    }

    private String getLanguage() {
        ListPreference language = (ListPreference) getPreference(R.string.preference_key_language);

        return language.getValue();
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
        ListPreference serverConnection = (ListPreference) getPreference(R.string.preference_key_server_connection);

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
        setUpTitle();

        // for updating Auto Upload Title
        getPreference(R.string.preference_screen_key_upload).setSummary(getAutoUploadSummary());
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
