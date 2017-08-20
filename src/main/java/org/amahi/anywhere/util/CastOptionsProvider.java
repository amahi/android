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

import com.google.android.gms.cast.framework.CastOptions;
import com.google.android.gms.cast.framework.OptionsProvider;
import com.google.android.gms.cast.framework.SessionProvider;
import com.google.android.gms.cast.framework.media.CastMediaOptions;
import com.google.android.gms.cast.framework.media.NotificationOptions;
import com.google.android.gms.cast.framework.media.widget.ExpandedControllerActivity;

import org.amahi.anywhere.R;

import java.util.List;

/**
 * Cast options provider helper class
 */
class CastOptionsProvider implements OptionsProvider {

	@Override
	public CastOptions getCastOptions(Context appContext) {
		NotificationOptions notificationOptions = new NotificationOptions.Builder()
				.setTargetActivityClassName(ExpandedControllerActivity.class.getName())
				.build();
		CastMediaOptions mediaOptions = new CastMediaOptions.Builder()
				.setNotificationOptions(notificationOptions)
				.setExpandedControllerActivityClassName(ExpandedControllerActivity.class.getName())
				.build();

		return new CastOptions.Builder()
				.setReceiverApplicationId(appContext.getString(R.string.app_id))
				.setCastMediaOptions(mediaOptions)
				.build();
	}

	@Override
	public List<SessionProvider> getAdditionalSessionProviders(Context context) {
		return null;
	}

}
