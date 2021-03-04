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
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.StrictMode;
import android.preference.PreferenceManager;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatDelegate;

import org.amahi.anywhere.job.NetConnectivityJob;
import org.amahi.anywhere.job.PhotosContentJob;
import org.amahi.anywhere.util.AmahiLifeCycleCallback;
import org.amahi.anywhere.util.AppTheme;

import dagger.ObjectGraph;

/**
 * Application declaration. Basically sets things up at the startup time,
 * such as dependency injection, logging, crash reporting and possible ANR detecting.
 */

public class AmahiApplication extends Application {
    private ObjectGraph injector;

    public static final String UPLOAD_CHANNEL_ID = "file_upload";
    public static final String DOWNLOAD_CHANNEL_ID = "file_download";

    private AppTheme themeEnabled = AppTheme.DEFAULT;
    private static AmahiApplication instance = null;

    public static AmahiApplication from(Context context) {
        return (AmahiApplication) context.getApplicationContext();
    }

    public static AmahiApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        setUpTheme();
        super.onCreate();

        instance = this;

        setUpDetecting();

        setUpActivityCallbacks();

        setUpInjections();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            setUpJobs();
        }
    }

    private void setUpTheme() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String val = preferences.getString(getString(R.string.pref_key_theme_list), getString(R.string.preference_key_system_default_theme));

        if (val.equals(getString(R.string.preference_key_system_default_theme))) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            this.themeEnabled = AppTheme.DEFAULT;

        } else if (val.equals(getString(R.string.preference_key_light_theme))) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            this.themeEnabled = AppTheme.LIGHT;

        } else if (val.equals(getString(R.string.preference_key_dark_theme))) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            this.themeEnabled = AppTheme.DARK;

        }

    }

    private boolean isDebugging() {
        return BuildConfig.DEBUG;
    }

    private void setUpDetecting() {
        if (isDebugging()) {
            StrictMode.enableDefaults();
        }
    }

    private void setUpActivityCallbacks() {
        if (isDebugging()) {
            registerActivityLifecycleCallbacks(new AmahiLifeCycleCallback());
        }
    }

    private void setUpInjections() {
        injector = ObjectGraph.create(new AmahiModule(this));
    }

    public void inject(Object injectionsConsumer) {
        injector.inject(injectionsConsumer);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void setUpJobs() {
        if (!PhotosContentJob.isScheduled(this)) {
            PhotosContentJob.scheduleJob(this);
        }
        if (!NetConnectivityJob.isScheduled(this)) {
            NetConnectivityJob.scheduleJob(this);
        }
    }

    public AppTheme getThemeEnabled() {
        return themeEnabled;
    }

    public void setThemeEnabled(AppTheme themeEnabled) {
        this.themeEnabled = themeEnabled;
    }

    public static class JobIds {
        public static final int PHOTOS_CONTENT_JOB = 125;
        public static final int NET_CONNECTIVITY_JOB = 126;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel() {

        // Creating NotificationChannel only for API 26+
        int importanceDownload = NotificationManager.IMPORTANCE_LOW;
        int importanceUpload = NotificationManager.IMPORTANCE_LOW;

        NotificationChannel uploadChannel = new NotificationChannel(UPLOAD_CHANNEL_ID, getString(R.string.upload_channel), importanceUpload);
        uploadChannel.setDescription(getString(R.string.upload_channel_desc));

        NotificationChannel downloadChannel = new NotificationChannel(DOWNLOAD_CHANNEL_ID, getString(R.string.download_channel), importanceDownload);
        downloadChannel.setDescription(getString(R.string.download_channel_desc));

        // Once the channel is registered, it's importance and behaviour can't be changed
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(uploadChannel);
        notificationManager.createNotificationChannel(downloadChannel);
    }
}
