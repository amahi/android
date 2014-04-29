package org.amahi.anywhere.server.client;

import com.squareup.okhttp.OkHttpClient;

import org.amahi.anywhere.server.api.ServerApi;
import org.amahi.anywhere.server.header.ApiHeaders;
import org.amahi.anywhere.server.model.Server;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerRoute;
import org.amahi.anywhere.server.model.ServerShare;

import java.util.List;

import retrofit.RestAdapter;
import retrofit.client.OkClient;

public class ServerClient
{
	private final ProxyClient proxyClient;

	private Server server;
	private ServerRoute serverRoute;

	private ServerApi api;

	public ServerClient() {
		this.proxyClient = new ProxyClient();
	}

	public void connect(Server server) {
		this.server = server;
		this.serverRoute = proxyClient.getRoute(server);

		this.api = buildApi();
	}

	private ServerApi buildApi() {
		RestAdapter apiAdapter = new RestAdapter.Builder()
			.setEndpoint(serverRoute.getRemoteAddress())
			.setClient(new OkClient(new OkHttpClient()))
			.setRequestInterceptor(new ApiHeaders())
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
