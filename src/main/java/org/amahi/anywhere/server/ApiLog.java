package org.amahi.anywhere.server;

import retrofit.RestAdapter;
import timber.log.Timber;

class ApiLog implements RestAdapter.Log
{
	private static final String TAG = "API";

	@Override
	public void log(String message) {
		Timber.tag(TAG);

		Timber.d(message);
	}
}
