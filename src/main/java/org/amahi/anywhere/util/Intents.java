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
import android.content.Intent;

import org.amahi.anywhere.activity.ServerFilesActivity;
import org.amahi.anywhere.activity.ServerSharesActivity;
import org.amahi.anywhere.server.model.Server;
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

		public static final String SERVER = "server";
		public static final String SERVER_FILE = "server_file";
		public static final String SERVER_SHARE = "server_share";
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

		public Intent buildServerSharesIntent(Server server) {
			Intent intent = new Intent(context, ServerSharesActivity.class);
			intent.putExtra(Extras.SERVER, server);

			return intent;
		}

		public Intent buildServerFilesIntent(ServerShare serverShare, ServerFile serverDirectory) {
			Intent intent = new Intent(context, ServerFilesActivity.class);
			intent.putExtra(Extras.SERVER_SHARE, serverShare);
			intent.putExtra(Extras.SERVER_FILE, serverDirectory);

			return intent;
		}
	}
}
