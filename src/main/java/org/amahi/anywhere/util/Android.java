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

package org.amahi.anywhere.util;

import android.content.Context;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import org.amahi.anywhere.BuildConfig;
import org.amahi.anywhere.R;

/**
 * Android properties accessor.
 */
public final class Android {
    private Android() {
    }

    public static boolean isTablet(Context context) {
        return context.getResources().getBoolean(R.bool.tablet);
    }

    public static boolean isPermissionRequired() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public static String getVersion() {
        return Build.VERSION.RELEASE;
    }

    public static String getApplicationVersion() {
        return BuildConfig.VERSION_NAME;
    }

    public static String getDeviceName() {
        return Build.MODEL;
    }

    public static int getDeviceScreenWidth(Context context) {
        return getDeviceScreenMetrics(context).widthPixels;
    }

    public static int getDeviceScreenHeight(Context context) {
        return getDeviceScreenMetrics(context).heightPixels;
    }

    public static double getDeviceScreenSize(Context context) {
        DisplayMetrics screenMetrics = getDeviceScreenMetrics(context);

        float screenWidth = screenMetrics.widthPixels / screenMetrics.xdpi;
        float screenHeight = screenMetrics.heightPixels / screenMetrics.ydpi;

        return Math.sqrt(Math.pow(screenWidth, 2) + Math.pow(screenHeight, 2));
    }

    private static DisplayMetrics getDeviceScreenMetrics(Context context) {
        DisplayMetrics screenMetrics = new DisplayMetrics();

        WindowManager windows = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            windows.getDefaultDisplay().getRealMetrics(screenMetrics);
        } else {
            windows.getDefaultDisplay().getMetrics(screenMetrics);
        }

        return screenMetrics;
    }
}
