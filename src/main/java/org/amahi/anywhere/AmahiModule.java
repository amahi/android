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
import android.content.Context;

import org.amahi.anywhere.activity.AuthenticationActivity;
import org.amahi.anywhere.activity.NativeVideoActivity;
import org.amahi.anywhere.activity.NavigationActivity;
import org.amahi.anywhere.activity.OfflineFilesActivity;
import org.amahi.anywhere.activity.RecentFilesActivity;
import org.amahi.anywhere.activity.ServerAppActivity;
import org.amahi.anywhere.activity.ServerFileAudioActivity;
import org.amahi.anywhere.activity.ServerFileImageActivity;
import org.amahi.anywhere.activity.ServerFileVideoActivity;
import org.amahi.anywhere.activity.ServerFileWebActivity;
import org.amahi.anywhere.activity.ServerFilesActivity;
import org.amahi.anywhere.cache.CacheModule;
import org.amahi.anywhere.fragment.AudioListFragment;
import org.amahi.anywhere.fragment.NavigationFragment;
import org.amahi.anywhere.fragment.ServerAppsFragment;
import org.amahi.anywhere.fragment.ServerFileAudioFragment;
import org.amahi.anywhere.fragment.ServerFileDownloadingFragment;
import org.amahi.anywhere.fragment.ServerFileImageFragment;
import org.amahi.anywhere.fragment.ServerFilesFragment;
import org.amahi.anywhere.fragment.ServerSharesFragment;
import org.amahi.anywhere.fragment.SettingsFragment;
import org.amahi.anywhere.fragment.UploadSettingsFragment;
import org.amahi.anywhere.server.ApiModule;
import org.amahi.anywhere.service.AudioService;
import org.amahi.anywhere.service.DownloadService;
import org.amahi.anywhere.service.UploadService;
import org.amahi.anywhere.service.VideoService;
import org.amahi.anywhere.task.AudioMetadataRetrievingTask;
import org.amahi.anywhere.tv.activity.TVWebViewActivity;
import org.amahi.anywhere.tv.activity.TvPlaybackAudioActivity;
import org.amahi.anywhere.tv.activity.TvPlaybackVideoActivity;
import org.amahi.anywhere.tv.fragment.MainTVFragment;
import org.amahi.anywhere.tv.fragment.ServerFileTvFragment;
import org.amahi.anywhere.tv.fragment.TvPlaybackAudioFragment;
import org.amahi.anywhere.tv.fragment.TvPlaybackVideoFragment;
import org.amahi.anywhere.util.UploadManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Application dependency injection module. Includes {@link org.amahi.anywhere.server.ApiModule} and
 * provides application's {@link android.content.Context} for possible consumers.
 */
@Module(
    includes = {
        ApiModule.class,
        CacheModule.class
    },
    injects = {
        AuthenticationActivity.class,
        NavigationActivity.class,
        ServerAppActivity.class,
        OfflineFilesActivity.class,
        ServerFilesActivity.class,
        ServerFileAudioActivity.class,
        ServerFileImageActivity.class,
        ServerFileVideoActivity.class,
        NativeVideoActivity.class,
        RecentFilesActivity.class,
        ServerFileWebActivity.class,
        NavigationFragment.class,
        ServerSharesFragment.class,
        ServerAppsFragment.class,
        ServerFilesFragment.class,
        ServerFileImageFragment.class,
        ServerFileAudioFragment.class,
        ServerFileDownloadingFragment.class,
        SettingsFragment.class,
        UploadSettingsFragment.class,
        AudioListFragment.class,
        AudioService.class,
        VideoService.class,
        MainTVFragment.class,
        TVWebViewActivity.class,
        ServerFileTvFragment.class,
        UploadService.class,
        DownloadService.class,
        UploadManager.class,
        TvPlaybackVideoFragment.class,
        TvPlaybackVideoActivity.class,
        TvPlaybackAudioActivity.class,
        TvPlaybackAudioFragment.class,
        AudioMetadataRetrievingTask.class
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
