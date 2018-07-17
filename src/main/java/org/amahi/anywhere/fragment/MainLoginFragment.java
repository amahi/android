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

import android.app.Fragment;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.dd.processbutton.iml.ActionProcessButton;
import com.squareup.otto.Subscribe;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.R;
import org.amahi.anywhere.bus.AuthenticationConnectionFailedEvent;
import org.amahi.anywhere.bus.AuthenticationFailedEvent;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.PINAccessEvent;
import org.amahi.anywhere.server.client.AmahiClient;
import org.amahi.anywhere.util.ViewDirector;

import javax.inject.Inject;

/**
 * A simple {@link Fragment} subclass.
 */

public class MainLoginFragment extends Fragment implements TextWatcher,
    View.OnClickListener {

    public static final String TAG = "MainLoginFragment";
    @Inject
    AmahiClient amahiClient;
    TextInputLayout username_layout, password_layout;
    ActionProcessButton signInButton;
    Button pinLoginButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_authentication, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setUpInjections();
        setUpLayout(view);
        setUpAuthentication(view);
    }

    private void setUpInjections() {
        AmahiApplication.from(getActivity()).inject(this);
    }

    private void setUpLayout(View view) {
        username_layout = view.findViewById(R.id.username_layout);
        password_layout = view.findViewById(R.id.password_layout);
        signInButton = view.findViewById(R.id.button_authentication);
        pinLoginButton = view.findViewById(R.id.button_pin_access);
    }

    private void setUpAuthentication(View view) {
        setUpAuthenticationMessages(view);
        setUpAuthenticationListeners();
    }

    private void setUpAuthenticationMessages(View view) {
        TextView authenticationFailureMessage = view.findViewById(R.id.text_message_authentication);
        TextView authenticationConnectionFailureMessage = view.findViewById(R.id.text_message_authentication_connection);

        authenticationFailureMessage.setMovementMethod(LinkMovementMethod.getInstance());
        authenticationConnectionFailureMessage.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void setUpAuthenticationListeners() {
        setUpAuthenticationTextListener();
        setUpPINAccessListener();
    }

    private void setUpAuthenticationTextListener() {
        getUsernameEditText().addTextChangedListener(this);
        getPasswordEditText().addTextChangedListener(this);
        getPasswordEditText().setOnEditorActionListener((v, actionId, event) -> {
            boolean handled = false;
            if (actionId == EditorInfo.IME_ACTION_GO) {
                signInButton.callOnClick();
                handled = true;
            }
            return handled;
        });
        signInButton.setOnClickListener(this);
    }

    private EditText getUsernameEditText() {
        return username_layout.getEditText();
    }

    private EditText getPasswordEditText() {
        return password_layout.getEditText();
    }

    @Override
    public void onClick(View view) {
        if (getUsername().trim().isEmpty() || getPassword().trim().isEmpty()) {
            ViewDirector.of(getActivity(), R.id.animator_message).show(R.id.text_message_authentication_empty);

            if (getUsername().trim().isEmpty())
                getUsernameEditText().getBackground().setColorFilter(ContextCompat.getColor(getActivity(), android.R.color.holo_red_light), PorterDuff.Mode.SRC_ATOP);
            if (getPassword().trim().isEmpty())
                getPasswordEditText().getBackground().setColorFilter(ContextCompat.getColor(getActivity(), android.R.color.holo_red_light), PorterDuff.Mode.SRC_ATOP);

            getUsernameEditText().addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if (!getUsername().trim().isEmpty())
                        getUsernameEditText().getBackground().setColorFilter(ContextCompat.getColor(getActivity(), R.color.blue_normal), PorterDuff.Mode.SRC_ATOP);
                    else
                        getUsernameEditText().getBackground().setColorFilter(ContextCompat.getColor(getActivity(), R.color.holo_red_light), PorterDuff.Mode.SRC_ATOP);
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });

            getPasswordEditText().addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if (!getPassword().trim().isEmpty())
                        getPasswordEditText().getBackground().setColorFilter(ContextCompat.getColor(getActivity(), R.color.blue_normal), PorterDuff.Mode.SRC_ATOP);
                    else
                        getPasswordEditText().getBackground().setColorFilter(ContextCompat.getColor(getActivity(), android.R.color.holo_red_light), PorterDuff.Mode.SRC_ATOP);
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

    public String getUsername() {
        return username_layout.getEditText().getText().toString();
    }

    public String getPassword() {
        return password_layout.getEditText().getText().toString();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    private void startAuthentication() {
        hideAuthenticationText();

        showProgress();

        hideAuthenticationFailureMessage();
    }

    private void hideAuthenticationFailureMessage() {
        ViewDirector.of(getActivity(), R.id.animator_message).show(R.id.view_message_empty);
    }

    private void hideAuthenticationText() {
        getUsernameEditText().setEnabled(false);
        getPasswordEditText().setEnabled(false);
    }

    private void showProgress() {
        signInButton.setMode(ActionProcessButton.Mode.ENDLESS);
        signInButton.setProgress(1);
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
        getUsernameEditText().setEnabled(true);
        getPasswordEditText().setEnabled(true);
    }

    private void hideProgress() {
        signInButton.setProgress(0);
    }

    private void showAuthenticationFailureMessage() {
        ViewDirector.of(getActivity(), R.id.animator_message).show(R.id.text_message_authentication);
    }

    @Subscribe
    public void onAuthenticationConnectionFailed(AuthenticationConnectionFailedEvent event) {
        finishAuthentication();

        showAuthenticationConnectionFailureMessage();
    }

    private void showAuthenticationConnectionFailureMessage() {
        ViewDirector.of(getActivity(), R.id.animator_message).show(R.id.text_message_authentication_connection);
    }

    private void setUpPINAccessListener() {
        pinLoginButton.setOnClickListener((v) -> {
            BusProvider.getBus().post(new PINAccessEvent());
        });
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
