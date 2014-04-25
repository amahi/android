package org.amahi.anywhere.server.header;

public class SessionHeaders extends ApiHeaders
{
	private static final class HeaderFields
	{
		private HeaderFields() {
		}

		public static final String SESSION = "Session";
	}

	private final String session;

	public SessionHeaders(String session) {
		this.session = session;
	}

	@Override
	public void intercept(RequestFacade request) {
		super.intercept(request);

		request.addHeader(HeaderFields.SESSION, session);
	}
}
