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
import android.app.Fragment;
import android.os.Bundle;
import androidx.annotation.Nullable;
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

import com.squareup.otto.Subscribe;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.R;
import org.amahi.anywhere.account.AmahiAccount;
import org.amahi.anywhere.bus.AuthenticationConnectionFailedEvent;
import org.amahi.anywhere.bus.AuthenticationFailedEvent;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.DialogButtonClickedEvent;
import org.amahi.anywhere.bus.ProbingProgressEvent;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.HdaAuthBody;
import org.amahi.anywhere.task.LocalServerProbingTask;
import org.amahi.anywhere.util.ViewDirector;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

public class PINAccessFragment extends Fragment {

    public static final String TAG = "PINAccessFragment";

    private ProgressDialogFragment progressDialog;

    @Inject
    public ServerClient serverClient;

    private EditText pinEditText;
    private Button pinLoginButton;
    private LocalServerProbingTask probingTask;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setRetainInstance(true);
        return inflater.inflate(R.layout.fragment_pin_access, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpInjections();
        setUpPINAuthentication(view);
        setUpSignInDialog();
    }

    private void setUpInjections() {
        AmahiApplication.from(getActivity()).inject(this);
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
        progressDialog = (ProgressDialogFragment) getChildFragmentManager()
            .findFragmentByTag(ProgressDialogFragment.SIGN_IN_DIALOG_TAG);
        if (progressDialog == null) {
            progressDialog = new ProgressDialogFragment();
            progressDialog.show(getChildFragmentManager(), ProgressDialogFragment.SIGN_IN_DIALOG_TAG);
        }
    }

    private void startAuthentication(String pin) {
        if (getAccounts().isEmpty()) {
            // secondary user access
            probingTask = new LocalServerProbingTask(getActivity(), pin);
            probingTask.execute();
        } else {
            // admin user access
            HdaAuthBody authBody = new HdaAuthBody(pin);
            serverClient.authenticateHdaUser(authBody);
        }
    }

    private AccountManager getAccountManager() {
        return AccountManager.get(getActivity());
    }

    private List<Account> getAccounts() {
        return Arrays.asList(getAccountManager().getAccountsByType(AmahiAccount.TYPE));
    }

    private void setUpSignInDialog() {
        progressDialog = (ProgressDialogFragment) getChildFragmentManager()
            .findFragmentByTag(ProgressDialogFragment.SIGN_IN_DIALOG_TAG);
    }

    @Subscribe
    public void onAuthenticationFailed(AuthenticationFailedEvent event) {
        dismissDialog();
        enableUIControls();
        showAuthenticationFailMessage();
    }

    private void dismissDialog() {
        if (progressDialog != null && progressDialog.isAdded()) {
            progressDialog.dismiss();
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
        if (getAccounts().isEmpty()) {
            ViewDirector.of(getActivity(), R.id.animator_message).show(R.id.text_message_local_connection);
        } else {
            ViewDirector.of(getActivity(), R.id.animator_message).show(R.id.text_message_connection);
        }
    }

    public String getPIN() {
        return pinEditText.getText().toString();
    }

    @Subscribe
    public void onProbingProgress(ProbingProgressEvent event) {
        updateSignInDialog(event.getProgress());
    }

    private void updateSignInDialog(int progress) {
        if (progressDialog != null && progressDialog.isAdded()) {
            progressDialog.setProgress(progress);
        }
    }

    @Subscribe
    public void onDialogButtonClicked(DialogButtonClickedEvent event) {
        if (event.getButtonId() == R.id.text_view_cancel) {
            if (probingTask != null) {
                probingTask.cancel(true);
            }
            dismissDialog();
        }
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
