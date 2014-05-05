package org.amahi.anywhere.bus;

import org.amahi.anywhere.server.model.ServerFile;

import java.util.List;

public class ServerFilesLoadedEvent implements BusEvent
{
	private final List<ServerFile> serverFiles;

	public ServerFilesLoadedEvent(List<ServerFile> serverFiles) {
		this.serverFiles = serverFiles;
	}

	public List<ServerFile> getServerFiles() {
		return serverFiles;
	}
}
