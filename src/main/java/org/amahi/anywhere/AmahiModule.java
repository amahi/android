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

import org.amahi.anywhere.fragment.NavigationFragment;
import org.amahi.anywhere.fragment.ServerFileAudioFragment;
import org.amahi.anywhere.fragment.ServerFileImageFragment;
import org.amahi.anywhere.fragment.ServerFileVideoFragment;
import org.amahi.anywhere.fragment.ServerFileWebFragment;
import org.amahi.anywhere.fragment.ServerFilesFragment;
import org.amahi.anywhere.server.ApiModule;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
	includes = {
		ApiModule.class
	},
	injects = {
		NavigationFragment.class,
		ServerFilesFragment.class,
		ServerFileImageFragment.class,
		ServerFileAudioFragment.class,
		ServerFileImageFragment.class,
		ServerFileVideoFragment.class,
		ServerFileWebFragment.class
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
