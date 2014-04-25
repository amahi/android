package org.amahi.anywhere.util;

import android.os.Build;

import org.amahi.anywhere.BuildConfig;

public final class Android
{
	private Android() {
	}

	public static String getVersion() {
		return Build.VERSION.RELEASE;
	}

	public static String getApplicationVersion() {
		return BuildConfig.VERSION_NAME;
	}
}
