package org.amahi.anywhere.activity;

import android.app.Activity;
import android.os.Bundle;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.server.client.AmahiClient;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.Server;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerShare;

import java.util.List;

import javax.inject.Inject;

public class ServersActivity extends Activity implements Runnable
{
	@Inject
	AmahiClient amahiClient;

	@Inject
	ServerClient serverClient;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setUpInjections();

		setUpFiles();
	}

	private void setUpInjections() {
		AmahiApplication.from(this).inject(this);
	}

	private void setUpFiles() {
		new Thread(this).start();
	}

	@Override
	public void run() {
		String authenticationToken = amahiClient.getAuthenticationToken("USER", "PASS");

		List<Server> servers = amahiClient.getServers(authenticationToken);
		Server server = servers.get(0);

		serverClient.connect(server);

		List<ServerShare> serverShares = serverClient.getShares();
		ServerShare serverShare = serverShares.get(0);

		List<ServerFile> serverFiles = serverClient.getFiles(serverShare, null);
	}
}
