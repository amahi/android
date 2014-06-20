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
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ViewAnimator;

import com.squareup.otto.Subscribe;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.R;
import org.amahi.anywhere.account.AmahiAccount;
import org.amahi.anywhere.bus.AuthenticationConnectionFailedEvent;
import org.amahi.anywhere.bus.AuthenticationFailedEvent;
import org.amahi.anywhere.bus.AuthenticationSucceedEvent;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.server.client.AmahiClient;

import javax.inject.Inject;

public class AuthenticationActivity extends AccountAuthenticatorActivity implements TextWatcher, View.OnClickListener
{
	@Inject
	AmahiClient amahiClient;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_authentication);

		setUpInjections();

		setUpAuthenticationAction();
		setUpAuthenticationMessages();
		setUpAuthenticationListeners();
	}

	private void setUpInjections() {
		AmahiApplication.from(this).inject(this);
	}

	private void setUpAuthenticationAction() {
		if (getUsername().isEmpty() || getPassword().isEmpty()) {
			getAuthenticationButton().setEnabled(false);
		} else {
			getAuthenticationButton().setEnabled(true);
		}
	}

	private String getUsername() {
		return getUsernameEdit().getText().toString();
	}

	private String getPassword() {
		return getPasswordEdit().getText().toString();
	}

	private Button getAuthenticationButton() {
		return (Button) findViewById(R.id.button_authentication);
	}

	private void setUpAuthenticationMessages() {
		TextView authenticationFailureMessage = (TextView) findViewById(R.id.text_message_authentication);
		TextView authenticationConnectionFailureMessage = (TextView) findViewById(R.id.text_message_authentication_connection);

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
	}

	private EditText getUsernameEdit() {
		return (EditText) findViewById(R.id.edit_username);
	}

	private EditText getPasswordEdit() {
		return (EditText) findViewById(R.id.edit_password);
	}

	@Override
	public void onTextChanged(CharSequence text, int after, int before, int count) {
		setUpAuthenticationAction();

		hideAuthenticationFailureMessage();
	}

	private void hideAuthenticationFailureMessage() {
		ViewAnimator animator = (ViewAnimator) findViewById(R.id.animator_message);
		animator.setDisplayedChild(animator.indexOfChild(findViewById(R.id.view_message_empty)));
	}

	@Override
	public void afterTextChanged(Editable text) {
	}

	@Override
	public void beforeTextChanged(CharSequence text, int start, int count, int before) {
	}

	private void setUpAuthenticationActionListener() {
		getAuthenticationButton().setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		setUpAuthentication();

		startAuthentication();
	}

	private void setUpAuthentication() {
		hideAuthenticationText();

		showProgress();

		hideAuthenticationFailureMessage();
	}

	private void hideAuthenticationText() {
		getUsernameEdit().setEnabled(false);
		getPasswordEdit().setEnabled(false);
	}

	private void showProgress() {
		ViewAnimator animator = (ViewAnimator) findViewById(R.id.animator_button);
		animator.setDisplayedChild(animator.indexOfChild(findViewById(android.R.id.progress)));
	}

	private void startAuthentication() {
		amahiClient.authenticate(getUsername(), getPassword());
	}

	@Subscribe
	public void onAuthenticationFailed(AuthenticationFailedEvent event) {
		tearDownAuthentication();

		showAuthenticationFailureMessage();
	}

	private void tearDownAuthentication() {
		showAuthenticationText();

		hideProgress();
	}

	private void showAuthenticationText() {
		getUsernameEdit().setEnabled(true);
		getPasswordEdit().setEnabled(true);
	}

	private void hideProgress() {
		ViewAnimator animator = (ViewAnimator) findViewById(R.id.animator_button);
		animator.setDisplayedChild(animator.indexOfChild(findViewById(R.id.button_authentication)));
	}

	private void showAuthenticationFailureMessage() {
		ViewAnimator animator = (ViewAnimator) findViewById(R.id.animator_message);
		animator.setDisplayedChild(animator.indexOfChild(findViewById(R.id.text_message_authentication)));
	}

	@Subscribe
	public void onAuthenticationConnectionFailed(AuthenticationConnectionFailedEvent event) {
		tearDownAuthentication();

		showAuthenticationConnectionFailureMessage();
	}

	private void showAuthenticationConnectionFailureMessage() {
		ViewAnimator animator = (ViewAnimator) findViewById(R.id.animator_message);
		animator.setDisplayedChild(animator.indexOfChild(findViewById(R.id.text_message_authentication_connection)));
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
}
