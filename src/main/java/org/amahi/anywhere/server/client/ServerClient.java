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

package org.amahi.anywhere.server.client;

import android.net.Uri;

import com.squareup.otto.Subscribe;

import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.ServerConnectedEvent;
import org.amahi.anywhere.bus.ServerRouteLoadedEvent;
import org.amahi.anywhere.server.Api;
import org.amahi.anywhere.server.ApiAdapter;
import org.amahi.anywhere.server.api.ProxyApi;
import org.amahi.anywhere.server.api.ServerApi;
import org.amahi.anywhere.server.model.Server;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerRoute;
import org.amahi.anywhere.server.model.ServerShare;
import org.amahi.anywhere.server.response.ServerFilesResponse;
import org.amahi.anywhere.server.response.ServerRouteResponse;
import org.amahi.anywhere.server.response.ServerSharesResponse;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ServerClient
{
	private final ApiAdapter apiAdapter;

	private final ProxyApi proxyApi;
	private ServerApi serverApi;

	private Server server;
	private ServerRoute serverRoute;
	private String serverAddress;

	@Inject
	public ServerClient(ApiAdapter apiAdapter) {
		this.apiAdapter = apiAdapter;

		this.proxyApi = buildProxyApi();
	}

	private ProxyApi buildProxyApi() {
		return apiAdapter.create(ProxyApi.class, Api.getProxyUrl());
	}

	public boolean isConnected(Server server) {
		return (this.server != null) && (this.server.getSession().equals(server.getSession()));
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

		finishServerConnection();
	}

	private void finishServerConnection() {
		BusProvider.getBus().unregister(this);

		BusProvider.getBus().post(new ServerConnectedEvent());
	}

	public void connectLocal() {
		this.serverAddress = serverRoute.getLocalAddress();
		this.serverApi = buildServerApi(serverAddress);
	}

	private ServerApi buildServerApi(String serverAddress) {
		return apiAdapter.create(ServerApi.class, serverAddress);
	}

	public void connectRemote() {
		this.serverAddress = serverRoute.getRemoteAddress();
		this.serverApi = buildServerApi(serverAddress);
	}

	public void getShares() {
		serverApi.getShares(server.getSession(), new ServerSharesResponse());
	}

	public void getFiles(ServerShare share) {
		serverApi.getFiles(server.getSession(), share.getName(), null, new ServerFilesResponse(null));
	}

	public void getFiles(ServerShare share, ServerFile directory) {
		serverApi.getFiles(server.getSession(), share.getName(), directory.getPath(), new ServerFilesResponse(directory));
	}

	public Uri getFileUri(ServerShare share, ServerFile file) {
		return Uri.parse(serverAddress)
			.buildUpon()
			.path("files")
			.appendQueryParameter("s", share.getName())
			.appendQueryParameter("p", file.getPath())
			.appendQueryParameter("Session", server.getSession())
			.build();
	}
}
