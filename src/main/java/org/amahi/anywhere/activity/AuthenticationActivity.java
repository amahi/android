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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ViewAnimator;

import com.squareup.otto.Subscribe;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.R;
import org.amahi.anywhere.account.AmahiAccount;
import org.amahi.anywhere.bus.AuthenticationDoneEvent;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.server.client.AmahiClient;

import javax.inject.Inject;

public class AuthenticationActivity extends AccountAuthenticatorActivity implements View.OnClickListener
{
	@Inject
	AmahiClient amahiClient;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_authentication);

		setUpInjections();

		setUpAuthenticationListener();
	}

	private void setUpInjections() {
		AmahiApplication.from(this).inject(this);
	}

	private void setUpAuthenticationListener() {
		Button authenticationButton = (Button) findViewById(R.id.button_authentication);

		authenticationButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		setUpAuthentication();
	}

	private void setUpAuthentication() {
		if (getUsername().isEmpty()) {
			getUsernameEdit().setError("Shouldn’t be empty");
			return;
		}

		if (getPassword().isEmpty()) {
			getPasswordEdit().setError("Shouldn’t be empty");
			return;
		}

		showProgress();

		amahiClient.getAuthenticationToken(getUsername(), getPassword());
	}

	private String getUsername() {
		return getUsernameEdit().getText().toString();
	}

	private EditText getUsernameEdit() {
		return (EditText) findViewById(R.id.edit_username);
	}

	private String getPassword() {
		return getPasswordEdit().getText().toString();
	}

	private EditText getPasswordEdit() {
		return (EditText) findViewById(R.id.edit_password);
	}

	private void showProgress() {
		ViewAnimator animator = (ViewAnimator) findViewById(R.id.animator);
		animator.setDisplayedChild(animator.indexOfChild(findViewById(android.R.id.progress)));
	}

	@Subscribe
	public void onAuthenticationDone(AuthenticationDoneEvent event) {
		AccountManager accountManager = AccountManager.get(this);

		Bundle authenticationBundle = new Bundle();

		Account account = new AmahiAccount(getUsername());
		String authenticationToken = event.getAuthentication().getToken();

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
