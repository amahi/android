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
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.squareup.otto.Subscribe;

import org.amahi.anywhere.R;
import org.amahi.anywhere.bus.AuthenticationConnectionFailedEvent;
import org.amahi.anywhere.bus.AuthenticationFailedEvent;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.task.LocalServerProbingTask;
import org.amahi.anywhere.util.ViewDirector;

public class PINAccessFragment extends Fragment {

    public static final String TAG = "PINAccessFragment";

    private EditText pinEditText;
    private Button pinLoginButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pin_access, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpPINAuthentication(view);
    }

    private void setUpPINAuthentication(View view) {
        setUpPINViews(view);
        setUpAuthenticationMessages(view);
        setUpAuthenticationListeners();
    }

    private void setUpPINViews(View view) {
        pinEditText = view.findViewById(R.id.edit_text_pin);
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

    private void setUpAuthenticationListeners() {
        pinEditText.addTextChangedListener(new TextWatcher() {
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

        pinEditText.setOnEditorActionListener((v, actionId, event) -> pinLoginButton.callOnClick());

        pinLoginButton.setOnClickListener(v -> {
            String pin = pinEditText.getText().toString();
            if (TextUtils.isEmpty(pin)) {
                ViewDirector.of(getActivity(), R.id.animator_message).show(R.id.text_message_pin_empty);
                return;
            }
            showProgress();
            startAuthentication(pin);
        });
    }

    private void showProgress() {
        LocalLoginDialogFragment fragment = (LocalLoginDialogFragment) getChildFragmentManager().findFragmentByTag("log_in");
        if (fragment == null) {
            fragment = new LocalLoginDialogFragment();
            fragment.show(getChildFragmentManager(), "log_in");
        }
    }

    private void startAuthentication(String pin) {
        new LocalServerProbingTask(getActivity(), pin).execute();
    }

    @Subscribe
    public void onAuthenticationFailed(AuthenticationFailedEvent event) {
        dismissDialog();
        enableUIControls();
        showAuthenticationFailMessage();
    }

    private void dismissDialog() {
        LocalLoginDialogFragment fragment = (LocalLoginDialogFragment) getChildFragmentManager().findFragmentByTag("log_in");
        if (fragment != null) {
            fragment.dismiss();
        }
    }

    private void enableUIControls() {
        pinEditText.setEnabled(true);
        pinLoginButton.setEnabled(true);
    }

    private void showAuthenticationFailMessage() {
        ViewDirector.of(getActivity(), R.id.animator_message).show(R.id.text_message_pin);
    }

    @Subscribe
    public void onAuthenticationConnectionFail(AuthenticationConnectionFailedEvent event) {
        dismissDialog();
        enableUIControls();
        showAuthenticationConnectionFailMessage();
    }

    private void showAuthenticationConnectionFailMessage() {
        ViewDirector.of(getActivity(), R.id.animator_message).show(R.id.text_message_connection);
    }

    public String getPIN() {
        return pinEditText.getText().toString();
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
