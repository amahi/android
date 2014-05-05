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

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.bus.AuthenticationDoneEvent;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.ConnectionNotAuthorizedEvent;
import org.amahi.anywhere.bus.ConnectionTimeoutEvent;
import org.amahi.anywhere.bus.ServerConnectedEvent;
import org.amahi.anywhere.bus.ServerFilesLoadedEvent;
import org.amahi.anywhere.bus.ServerSharesLoadedEvent;
import org.amahi.anywhere.bus.ServersLoadedEvent;
import org.amahi.anywhere.server.client.AmahiClient;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.Authentication;
import org.amahi.anywhere.server.model.Server;
import org.amahi.anywhere.server.model.ServerShare;

import javax.inject.Inject;

public class ServersActivity extends Activity
{
	@Inject
	AmahiClient amahiClient;

	@Inject
	ServerClient serverClient;

	Server server;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setUpInjections();

		setUpAuthentication();
	}

	private void setUpInjections() {
		AmahiApplication.from(this).inject(this);
	}

	private void setUpAuthentication() {
		amahiClient.getAuthenticationToken("USER", "PASS");
	}

	@Subscribe
	public void onAuthenticationDone(AuthenticationDoneEvent event) {
		showMessage("Authorized.");

		setUpServers(event.getAuthentication());
	}

	private void showMessage(String message) {
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}

	private void setUpServers(Authentication authentication) {
		amahiClient.getServers(authentication.getToken());
	}

	@Subscribe
	public void onServersLoaded(ServersLoadedEvent event) {
		showMessage("Loaded: servers.");

		setUpServerConnection(event.getServers().get(0));
	}

	private void setUpServerConnection(Server server) {
		this.server = server;

		serverClient.connect(server);
	}

	@Subscribe
	public void onServerConnection(ServerConnectedEvent event) {
		showMessage("Connected.");

		setUpServerShares();
	}

	private void setUpServerShares() {
		serverClient.getShares();
	}

	@Subscribe
	public void onServerSharesLoaded(ServerSharesLoadedEvent event) {
		showMessage("Loaded: server’s shares.");

		setUpServerFiles(event.getServerShares().get(0));
	}

	private void setUpServerFiles(ServerShare serverShare) {
		serverClient.getFiles(serverShare, null);
	}

	@Subscribe
	public void onServerFilesLoaded(ServerFilesLoadedEvent event) {
		showMessage("Loaded: server’s files");
	}

	@Subscribe
	public void onConnectionNotAuthorized(ConnectionNotAuthorizedEvent event) {
		showMessage("Unauthorized.");
	}

	@Subscribe
	public void onConnectionTimeout(ConnectionTimeoutEvent event) {
		showMessage("Timeout.");
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
