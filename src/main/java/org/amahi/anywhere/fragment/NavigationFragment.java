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
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OnAccountsUpdateListener;
import android.accounts.OperationCanceledException;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.ViewAnimator;

import com.squareup.otto.Subscribe;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.R;
import org.amahi.anywhere.account.AmahiAccount;
import org.amahi.anywhere.adapter.ServerSharesAdapter;
import org.amahi.anywhere.adapter.ServersAdapter;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.ServerConnectedEvent;
import org.amahi.anywhere.bus.ServerSharesLoadFailedEvent;
import org.amahi.anywhere.bus.ServerSharesLoadedEvent;
import org.amahi.anywhere.bus.ServersLoadFailedEvent;
import org.amahi.anywhere.bus.ServersLoadedEvent;
import org.amahi.anywhere.bus.SettingsSelectedEvent;
import org.amahi.anywhere.bus.ShareSelectedEvent;
import org.amahi.anywhere.server.client.AmahiClient;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.Server;
import org.amahi.anywhere.server.model.ServerShare;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

public class NavigationFragment extends Fragment implements AccountManagerCallback<Bundle>,
	AdapterView.OnItemSelectedListener,
	AdapterView.OnItemClickListener,
	OnAccountsUpdateListener
{
	@Inject
	AmahiClient amahiClient;

	@Inject
	ServerClient serverClient;

	@Override
	public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
		return layoutInflater.inflate(R.layout.fragment_navigation, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		setUpInjections();

		setUpSettingsMenu();

		setUpAuthentication();
		setUpAuthenticationListener();
	}

	private void setUpInjections() {
		AmahiApplication.from(getActivity()).inject(this);
	}

	private void setUpSettingsMenu() {
		setHasOptionsMenu(true);
	}

	private void setUpAuthentication() {
		if (getAccounts().isEmpty()) {
			setUpAccount();
		} else {
			setUpAuthenticationToken();
		}
	}

	private void setUpAuthenticationListener() {
		getAccountManager().addOnAccountsUpdatedListener(this, null, false);
	}

	@Override
	public void onAccountsUpdated(Account[] accounts) {
		if (getAccounts().isEmpty()) {
			setUpAccount();
		}
	}

	private List<Account> getAccounts() {
		return Arrays.asList(getAccountManager().getAccountsByType(AmahiAccount.TYPE_ACCOUNT));
	}

	private AccountManager getAccountManager() {
		return AccountManager.get(getActivity());
	}

	private void setUpAccount() {
		getAccountManager().addAccount(AmahiAccount.TYPE_ACCOUNT, AmahiAccount.TYPE_TOKEN, null, null, getActivity(), this, null);
	}

	private void setUpAuthenticationToken() {
		Account account = getAccounts().get(0);

		getAccountManager().getAuthToken(account, AmahiAccount.TYPE_ACCOUNT, null, getActivity(), this, null);
	}

	@Override
	public void run(AccountManagerFuture<Bundle> accountManagerFuture) {
		try {
			Bundle accountManagerResult = accountManagerFuture.getResult();

			String authenticationToken = accountManagerResult.getString(AccountManager.KEY_AUTHTOKEN);

			if (authenticationToken != null) {
				setUpServers(authenticationToken);
			} else {
				setUpAuthenticationToken();
			}
		} catch (OperationCanceledException e) {
			tearDownActivity();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (AuthenticatorException e) {
			throw new RuntimeException(e);
		}
	}

	private void tearDownActivity() {
		getActivity().finish();
	}

	private void setUpServers(String authenticationToken) {
		setUpServersAdapter();
		setUpServersContent(authenticationToken);
		setUpServersListener();
	}

	private void setUpServersAdapter() {
		getServersSpinner().setAdapter(new ServersAdapter(getActivity()));
	}

	private Spinner getServersSpinner() {
		return (Spinner) getView().findViewById(R.id.spinner_servers);
	}

	private void setUpServersContent(String authenticationToken) {
		amahiClient.getServers(authenticationToken);
	}

	@Subscribe
	public void onServersLoaded(ServersLoadedEvent event) {
		setUpServersContent(event.getServers());

		showContent();
	}

	private void setUpServersContent(List<Server> servers) {
		getServersAdapter().replaceWith(filterActiveServers(servers));
	}

	private ServersAdapter getServersAdapter() {
		return (ServersAdapter) getServersSpinner().getAdapter();
	}

	private List<Server> filterActiveServers(List<Server> servers) {
		List<Server> activeServers = new ArrayList<Server>();

		for (Server server : servers) {
			if (server.isActive()) {
				activeServers.add(server);
			}
		}

		return activeServers;
	}

	private void showContent() {
		ViewAnimator animator = (ViewAnimator) getView().findViewById(R.id.animator_content);
		animator.setDisplayedChild(animator.indexOfChild(getView().findViewById(R.id.layout_content)));
	}

	@Subscribe
	public void onServersLoadFailed(ServersLoadFailedEvent event) {
		showError();
	}

	private void showError() {
		ViewAnimator animator = (ViewAnimator) getView().findViewById(R.id.animator_content);
		animator.setDisplayedChild(animator.indexOfChild(getView().findViewById(R.id.layout_error)));
	}

	private void setUpServersListener() {
		getServersSpinner().setOnItemSelectedListener(this);
	}

	@Override
	public void onNothingSelected(AdapterView<?> spinnerView) {
	}

	@Override
	public void onItemSelected(AdapterView<?> spinnerView, View view, int position, long id) {
		Server server = getServersAdapter().getItem(position);

		setUpShares(server);
	}

	private void setUpShares(Server server) {
		setUpSharesAdapter();
		setUpSharesListener();

		setUpServerConnection(server);
	}

	private void setUpSharesAdapter() {
		getSharesList().setAdapter(new ServerSharesAdapter(getActivity()));
	}

	private ListView getSharesList() {
		return (ListView) getView().findViewById(R.id.list_shares);
	}

	private void setUpSharesListener() {
		getSharesList().setOnItemClickListener(this);
	}

	private void setUpServerConnection(Server server) {
		if (serverClient.isConnected(server)) {
			setUpServerConnection();
			setUpServerConnectionIndicator();
		} else {
			serverClient.connect(server);
		}
	}

	@Subscribe
	public void onServerConnected(ServerConnectedEvent event) {
		setUpServerConnection();
		setUpServerConnectionIndicator();
	}

	private void setUpServerConnection() {
		if (isConnectionLocal()) {
			serverClient.connectLocal();
		} else {
			serverClient.connectRemote();
		}

		setUpSharesContent();
	}

	private void setUpServerConnectionIndicator() {
		getActivity().getActionBar().setBackgroundDrawable(getServerConnectionIndicator());
	}

	private Drawable getServerConnectionIndicator() {
		if (isConnectionLocal()) {
			return getResources().getDrawable(R.drawable.bg_action_bar);
		} else {
			return getResources().getDrawable(R.drawable.bg_action_bar_warning);
		}
	}

	private boolean isConnectionLocal() {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
		String preferenceConnection = preferences.getString(getString(R.string.preference_key_server_connection), null);

		return preferenceConnection.equals(getString(R.string.preference_key_server_connection_local));
	}

	private void setUpSharesContent() {
		serverClient.getShares();
	}

	@Subscribe
	public void onSharesLoaded(ServerSharesLoadedEvent event) {
		setUpSharesContent(event.getServerShares());

		showSharesContent();
	}

	private void setUpSharesContent(List<ServerShare> shares) {
		getSharesAdapter().replaceWith(shares);
	}

	private ServerSharesAdapter getSharesAdapter() {
		return (ServerSharesAdapter) getSharesList().getAdapter();
	}

	private void showSharesContent() {
		ViewAnimator animator = (ViewAnimator) getView().findViewById(R.id.animator_shares);

		View content = getView().findViewById(R.id.list_shares);

		if (animator.getDisplayedChild() != animator.indexOfChild(content)) {
			animator.setDisplayedChild(animator.indexOfChild(content));
		}
	}

	@Subscribe
	public void onSharesLoadFailed(ServerSharesLoadFailedEvent event) {
		showSharesError();
	}

	private void showSharesError() {
		ViewAnimator animator = (ViewAnimator) getView().findViewById(R.id.animator_shares);
		animator.setDisplayedChild(animator.indexOfChild(getView().findViewById(R.id.layout_shares_error)));
	}

	@Override
	public void onItemClick(AdapterView<?> listView, View view, int position, long id) {
		getSharesList().setItemChecked(position, true);

		ServerShare share = getSharesAdapter().getItem(position);

		BusProvider.getBus().post(new ShareSelectedEvent(share));
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
		super.onCreateOptionsMenu(menu, menuInflater);

		menuInflater.inflate(R.menu.action_bar_navigation, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem) {
		switch (menuItem.getItemId()) {
			case R.id.menu_settings:
				BusProvider.getBus().post(new SettingsSelectedEvent());
				return true;

			default:
				return super.onOptionsItemSelected(menuItem);
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		setUpServerConnectionIndicator();

		BusProvider.getBus().register(this);
	}

	@Override
	public void onPause() {
		super.onPause();

		BusProvider.getBus().unregister(this);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		tearDownAuthenticationListener();
	}

	private void tearDownAuthenticationListener() {
		getAccountManager().removeOnAccountsUpdatedListener(this);
	}
}
