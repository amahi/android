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
import android.os.StrictMode;

import com.crashlytics.android.Crashlytics;

import dagger.ObjectGraph;
import timber.log.Timber;

public class AmahiApplication extends Application
{
	private ObjectGraph injector;

	public static AmahiApplication from(Context context) {
		return (AmahiApplication) context.getApplicationContext();
	}

	@Override
	public void onCreate() {
		super.onCreate();

		setUpLogging();
		setUpReporting();
		setUpDetecting();

		setUpInjections();
	}

	private void setUpLogging() {
		if (isDebugging()) {
			Timber.plant(new Timber.DebugTree());
		}
	}

	private boolean isDebugging() {
		return BuildConfig.DEBUG;
	}

	private void setUpReporting() {
		if (!isDebugging()) {
			Crashlytics.start(this);
		}
	}

	private void setUpDetecting() {
		if (isDebugging()) {
			StrictMode.enableDefaults();
		}
	}

	private void setUpInjections() {
		injector = ObjectGraph.create(new AmahiModule(this));
	}

	public void inject(Object injectionsConsumer) {
		injector.inject(injectionsConsumer);
	}
}
