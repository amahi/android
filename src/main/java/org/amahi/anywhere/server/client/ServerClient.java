package org.amahi.anywhere.server.client;

import com.squareup.okhttp.OkHttpClient;

import org.amahi.anywhere.server.Api;
import org.amahi.anywhere.server.api.ProxyApi;
import org.amahi.anywhere.server.api.ServerApi;
import org.amahi.anywhere.server.header.SessionHeaders;
import org.amahi.anywhere.server.model.Server;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerRoute;
import org.amahi.anywhere.server.model.ServerShare;

import java.util.List;

import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;

public class ServerClient
{
	private final Server server;
	private ServerRoute serverRoute;

	private final ProxyApi proxyApi;
	private ServerApi serverApi;

	public static ServerClient of(Server server) {
		return new ServerClient(server);
	}

	private ServerClient(Server server) {
		this.server = server;

		this.proxyApi = buildProxyApi();
	}

	private ProxyApi buildProxyApi() {
		RestAdapter apiAdapter = new RestAdapter.Builder()
			.setEndpoint(Api.getProxyUrl())
			.setClient(new OkClient(new OkHttpClient()))
			.setRequestInterceptor(new SessionHeaders(server.getSession()))
			.build();

		return apiAdapter.create(ProxyApi.class);
	}

	public void connect() {
		this.serverRoute = proxyApi.getServerRoute();

		this.serverApi = buildServerApi(serverRoute.getRemoteAddress());
	}

	private ServerApi buildServerApi(String serverAddress) {
		RestAdapter apiAdapter = new RestAdapter.Builder()
			.setEndpoint(serverAddress)
			.setClient(new OkClient(new OkHttpClient()))
			.setRequestInterceptor(new SessionHeaders(server.getSession()))
			.build();

		return apiAdapter.create(ServerApi.class);
	}

	public List<ServerShare> getShares() {
		try {
			return serverApi.getShares();
		} catch (RetrofitError e) {
			throw new RuntimeException(e);
		}
	}

	public List<ServerFile> getFiles(ServerShare share, String path) {
		try {
			return serverApi.getFiles(share.getName(), path);
		} catch (RetrofitError e) {
			throw new RuntimeException(e);
		}
	}
}
