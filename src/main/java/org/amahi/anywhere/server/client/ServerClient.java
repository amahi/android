package org.amahi.anywhere.server.client;

import org.amahi.anywhere.server.Api;
import org.amahi.anywhere.server.api.ProxyApi;
import org.amahi.anywhere.server.api.ServerApi;
import org.amahi.anywhere.server.header.ApiHeaders;
import org.amahi.anywhere.server.model.Server;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerRoute;
import org.amahi.anywhere.server.model.ServerShare;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit.RestAdapter;
import retrofit.client.Client;

@Singleton
public class ServerClient
{
	private final Client client;
	private final ApiHeaders headers;

	private final ProxyApi proxyApi;
	private ServerApi serverApi;

	private Server server;
	private ServerRoute serverRoute;

	@Inject
	public ServerClient(Client client, ApiHeaders headers) {
		this.client = client;
		this.headers = headers;

		this.proxyApi = buildProxyApi();
	}

	private ProxyApi buildProxyApi() {
		RestAdapter apiAdapter = new RestAdapter.Builder()
			.setEndpoint(Api.getProxyUrl())
			.setClient(client)
			.setRequestInterceptor(headers)
			.build();

		return apiAdapter.create(ProxyApi.class);
	}

	public void connect(Server server) {
		this.server = server;
		this.serverRoute = proxyApi.getServerRoute(server.getSession());

		this.serverApi = buildServerApi();
	}

	private ServerApi buildServerApi() {
		RestAdapter apiAdapter = new RestAdapter.Builder()
			.setEndpoint(serverRoute.getRemoteAddress())
			.setClient(client)
			.setRequestInterceptor(headers)
			.build();

		return apiAdapter.create(ServerApi.class);
	}

	public List<ServerShare> getShares() {
		return serverApi.getShares(server.getSession());
	}

	public List<ServerFile> getFiles(ServerShare share, String path) {
		return serverApi.getFiles(server.getSession(), share.getName(), path);
	}
}
