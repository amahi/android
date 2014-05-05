package org.amahi.anywhere.server.client;

import com.squareup.otto.Subscribe;

import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.ServerConnectedEvent;
import org.amahi.anywhere.bus.ServerRouteLoadedEvent;
import org.amahi.anywhere.server.Api;
import org.amahi.anywhere.server.api.ProxyApi;
import org.amahi.anywhere.server.api.ServerApi;
import org.amahi.anywhere.server.header.ApiHeaders;
import org.amahi.anywhere.server.model.Server;
import org.amahi.anywhere.server.model.ServerRoute;
import org.amahi.anywhere.server.model.ServerShare;
import org.amahi.anywhere.server.response.ServerRouteResponse;
import org.amahi.anywhere.server.response.ServerFilesResponse;
import org.amahi.anywhere.server.response.ServerSharesResponse;

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

		startServerConnection();
	}

	private void startServerConnection() {
		BusProvider.getBus().register(this);

		proxyApi.getServerRoute(server.getSession(), new ServerRouteResponse());
	}

	@Subscribe
	public void onServerRouteLoaded(ServerRouteLoadedEvent event) {
		this.serverRoute = event.getServerRoute();
		this.serverApi = buildServerApi();

		finishServerConnection();
	}

	private void finishServerConnection() {
		BusProvider.getBus().unregister(this);

		BusProvider.getBus().post(new ServerConnectedEvent());
	}

	private ServerApi buildServerApi() {
		RestAdapter apiAdapter = new RestAdapter.Builder()
			.setEndpoint(serverRoute.getRemoteAddress())
			.setClient(client)
			.setRequestInterceptor(headers)
			.build();

		return apiAdapter.create(ServerApi.class);
	}

	public void getShares() {
		serverApi.getShares(server.getSession(), new ServerSharesResponse());
	}

	public void getFiles(ServerShare share, String path) {
		serverApi.getFiles(server.getSession(), share.getName(), path, new ServerFilesResponse());
	}
}
