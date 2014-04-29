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
import org.amahi.anywhere.bus.ServersLoadedEvent;
import org.amahi.anywhere.server.client.AmahiClient;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.Authentication;

import javax.inject.Inject;

public class ServersActivity extends Activity
{
	@Inject
	AmahiClient amahiClient;

	@Inject
	ServerClient serverClient;

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
		showMessage("Loaded.");
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
