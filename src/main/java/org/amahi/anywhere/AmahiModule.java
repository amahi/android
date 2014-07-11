/*
 * Copyright (c) 2014 Amahi
 *
 * This file is part of Amahi.
 *
 * Amahi is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Amahi is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Amahi. If not, see <http ://www.gnu.org/licenses/>.
 */

package org.amahi.anywhere;

import android.app.Application;

import org.amahi.anywhere.activity.AuthenticationActivity;
import org.amahi.anywhere.activity.ServerFileAudioActivity;
import org.amahi.anywhere.activity.ServerFileImageActivity;
import org.amahi.anywhere.activity.ServerFileVideoActivity;
import org.amahi.anywhere.activity.ServerFileWebActivity;
import org.amahi.anywhere.activity.ServerFilesActivity;
import org.amahi.anywhere.fragment.NavigationFragment;
import org.amahi.anywhere.fragment.ServerFileImageFragment;
import org.amahi.anywhere.fragment.ServerFilesFragment;
import org.amahi.anywhere.fragment.SettingsFragment;
import org.amahi.anywhere.server.ApiModule;
import org.amahi.anywhere.service.AudioService;
import org.amahi.anywhere.service.VideoService;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
	includes = {
		ApiModule.class
	},
	injects = {
		AuthenticationActivity.class,
		ServerFilesActivity.class,
		ServerFileAudioActivity.class,
		ServerFileImageActivity.class,
		ServerFileVideoActivity.class,
		ServerFileWebActivity.class,
		NavigationFragment.class,
		ServerFilesFragment.class,
		ServerFileImageFragment.class,
		SettingsFragment.class,
		AudioService.class,
		VideoService.class
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
