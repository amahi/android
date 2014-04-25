package org.amahi.anywhere.server;

import org.amahi.anywhere.BuildConfig;

public final class Api
{
	private Api() {
	}

	public static String getAmahiUrl() {
		return BuildConfig.API_URL_AMAHI;
	}

	public static String getProxyUrl() {
		return BuildConfig.API_URL_PROXY;
	}

	public static String getClientId() {
		return BuildConfig.API_CLIENT_ID;
	}

	public static String getClientSecret() {
		return BuildConfig.API_CLIENT_SECRET;
	}
}
