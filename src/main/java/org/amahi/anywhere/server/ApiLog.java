package org.amahi.anywhere.server;

import retrofit.RestAdapter;
import timber.log.Timber;

public class ApiLog implements RestAdapter.Log
{
	@Override
	public void log(String message) {
		Timber.tag("API");

		Timber.d(message);
	}
}
