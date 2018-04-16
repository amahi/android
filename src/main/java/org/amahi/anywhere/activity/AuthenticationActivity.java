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
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.app.Activity;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.dd.processbutton.iml.ActionProcessButton;
import com.squareup.otto.Subscribe;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.R;
import org.amahi.anywhere.account.AmahiAccount;
import org.amahi.anywhere.bus.AuthenticationConnectionFailedEvent;
import org.amahi.anywhere.bus.AuthenticationFailedEvent;
import org.amahi.anywhere.bus.AuthenticationSucceedEvent;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.server.client.AmahiClient;
import org.amahi.anywhere.util.ViewDirector;

import javax.inject.Inject;

/**
 * Authentication activity. Allows user authentication. If operation succeed
 * the authentication token is saved at the {@link android.accounts.AccountManager}.
 */
public class AuthenticationActivity extends AccountAuthenticatorActivity implements TextWatcher {
    @Inject
    AmahiClient amahiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        setUpInjections();

        setUpAuthentication();
    }

    private void setUpInjections() {
        AmahiApplication.from(this).inject(this);
    }

    private void setUpAuthentication() {
        setUpAuthenticationMessages();
        setUpAuthenticationListeners();
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

    private ActionProcessButton getAuthenticationButton() {
        return findViewById(R.id.button_authentication);
    }

    private void setUpAuthenticationMessages() {
        TextView authenticationFailureMessage = findViewById(R.id.text_message_authentication);
        TextView authenticationConnectionFailureMessage = findViewById(R.id.text_message_authentication_connection);

        authenticationFailureMessage.setMovementMethod(LinkMovementMethod.getInstance());
        authenticationConnectionFailureMessage.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void setUpAuthenticationListeners() {
        setUpAuthenticationTextListener();
        setUpAuthenticationActionListener();
    }

    private void setUpAuthenticationTextListener() {
        getUsernameEdit().addTextChangedListener(this);
        getPasswordEdit().addTextChangedListener(this);
        getPasswordEdit().setOnEditorActionListener((v, actionId, event) -> {
            boolean handled = false;
            if (actionId == EditorInfo.IME_ACTION_GO) {
                onClick(getAuthenticationButton());
                handled = true;
            }
            return handled;
        });
    }

    @Override
    public void onTextChanged(CharSequence text, int after, int before, int count) {
        hideAuthenticationFailureMessage();
    }

    private void hideAuthenticationFailureMessage() {
        ViewDirector.of(this, R.id.animator_message).show(R.id.view_message_empty);
    }

    @Override
    public void afterTextChanged(Editable text) {
    }

    @Override
    public void beforeTextChanged(CharSequence text, int start, int count, int before) {
    }

    private void setUpAuthenticationActionListener() {
        getAuthenticationButton().setOnClickListener(this::onClick);
    }

    public void onClick(View view) {
        if (getUsername().trim().isEmpty() || getPassword().trim().isEmpty()) {
            ViewDirector.of(this, R.id.animator_message).show(R.id.text_message_authentication_empty);

            if (getUsername().trim().isEmpty())
                getUsernameEdit().getBackground().setColorFilter(ContextCompat.getColor(AuthenticationActivity.this, android.R.color.holo_red_light), PorterDuff.Mode.SRC_ATOP);
            if (getPassword().trim().isEmpty())
                getPasswordEdit().getBackground().setColorFilter(ContextCompat.getColor(AuthenticationActivity.this, android.R.color.holo_red_light), PorterDuff.Mode.SRC_ATOP);

            getUsernameEdit().addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if (!getUsername().trim().isEmpty())
                        getUsernameEdit().getBackground().setColorFilter(ContextCompat.getColor(AuthenticationActivity.this, R.color.blue_normal), PorterDuff.Mode.SRC_ATOP);
                    else
                        getUsernameEdit().getBackground().setColorFilter(ContextCompat.getColor(AuthenticationActivity.this, R.color.holo_red_light), PorterDuff.Mode.SRC_ATOP);
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });

            getPasswordEdit().addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if (!getPassword().trim().isEmpty())
                        getPasswordEdit().getBackground().setColorFilter(ContextCompat.getColor(AuthenticationActivity.this, R.color.blue_normal), PorterDuff.Mode.SRC_ATOP);
                    else
                        getPasswordEdit().getBackground().setColorFilter(ContextCompat.getColor(AuthenticationActivity.this, android.R.color.holo_red_light), PorterDuff.Mode.SRC_ATOP);
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });

        } else {
            startAuthentication();

            authenticate();
        }
    }

    private void startAuthentication() {
        hideAuthenticationText();

        showProgress();

        hideAuthenticationFailureMessage();
    }

    private void hideAuthenticationText() {
        getUsernameEdit().setEnabled(false);
        getPasswordEdit().setEnabled(false);
    }

    private void showProgress() {
        ActionProcessButton authenticationButton = getAuthenticationButton();

        authenticationButton.setMode(ActionProcessButton.Mode.ENDLESS);
        authenticationButton.setProgress(1);
    }

    private void authenticate() {
        amahiClient.authenticate(getUsername(), getPassword());
    }

    @Subscribe
    public void onAuthenticationFailed(AuthenticationFailedEvent event) {
        finishAuthentication();

        showAuthenticationFailureMessage();
    }

    private void finishAuthentication() {
        showAuthenticationText();

        hideProgress();
    }

    private void showAuthenticationText() {
        getUsernameEdit().setEnabled(true);
        getPasswordEdit().setEnabled(true);
    }

    private void hideProgress() {
        getAuthenticationButton().setProgress(0);
    }

    private void showAuthenticationFailureMessage() {
        ViewDirector.of(this, R.id.animator_message).show(R.id.text_message_authentication);
    }

    @Subscribe
    public void onAuthenticationConnectionFailed(AuthenticationConnectionFailedEvent event) {
        finishAuthentication();

        showAuthenticationConnectionFailureMessage();
    }

    private void showAuthenticationConnectionFailureMessage() {
        ViewDirector.of(this, R.id.animator_message).show(R.id.text_message_authentication_connection);
    }

    @Subscribe
    public void onAuthenticationSucceed(AuthenticationSucceedEvent event) {
        finishAuthentication(event.getAuthentication().getToken());
    }

    private void finishAuthentication(String authenticationToken) {
        AccountManager accountManager = AccountManager.get(this);

        Bundle authenticationBundle = new Bundle();

        Account account = new AmahiAccount(getUsername());

        if (accountManager.addAccountExplicitly(account, getPassword(), null)) {
            authenticationBundle.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
            authenticationBundle.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
            authenticationBundle.putString(AccountManager.KEY_AUTHTOKEN, authenticationToken);

            accountManager.setAuthToken(account, account.type, authenticationToken);
        }

        setAccountAuthenticatorResult(authenticationBundle);

        setResult(Activity.RESULT_OK);

        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();

        BusProvider.getBus().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        BusProvider.getBus().unregister(this);
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }
}
