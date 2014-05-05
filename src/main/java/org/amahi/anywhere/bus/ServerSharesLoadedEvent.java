package org.amahi.anywhere.bus;

import org.amahi.anywhere.server.model.ServerShare;

import java.util.List;

public class ServerSharesLoadedEvent implements BusEvent
{
	private final List<ServerShare> serverShares;

	public ServerSharesLoadedEvent(List<ServerShare> serverShares) {
		this.serverShares = serverShares;
	}

	public List<ServerShare> getServerShares() {
		return serverShares;
	}
}
