package org.amahi.anywhere.bus;

import org.amahi.anywhere.server.model.Authentication;

public class AuthenticationDoneEvent implements BusEvent
{
	private final Authentication authentication;

	public AuthenticationDoneEvent(Authentication authentication) {
		this.authentication = authentication;
	}

	public Authentication getAuthentication() {
		return authentication;
	}
}
