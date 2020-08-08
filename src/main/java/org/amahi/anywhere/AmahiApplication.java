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

import com.crashlytics.android.Crashlytics;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.config.CoreConfigurationBuilder;
import org.acra.config.MailSenderConfigurationBuilder;
import org.acra.config.ToastConfigurationBuilder;
import org.acra.data.StringFormat;
import org.amahi.anywhere.job.NetConnectivityJob;
import org.amahi.anywhere.job.PhotosContentJob;
import org.amahi.anywhere.server.Api;

import dagger.ObjectGraph;
import io.fabric.sdk.android.Fabric;
import timber.log.Timber;

/**
 * Application declaration. Basically sets things up at the startup time,
 * such as dependency injection, logging, crash reporting and possible ANR detecting.
 */

public class AmahiApplication extends Application {
    private ObjectGraph injector;

    private static final String UPLOAD_CHANNEL_ID = "file_upload";
    private static final String DOWNLOAD_CHANNEL_ID = "file_download";

    private Boolean isLightThemeEnabled = false;
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
        setUpLogging();
        setUpReporting();
        setUpDetecting();

        setUpInjections();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            setUpJobs();
        }
    }

    private void setUpLogging() {
        if (isDebugging()) {
            Timber.plant(new Timber.DebugTree());
        }
    }

    private void setUpTheme() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        this.isLightThemeEnabled = preferences.getBoolean(getString(R.string.pref_key_light_theme), false);
        if (this.isLightThemeEnabled) {
            AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_YES);
        }
    }

    public void setIsLightThemeEnabled(Boolean isLightThemeEnabled) {
        this.isLightThemeEnabled = isLightThemeEnabled;
    }

    public Boolean isLightThemeEnabled() {
        return isLightThemeEnabled;
    }

    private boolean isDebugging() {
        return BuildConfig.DEBUG;
    }

    private void setUpReporting() {
        if (!isDebugging()) {
            Fabric.with(this, new Crashlytics());
        }
    }

    private void setUpDetecting() {
        if (isDebugging()) {
//            StrictMode.enableDefaults();
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

    public static class JobIds {
        public static final int PHOTOS_CONTENT_JOB = 125;
        public static final int NET_CONNECTIVITY_JOB = 126;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel() {

        // Creating NotificationChannel only for API 26+
        int importance = NotificationManager.IMPORTANCE_DEFAULT;

        NotificationChannel uploadChannel = new NotificationChannel(UPLOAD_CHANNEL_ID, getString(R.string.upload_channel), importance);
        uploadChannel.setDescription(getString(R.string.upload_channel_desc));

        NotificationChannel downloadChannel = new NotificationChannel(DOWNLOAD_CHANNEL_ID, getString(R.string.download_channel), importance);
        downloadChannel.setDescription(getString(R.string.download_channel_desc));

        // Once the channel is registered, it's importance and behaviour can't be changed
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(uploadChannel);
        notificationManager.createNotificationChannel(downloadChannel);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        if (isDebugging()) {

            if(Api.toSendMail()) {
                CoreConfigurationBuilder builder = new CoreConfigurationBuilder(this)
                    .setBuildConfigClass(BuildConfig.class)
                    .setReportFormat(StringFormat.JSON)
                    .setAlsoReportToAndroidFramework(true)
                    .setReportContent(ReportField.APP_VERSION_CODE)
                    .setReportContent(ReportField.APP_VERSION_NAME)
                    .setReportContent(ReportField.ANDROID_VERSION)
                    .setReportContent(ReportField.PHONE_MODEL)
                    .setReportContent(ReportField.CUSTOM_DATA)
                    .setReportContent(ReportField.STACK_TRACE)
                    .setReportContent(ReportField.LOGCAT);

                builder.getPluginConfigurationBuilder(MailSenderConfigurationBuilder.class)
                    .setMailTo(Api.getAcraEmail())
                    .setEnabled(true);

                builder.getPluginConfigurationBuilder(ToastConfigurationBuilder.class)
                    .setResText(R.string.acra_report_toast)
                    .setEnabled(true);

                ACRA.init(this, builder);
            }
        }
    }
}
