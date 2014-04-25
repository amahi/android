package org.amahi.anywhere.activity;

import android.app.Activity;
import android.os.Bundle;

import org.amahi.anywhere.server.client.AmahiClient;
import org.amahi.anywhere.server.client.Authenticator;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.Server;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerShare;

import java.util.List;

public class ServersActivity extends Activity implements Runnable
{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getFiles();
	}

	private void getFiles() {
		new Thread(this).start();
	}

	@Override
	public void run() {
		String authenticationToken = Authenticator.with(this).authenticate("USER", "PASS");

		List<Server> servers = AmahiClient.with(authenticationToken).getServers();
		Server server = servers.get(0);

		ServerClient serverClient = ServerClient.of(server);
		serverClient.connect();

		List<ServerShare> serverShares = serverClient.getShares();
		ServerShare serverShare = serverShares.get(0);

		List<ServerFile> serverFiles = serverClient.getFiles(serverShare, null);
	}
}
