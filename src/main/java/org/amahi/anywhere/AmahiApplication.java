package org.amahi.anywhere;

import android.app.Application;
import android.content.Context;

import dagger.ObjectGraph;

public class AmahiApplication extends Application
{
	private ObjectGraph injections;

	public static AmahiApplication from(Context context) {
		return (AmahiApplication) context.getApplicationContext();
	}

	@Override
	public void onCreate() {
		super.onCreate();

		setUpInjections();
	}

	private void setUpInjections() {
		injections = ObjectGraph.create(new AmahiModule());
	}

	public void inject(Object injectionsConsumer) {
		injections.inject(injectionsConsumer);
	}
}
