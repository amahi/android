package org.amahi.anywhere.server.client;

import org.amahi.anywhere.server.api.ServerApi;
import org.amahi.anywhere.server.header.ApiHeaders;
import org.amahi.anywhere.server.model.Server;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerRoute;
import org.amahi.anywhere.server.model.ServerShare;

import java.util.List;

import javax.inject.Inject;

import retrofit.RestAdapter;
import retrofit.client.Client;

public class ServerClient
{
	private final ProxyClient proxyClient;

	private final Client client;
	private final ApiHeaders headers;

	private Server server;
	private ServerRoute serverRoute;

	private ServerApi api;

	@Inject
	public ServerClient(ProxyClient proxyClient, Client client, ApiHeaders headers) {
		this.proxyClient = proxyClient;

		this.client = client;
		this.headers = headers;
	}

	public void connect(Server server) {
		this.server = server;
		this.serverRoute = proxyClient.getRoute(server);

		this.api = buildApi();
	}

	private ServerApi buildApi() {
		RestAdapter apiAdapter = new RestAdapter.Builder()
			.setEndpoint(serverRoute.getRemoteAddress())
			.setClient(client)
			.setRequestInterceptor(headers)
			.build();

		return apiAdapter.create(ServerApi.class);
	}

	public List<ServerShare> getShares() {
		return api.getShares(server.getSession());
	}

	public List<ServerFile> getFiles(ServerShare share, String path) {
		return api.getFiles(server.getSession(), share.getName(), path);
	}
}
