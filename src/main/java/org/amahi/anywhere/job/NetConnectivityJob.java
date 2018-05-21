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

package org.amahi.anywhere.job;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import org.amahi.anywhere.AmahiApplication.JobIds;
import org.amahi.anywhere.service.DownloadService;
import org.amahi.anywhere.service.UploadService;

/**
 * Job to monitor when there is a change to photos in the media provider.
 */
@RequiresApi(api = Build.VERSION_CODES.N)
public class NetConnectivityJob extends JobService {
    // A pre-built JobInfo we use for scheduling our job.
    static final JobInfo JOB_INFO;

    static {
        JobInfo.Builder builder = new JobInfo.Builder(JobIds.NET_CONNECTIVITY_JOB,
            new ComponentName("org.amahi.anywhere", NetConnectivityJob.class.getName()));
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
        JOB_INFO = builder.build();
    }

    private final String TAG = this.getClass().getName();

    // Schedule this job, replace any existing one.
    public static void scheduleJob(Context context) {
        JobScheduler js = context.getSystemService(JobScheduler.class);
        js.schedule(JOB_INFO);
        Log.i("NetworkConnectivityJob", "JOB SCHEDULED!");
    }

    // Check whether this job is currently scheduled.
    public static boolean isScheduled(Context context) {
        JobScheduler js = context.getSystemService(JobScheduler.class);
        JobInfo job = js.getPendingJob(JobIds.NET_CONNECTIVITY_JOB);
        return job != null;
    }

    // Cancel this job, if currently scheduled.
    public static void cancelJob(Context context) {
        JobScheduler js = context.getSystemService(JobScheduler.class);
        js.cancel(JobIds.NET_CONNECTIVITY_JOB);
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.i(TAG, "JOB STARTED!");
        Intent intent = new Intent(this, UploadService.class);
        startService(intent);
        startDownloadService(this);
        return false;
    }

    private void startDownloadService(Context context) {
        Intent downloadService = new Intent(context, DownloadService.class);
        downloadService.setAction(ConnectivityManager.CONNECTIVITY_ACTION);
        context.startService(downloadService);
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
