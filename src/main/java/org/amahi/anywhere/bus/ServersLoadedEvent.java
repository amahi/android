package org.amahi.anywhere.bus;

import org.amahi.anywhere.server.model.Server;

import java.util.List;

public class ServersLoadedEvent implements BusEvent
{
	private final List<Server> servers;

	public ServersLoadedEvent(List<Server> servers) {
		this.servers = servers;
	}

	public List<Server> getServers() {
		return servers;
	}
}
