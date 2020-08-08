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
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import androidx.annotation.RequiresApi;
import android.util.Log;

import org.amahi.anywhere.AmahiApplication.JobIds;
import org.amahi.anywhere.util.Intents;

import java.util.ArrayList;
import java.util.List;

/**
 * Job to monitor when there is a change to photos in the media provider.
 */
@RequiresApi(api = Build.VERSION_CODES.N)
public class PhotosContentJob extends JobService {
    // The root URI of the media provider, to monitor for generic changes to its content.
    static final Uri MEDIA_URI = Uri.parse("content://" + MediaStore.AUTHORITY + "/");
    // Path segments for image-specific URIs in the provider.
    static final List<String> EXTERNAL_PATH_SEGMENTS
        = MediaStore.Images.Media.EXTERNAL_CONTENT_URI.getPathSegments();
    // A pre-built JobInfo we use for scheduling our job.
    static final JobInfo JOB_INFO;

    static {
        JobInfo.Builder builder = new JobInfo.Builder(JobIds.PHOTOS_CONTENT_JOB,
            new ComponentName("org.amahi.anywhere", PhotosContentJob.class.getName()));
        // Look for specific changes to images in the provider.
        builder.addTriggerContentUri(new JobInfo.TriggerContentUri(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            JobInfo.TriggerContentUri.FLAG_NOTIFY_FOR_DESCENDANTS));
        // Also look for general reports of changes in the overall provider.
        builder.addTriggerContentUri(new JobInfo.TriggerContentUri(MEDIA_URI, 0));
        JOB_INFO = builder.build();
    }

    private static final String TAG = PhotosContentJob.class.getName();
    JobParameters mRunningParams;

    // Schedule this job, replace any existing one.
    public static void scheduleJob(Context context) {
        JobScheduler js = context.getSystemService(JobScheduler.class);
        js.schedule(JOB_INFO);
        Log.i(TAG, "JOB SCHEDULED!");
    }

    // Check whether this job is currently scheduled.
    public static boolean isScheduled(Context context) {
        JobScheduler js = context.getSystemService(JobScheduler.class);
        JobInfo job = js.getPendingJob(JobIds.PHOTOS_CONTENT_JOB);
        return job != null;
    }

    // Cancel this job, if currently scheduled.
    public static void cancelJob(Context context) {
        JobScheduler js = context.getSystemService(JobScheduler.class);
        js.cancel(JobIds.PHOTOS_CONTENT_JOB);
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.i(TAG, "JOB STARTED!");
        mRunningParams = params;

        // Did we trigger due to a content change?
        if (params.getTriggeredContentAuthorities() != null) {
            if (params.getTriggeredContentUris() != null) {
                // If we have details about which URIs changed, then iterate through them
                // and collect valid uris and send them to UploadService
                ArrayList<Uri> uris = new ArrayList<>();
                for (Uri uri : params.getTriggeredContentUris()) {
                    List<String> path = uri.getPathSegments();
                    if (path != null && path.size() == EXTERNAL_PATH_SEGMENTS.size() + 1) {
                        // This is a specific file.
                        uris.add(uri);
                    }
                }
                Intent intent = Intents.Builder.with(this).buildUploadServiceIntent(uris);
                startService(intent);
            } else {
                // We don't have any details about URIs (because too many changed at once)
                Log.i(TAG, "Photos rescan needed!");
            }
        } else {
            Log.i(TAG, "(No photos content)");
        }

        scheduleJob(PhotosContentJob.this);
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
