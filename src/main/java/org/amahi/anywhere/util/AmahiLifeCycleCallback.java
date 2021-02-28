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

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class AmahiLifeCycleCallback implements Application.ActivityLifecycleCallbacks {

    private static final String TAG = "AmahiActivityCallback";

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {
        Log.v(TAG, activity.getClass().getSimpleName() + " Created");
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        Log.v(TAG, activity.getClass().getSimpleName() + " Started");
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        Log.v(TAG, activity.getClass().getSimpleName() + " Resumed");
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        Log.v(TAG, activity.getClass().getSimpleName() + " Paused");
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        Log.v(TAG, activity.getClass().getSimpleName() + " Stopped");
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {
        Log.v(TAG, activity.getClass().getSimpleName() + " Instance State Saved");
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        Log.v(TAG, activity.getClass().getSimpleName() + " Destroyed");
    }
}
