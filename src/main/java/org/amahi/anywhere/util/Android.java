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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

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

        windows.getDefaultDisplay().getRealMetrics(screenMetrics);

        return screenMetrics;
    }

    public static String loadServersFromAsset(Context context) {
        String json = "[]";
        try {
            InputStream is = context.getAssets().open("customServers.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException ignored) {
        }
        return json;
    }
}
