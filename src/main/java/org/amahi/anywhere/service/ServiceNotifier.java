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

package org.amahi.anywhere.service;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.Service;
import android.content.Context;

import androidx.core.app.NotificationCompat;

import org.amahi.anywhere.R;

abstract public class ServiceNotifier extends Service {

    public NotificationCompat.Builder startForegroundNotif(String channelID) {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), channelID);
        notificationBuilder
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_app_logo)
            .setContentTitle(getString(R.string.application_name))
            .setShowWhen(false)
            .build();
        Notification notification = notificationBuilder.build();
        int nID = (int) System.currentTimeMillis() % 10000;
        startForeground(nID, notification);
        return notificationBuilder;
    }

    private static boolean isServiceRunningInForeground(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                if (service.foreground) {
                    return true;
                }
            }
        }
        return false;
    }

    public void stopForegroundService(Context ctx, Service serviceClass, boolean flag) {
        if (isServiceRunningInForeground(ctx, serviceClass.getClass())) {
            serviceClass.stopForeground(flag);
        }
    }
}
