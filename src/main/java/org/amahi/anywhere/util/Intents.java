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
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;

import org.amahi.anywhere.activity.ServerFileAudioActivity;
import org.amahi.anywhere.activity.ServerFileImageActivity;
import org.amahi.anywhere.activity.ServerFileVideoActivity;
import org.amahi.anywhere.activity.ServerFileWebActivity;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerShare;

public final class Intents
{
	private Intents() {
	}

	public static final class Extras
	{
		private Extras() {
		}

		public static final String SERVER_SHARE = "server_share";
		public static final String SERVER_FILE = "server_file";
	}

	public static final class Builder
	{
		private final Context context;

		public static Builder with(Context context) {
			return new Builder(context);
		}

		private Builder(Context context) {
			this.context = context;
		}

		public Intent buildServerFileIntent(ServerShare share, ServerFile file) {
			Intent intent = new Intent(context, getServerFileActivity(file));
			intent.putExtra(Extras.SERVER_SHARE, share);
			intent.putExtra(Extras.SERVER_FILE, file);

			return intent;
		}

		private Class<? extends Activity> getServerFileActivity(ServerFile file) {
			String fileFormat = file.getMime();

			if (ServerFileAudioActivity.SUPPORTED_FORMATS.contains(fileFormat)) {
				return ServerFileAudioActivity.class;
			}

			if (ServerFileImageActivity.SUPPORTED_FORMATS.contains(fileFormat)) {
				return ServerFileImageActivity.class;
			}

			if (ServerFileVideoActivity.SUPPORTED_FORMATS.contains(fileFormat)) {
				return ServerFileVideoActivity.class;
			}

			if (ServerFileWebActivity.SUPPORTED_FORMATS.contains(fileFormat)) {
				return ServerFileWebActivity.class;
			}

			throw new ActivityNotFoundException();
		}
	}
}
