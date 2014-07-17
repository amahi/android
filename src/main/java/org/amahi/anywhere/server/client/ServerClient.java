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
import org.amahi.anywhere.bus.NetworkChangedEvent;
import org.amahi.anywhere.bus.ServerConnectedEvent;
import org.amahi.anywhere.bus.ServerConnectionChangedEvent;
import org.amahi.anywhere.bus.ServerConnectionDetectedEvent;
import org.amahi.anywhere.bus.ServerRouteLoadedEvent;
import org.amahi.anywhere.server.Api;
import org.amahi.anywhere.server.ApiAdapter;
import org.amahi.anywhere.server.ApiConnection;
import org.amahi.anywhere.server.api.ProxyApi;
import org.amahi.anywhere.server.api.ServerApi;
import org.amahi.anywhere.server.model.Server;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerRoute;
import org.amahi.anywhere.server.model.ServerShare;
import org.amahi.anywhere.server.response.ServerAppsResponse;
import org.amahi.anywhere.server.response.ServerFilesResponse;
import org.amahi.anywhere.server.response.ServerRouteResponse;
import org.amahi.anywhere.server.response.ServerSharesResponse;
import org.amahi.anywhere.task.ServerConnectionDetectingTask;

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
	private ApiConnection serverConnection;

	private int network;

	@Inject
	public ServerClient(ApiAdapter apiAdapter) {
		this.apiAdapter = apiAdapter;
		this.proxyApi = buildProxyApi();

		this.serverConnection = ApiConnection.AUTO;

		this.network = Integer.MIN_VALUE;

		setUpBus();
	}

	private ProxyApi buildProxyApi() {
		return apiAdapter.create(ProxyApi.class, Api.getProxyUrl());
	}

	private void setUpBus() {
		BusProvider.getBus().register(this);
	}

	@Subscribe
	public void onNetworkChanged(NetworkChangedEvent event) {
		if (this.serverConnection != ApiConnection.AUTO) {
			return;
		}

		if (this.network != event.getNetwork()) {
			this.network = event.getNetwork();

			startServerConnectionDetection();
		}
	}

	private void startServerConnectionDetection() {
		this.serverAddress = serverRoute.getLocalAddress();
		this.serverApi = buildServerApi();

		ServerConnectionDetectingTask.execute(serverRoute);
	}

	@Subscribe
	public void onServerConnectionDetected(ServerConnectionDetectedEvent event) {
		this.serverAddress = event.getServerAddress();
		this.serverApi = buildServerApi();

		finishServerConnectionDetection();
	}

	private void finishServerConnectionDetection() {
		BusProvider.getBus().post(new ServerConnectionChangedEvent());
	}

	public boolean isConnected() {
		return (server != null) && (serverRoute != null) && (serverAddress != null);
	}

	public boolean isConnected(Server server) {
		return (this.server != null) && (this.server.getSession().equals(server.getSession()));
	}

	public void connect(Server server) {
		this.server = server;

		startServerConnection();
	}

	private void startServerConnection() {
		proxyApi.getServerRoute(server.getSession(), new ServerRouteResponse());
	}

	@Subscribe
	public void onServerRouteLoaded(ServerRouteLoadedEvent event) {
		this.serverRoute = event.getServerRoute();

		finishServerConnection();
	}

	private void finishServerConnection() {
		BusProvider.getBus().post(new ServerConnectedEvent());
	}

	public void connectAuto() {
		this.serverConnection = ApiConnection.AUTO;

		startServerConnectionDetection();
	}

	public void connectLocal() {
		this.serverConnection = ApiConnection.LOCAL;
		this.serverAddress = serverRoute.getLocalAddress();
		this.serverApi = buildServerApi();
	}

	private ServerApi buildServerApi() {
		return apiAdapter.create(ServerApi.class, serverAddress);
	}

	public void connectRemote() {
		this.serverConnection = ApiConnection.REMOTE;
		this.serverAddress = serverRoute.getRemoteAddress();
		this.serverApi = buildServerApi();
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

	public void getApps() {
		serverApi.getApps(server.getSession(), new ServerAppsResponse());
	}
}
