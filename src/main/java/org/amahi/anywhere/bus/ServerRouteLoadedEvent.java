package org.amahi.anywhere.bus;

import org.amahi.anywhere.server.model.ServerRoute;

public class ServerRouteLoadedEvent implements BusEvent
{
	private final ServerRoute serverRoute;

	public ServerRouteLoadedEvent(ServerRoute serverRoute) {
		this.serverRoute = serverRoute;
	}

	public ServerRoute getServerRoute() {
		return serverRoute;
	}
}
