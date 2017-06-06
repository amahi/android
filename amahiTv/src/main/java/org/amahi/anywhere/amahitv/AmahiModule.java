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

package org.amahi.anywhere.amahitv;

import android.app.Application;
import android.content.Context;

import org.amahi.anywhere.amahitv.activity.AuthenticationActivity;
import org.amahi.anywhere.amahitv.activity.NavigationActivity;
import org.amahi.anywhere.amahitv.activity.ServerAppActivity;
import org.amahi.anywhere.amahitv.activity.ServerFileAudioActivity;
import org.amahi.anywhere.amahitv.activity.ServerFileImageActivity;
import org.amahi.anywhere.amahitv.activity.ServerFileVideoActivity;
import org.amahi.anywhere.amahitv.activity.ServerFileWebActivity;
import org.amahi.anywhere.amahitv.activity.ServerFilesActivity;
import org.amahi.anywhere.amahitv.fragment.NavigationFragment;
import org.amahi.anywhere.amahitv.fragment.ServerAppsFragment;
import org.amahi.anywhere.amahitv.fragment.ServerFileDownloadingFragment;
import org.amahi.anywhere.amahitv.fragment.ServerFileImageFragment;
import org.amahi.anywhere.amahitv.fragment.ServerFilesFragment;
import org.amahi.anywhere.amahitv.fragment.ServerSharesFragment;
import org.amahi.anywhere.amahitv.fragment.SettingsFragment;
import org.amahi.anywhere.amahitv.server.ApiModule;
import org.amahi.anywhere.amahitv.service.AudioService;
import org.amahi.anywhere.amahitv.service.VideoService;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Application dependency injection module. Includes {@link org.amahi.anywhere.amahitv.server.ApiModule} and
 * provides application's {@link Context} for possible consumers.
 */
@Module(
        includes = {
                ApiModule.class
        },
        injects = {
                AuthenticationActivity.class,
                NavigationActivity.class,
                ServerAppActivity.class,
                ServerFilesActivity.class,
                ServerFileAudioActivity.class,
                ServerFileImageActivity.class,
                ServerFileVideoActivity.class,
                ServerFileWebActivity.class,
                NavigationFragment.class,
                ServerSharesFragment.class,
                ServerAppsFragment.class,
                ServerFilesFragment.class,
                ServerFileImageFragment.class,
                ServerFileDownloadingFragment.class,
                SettingsFragment.class,
                AudioService.class,
                VideoService.class
        }
)
class AmahiModule {
    private final Application application;

    public AmahiModule(Application application) {
        this.application = application;
    }

    @Provides
    @Singleton
    Context provideContext() {
        return application;
    }
}
