package org.amahi.anywhere;

import android.app.Application;

import org.amahi.anywhere.activity.ServersActivity;
import org.amahi.anywhere.server.ApiModule;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
	includes = {
		ApiModule.class
	},
	injects = {
		ServersActivity.class
	}
)
class AmahiModule
{
	private final Application application;

	public AmahiModule(Application application) {
		this.application = application;
	}

	@Provides
	@Singleton
	Application provideApplication() {
		return application;
	}
}
