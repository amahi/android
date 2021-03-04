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

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dd.processbutton.iml.ActionProcessButton;
import com.google.android.material.textfield.TextInputLayout;
import com.squareup.otto.Subscribe;

import org.amahi.anywhere.R;
import org.amahi.anywhere.account.AmahiAccount;
import org.amahi.anywhere.bus.AuthenticationConnectionFailedEvent;
import org.amahi.anywhere.bus.AuthenticationFailedEvent;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.task.LocalServerProbingTask;
import org.amahi.anywhere.util.ViewDirector;

import java.util.Objects;

public class PINAccessFragment extends Fragment {

    public static final String TAG = PINAccessFragment.class.getSimpleName();

    private TextInputLayout pinLayout;
    private ActionProcessButton pinLoginButton;
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pin_access, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        setUpPINAuthentication(view);
    }

    private void setUpPINAuthentication(View view) {
        setUpPINViews(view);
        setUpAuthenticationMessages(view);
        setUpAuthenticationListeners();
    }

    private void setUpPINViews(View view) {
        pinLayout = view.findViewById(R.id.pin_layout);
        pinLoginButton = view.findViewById(R.id.button_pin_sign_in);
    }

    private void setUpAuthenticationMessages(View view) {
        TextView pinWrongMessage = view.findViewById(R.id.text_message_pin);
        TextView connectionFailureMessage = view.findViewById(R.id.text_message_connection);
        TextView pinEmptyMessage = view.findViewById(R.id.text_message_pin_empty);

        pinWrongMessage.setMovementMethod(LinkMovementMethod.getInstance());
        connectionFailureMessage.setMovementMethod(LinkMovementMethod.getInstance());
        pinEmptyMessage.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private EditText getPINEditText() {
        return pinLayout.getEditText();
    }

    public String getPIN() {
        return getPINEditText().getText().toString();
    }

    private void setUpAuthenticationListeners() {
        getPINEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ViewDirector.of(getActivity(), R.id.animator_message).show(R.id.view_message_empty);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        getPINEditText().setOnEditorActionListener((v, actionId, event) -> pinLoginButton.callOnClick());

        pinLoginButton.setOnClickListener(v -> {
            if (getPIN().trim().isEmpty()) {
                ViewDirector.of(getActivity(), R.id.animator_message).show(R.id.text_message_pin_empty);
                return;
            } else if (getPIN().length() > 5 || getPIN().length() < 3) {
                ViewDirector.of(getActivity(), R.id.animator_message).show(R.id.text_message_pin);
                return;
            }
            showProgress();
            startAuthentication(getPIN());
        });
    }

    private void showProgress() {

        InputMethodManager imm = (InputMethodManager) Objects.requireNonNull(getContext()).getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        if(pinLoginButton!=null) {
            pinLoginButton.setMode(ActionProcessButton.Mode.ENDLESS);
            pinLoginButton.setProgress(1);
        }
    }

    private void hideProgress() {
        if(pinLoginButton!=null) {
            pinLoginButton.setProgress(0);
        }
    }

    private void startAuthentication(String pin) {
        AmahiAccount.accountType = AmahiAccount.TYPE_LOCAL;
        new LocalServerProbingTask(getActivity(), pin).execute();
    }

    @Subscribe
    public void onAuthenticationFailed(AuthenticationFailedEvent event) {
        hideProgress();
        enableUIControls();
        showAuthenticationFailMessage();
    }

    private void enableUIControls() {
        pinLayout.setEnabled(true);
        pinLoginButton.setEnabled(true);
    }

    private void showAuthenticationFailMessage() {
        ViewDirector.of(getActivity(), R.id.animator_message).show(R.id.text_message_pin);
    }

    @Subscribe
    public void onAuthenticationConnectionFail(AuthenticationConnectionFailedEvent event) {
        hideProgress();
        enableUIControls();
        showAuthenticationConnectionFailMessage();
    }

    private void showAuthenticationConnectionFailMessage() {
        ViewDirector.of(getActivity(), R.id.animator_message).show(R.id.text_message_connection);
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
