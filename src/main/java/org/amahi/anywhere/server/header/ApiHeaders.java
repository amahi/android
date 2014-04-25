package org.amahi.anywhere.server.header;

import org.amahi.anywhere.util.Android;

import retrofit.RequestInterceptor;

public class ApiHeaders implements RequestInterceptor
{
	private static final class HeaderFields
	{
		private HeaderFields() {
		}

		public static final String ACCEPT = "Accept";
		public static final String USER_AGENT = "User-Agent";
	}

	private static final class HeaderValues
	{
		private HeaderValues() {
		}

		public static final String ACCEPT = "application/json";
		public static final String USER_AGENT;

		static {
			USER_AGENT = String.format("AmahiAnywhere/%s (Android %s)", Android.getApplicationVersion(), Android.getVersion());
		}
	}

	@Override
	public void intercept(RequestFacade request) {
		request.addHeader(HeaderFields.ACCEPT, HeaderValues.ACCEPT);
		request.addHeader(HeaderFields.USER_AGENT, HeaderValues.USER_AGENT);
	}
}
