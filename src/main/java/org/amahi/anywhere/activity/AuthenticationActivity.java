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

package org.amahi.anywhere.activity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Fragment;
import android.os.Bundle;

import com.google.android.material.textfield.TextInputLayout;

import androidx.appcompat.app.AppCompatDelegate;

import android.widget.EditText;

import com.squareup.otto.Subscribe;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.R;
import org.amahi.anywhere.account.AccountAuthenticatorAppCompatActivity;
import org.amahi.anywhere.account.AmahiAccount;
import org.amahi.anywhere.bus.AuthenticationSucceedEvent;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.server.client.AmahiClient;

import org.amahi.anywhere.bus.PINAccessEvent;
import org.amahi.anywhere.bus.ServerAuthenticationCompleteEvent;
import org.amahi.anywhere.fragment.MainLoginFragment;
import org.amahi.anywhere.fragment.PINAccessFragment;
import org.amahi.anywhere.server.model.Server;
import org.amahi.anywhere.util.Fragments;
import org.amahi.anywhere.util.Intents;
import org.amahi.anywhere.util.Preferences;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

/**
 * Authentication activity. Allows user authentication. If operation succeed
 * the authentication token is saved at the {@link android.accounts.AccountManager}.
 */
public class AuthenticationActivity extends AccountAuthenticatorAppCompatActivity {
    @Inject
    AmahiClient amahiClient;

    private String accountType = AmahiAccount.TYPE;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (AmahiApplication.getInstance().isLightThemeEnabled()) {
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        setUpInjections();

        setUpAuthenticationFragment();
    }

    private void setUpInjections() {
        AmahiApplication.from(this).inject(this);
    }

    private String getUsername() {
        return getUsernameEdit().getText().toString();
    }

    private EditText getUsernameEdit() {
        TextInputLayout username_layout = findViewById(R.id.username_layout);
        return username_layout.getEditText();
    }

    private String getPassword() {
        return getPasswordEdit().getText().toString();
    }

    private EditText getPasswordEdit() {
        TextInputLayout password_layout = findViewById(R.id.password_layout);
        return password_layout.getEditText();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        accountType = savedInstanceState.getString(State.ACCOUNT_TYPE, AmahiAccount.TYPE);
    }

    private void setUpAuthenticationFragment() {

        accountType = getIntent().getStringExtra(Intents.Extras.ACCOUNT_TYPE);
        if (accountType != null && (accountType.equals(AmahiAccount.TYPE_ADMIN)
            || accountType.equals(AmahiAccount.TYPE_USER))) {

            PINAccessFragment f = (PINAccessFragment) getFragmentManager().findFragmentByTag(PINAccessFragment.TAG);
            if (f == null) {
                showPINAccessFragment();
            }
        } else
            {
                try {
                    accountType = AmahiAccount.TYPE;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                MainLoginFragment f = (MainLoginFragment) getFragmentManager().findFragmentByTag(MainLoginFragment.TAG);
                if (f == null) {
                    showMainLoginFragment();
                }


            }
        }


    private void showMainLoginFragment() {
        Fragment fragment = Fragments.Builder.buildMainLoginFragment();
        getFragmentManager()
            .beginTransaction()
            .replace(R.id.main_container, fragment, MainLoginFragment.TAG)
            .commit();
    }

    @Subscribe
    public void onAuthenticationSucceed(AuthenticationSucceedEvent event) {
        if (accountType.equals(AmahiAccount.TYPE)) {
            MainLoginFragment fragment = (MainLoginFragment) getFragmentManager().findFragmentByTag(MainLoginFragment.TAG);

            finishAuthentication(event.getAuthentication().getToken(), fragment.getUsername(), fragment.getPassword());
        } else if (accountType.equals(AmahiAccount.TYPE_USER)) {
            String ip = Preferences.getLocalServerIP(this);
            Preferences.setServerName(this, ip);
            // TODO: set server session
            Preferences.setServerSession(this, event.getAuthentication().getToken());
            Preferences.setServerToken(this, event.getAuthentication().getToken());
            PINAccessFragment fragment = (PINAccessFragment) getFragmentManager().findFragmentByTag(PINAccessFragment.TAG);

            finishAuthentication(event.getAuthentication().getToken(), "Server", fragment.getPIN());
        }
    }

    private void finishAuthentication(String authenticationToken, String username, String password) {
        AccountManager accountManager = AccountManager.get(this);

        Bundle authenticationBundle = new Bundle();

        Account account = new AmahiAccount(username);
        if (accountType.equals(AmahiAccount.TYPE_USER)) {
            authenticationBundle.putString("ip", Preferences.getLocalServerIP(this));
            authenticationBundle.putString("is_local", "T");
        } else {
            authenticationBundle.putString("is_local", "F");
            authenticationBundle.putString(Intents.Extras.ACCOUNT_TYPE, getIntent().getStringExtra(Intents.Extras.ACCOUNT_TYPE));
        }

        if (accountManager.addAccountExplicitly(account, password, authenticationBundle)) {
            authenticationBundle.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
            authenticationBundle.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
            authenticationBundle.putString(AccountManager.KEY_AUTHTOKEN, authenticationToken);

            accountManager.setAuthToken(account, account.type, authenticationToken);
        }

        setAccountAuthenticatorResult(authenticationBundle);

        setResult(RESULT_OK);

        finish();
    }

    @Subscribe
    public void onServerAuthenticationComplete(ServerAuthenticationCompleteEvent event) {
        Preferences.setServerSession(this, getServer().getSession());
        Preferences.setServerName(this, getServer().getName());
        storeAuthToken(event.getAuthToken());
    }

    private Server getServer() {
        return getIntent().getParcelableExtra(Intents.Extras.SERVER_FILE);
    }

    private void storeAuthToken(String authToken) {
        Preferences.setServerToken(this, authToken);
        setResult(RESULT_OK);
        finish();
    }

    private List<Account> getAccounts() {
        return Arrays.asList(AccountManager.get(this).getAccountsByType(AmahiAccount.TYPE));
    }

    @Subscribe
    public void onPINAccess(PINAccessEvent event) {
        accountType = AmahiAccount.TYPE_USER;
        showPINAccessFragment();
    }

    private void showPINAccessFragment() {
        Fragment fragment = Fragments.Builder.buildPINFragment();
        getFragmentManager()
            .beginTransaction()
            .replace(R.id.main_container, fragment, PINAccessFragment.TAG)
            .addToBackStack(MainLoginFragment.TAG)
            .commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(State.ACCOUNT_TYPE, accountType);
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
    public void onBackPressed() {
        super.onBackPressed();
        if (accountType.equals(AmahiAccount.TYPE_USER)) {
            accountType = AmahiAccount.TYPE;
        }
        finishAffinity();
    }

    private static final class State {
        static final String ACCOUNT_TYPE = "account_type";

        private State() {
        }
    }
}
