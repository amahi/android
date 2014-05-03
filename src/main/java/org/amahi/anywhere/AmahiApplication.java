package org.amahi.anywhere;

import android.app.Application;
import android.content.Context;

import dagger.ObjectGraph;

public class AmahiApplication extends Application
{
	private ObjectGraph injector;

	public static AmahiApplication from(Context context) {
		return (AmahiApplication) context.getApplicationContext();
	}

	@Override
	public void onCreate() {
		super.onCreate();

		setUpInjections();
	}

	private void setUpInjections() {
		injector = ObjectGraph.create(new AmahiModule(this));
	}

	public void inject(Object injectionsConsumer) {
		injector.inject(injectionsConsumer);
	}
}
