package org.amahi.anywhere;

import org.amahi.anywhere.activity.ServersActivity;
import org.amahi.anywhere.server.ApiModule;

import dagger.Module;

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
}
