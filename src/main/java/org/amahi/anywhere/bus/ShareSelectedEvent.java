package org.amahi.anywhere.bus;

import org.amahi.anywhere.server.model.ServerShare;

public class ShareSelectedEvent implements BusEvent
{
	private final ServerShare share;

	public ShareSelectedEvent(ServerShare share) {
		this.share = share;
	}

	public ServerShare getShare() {
		return share;
	}
}
