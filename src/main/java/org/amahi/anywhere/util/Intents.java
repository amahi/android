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
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Parcelable;

import org.amahi.anywhere.activity.ServerFileAudioActivity;
import org.amahi.anywhere.activity.ServerFileImageActivity;
import org.amahi.anywhere.activity.ServerFileVideoActivity;
import org.amahi.anywhere.activity.ServerFileWebActivity;
import org.amahi.anywhere.activity.ServerFilesActivity;
import org.amahi.anywhere.activity.SettingsActivity;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerShare;

import java.util.ArrayList;
import java.util.List;

public final class Intents
{
	private Intents() {
	}

	public static final class Extras
	{
		private Extras() {
		}

		public static final String SERVER_FILE = "server_file";
		public static final String SERVER_FILES = "server_files";
		public static final String SERVER_SHARE = "server_share";
	}

	public static final class Uris
	{
		private Uris() {
		}

		public static final String EMAIL = "mailto:%s?subject=%s";

		public static final String GOOGLE_PLAY = "market://details?id=%s";
		public static final String GOOGLE_PLAY_SEARCH = "market://search?q=%s";
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

		public Intent buildServerFilesActivity(ServerShare share) {
			Intent intent = new Intent(context, ServerFilesActivity.class);
			intent.putExtra(Extras.SERVER_SHARE, share);

			return intent;
		}

		public boolean isServerFileSupported(ServerFile file) {
			return getServerFileActivity(file) != null;
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

			return null;
		}

		public Intent buildServerFileIntent(ServerShare share, List<ServerFile> files, ServerFile file) {
			Intent intent = new Intent(context, getServerFileActivity(file));
			intent.putExtra(Extras.SERVER_SHARE, share);
			intent.putParcelableArrayListExtra(Extras.SERVER_FILES, new ArrayList<Parcelable>(files));
			intent.putExtra(Extras.SERVER_FILE, file);

			return intent;
		}

		public boolean isServerFileOpeningSupported(ServerFile file) {
			PackageManager packageManager = context.getPackageManager();

			List<ResolveInfo> applications = packageManager.queryIntentActivities(
				buildServerFileOpeningIntent(file),
				PackageManager.MATCH_DEFAULT_ONLY);

			return !applications.isEmpty();
		}

		private Intent buildServerFileOpeningIntent(ServerFile file) {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setType(file.getMime());

			return intent;
		}

		public Intent buildServerFileOpeningIntent(ServerFile file, Uri fileUri) {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(fileUri, file.getMime());

			return Intent.createChooser(intent, null);
		}

		public Intent buildServerFileSharingIntent(ServerFile file, Uri fileUri) {
			Intent intent = new Intent(Intent.ACTION_SEND);
			intent.setType(file.getMime());
			intent.putExtra(Intent.EXTRA_STREAM, fileUri);

			return Intent.createChooser(intent, null);
		}

		public Intent buildSettingsIntent() {
			return new Intent(context, SettingsActivity.class);
		}

		public Intent buildVersionIntent() {
			String versionUri = "https://www.amahi.org/android";

			return new Intent(Intent.ACTION_VIEW, Uri.parse(versionUri));
		}

		public Intent buildFeedbackIntent() {
			String feedbackAddress = "support@amahi.org";
			String feedbackSubject = "Android Amahi Anywhere";

			String feedbackUri = String.format(Uris.EMAIL, feedbackAddress, feedbackSubject);

			return new Intent(Intent.ACTION_SENDTO, Uri.parse(feedbackUri));
		}

		public Intent buildGooglePlayIntent() {
			String googlePlayUri = String.format(Uris.GOOGLE_PLAY, context.getPackageName());

			return new Intent(Intent.ACTION_VIEW, Uri.parse(googlePlayUri));
		}

		public Intent buildGooglePlaySearchIntent(String search) {
			String googlePlaySearchUri = String.format(Uris.GOOGLE_PLAY_SEARCH, search);

			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse(googlePlaySearchUri));

			return intent;
		}
	}
}
