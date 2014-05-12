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
import android.app.Fragment;
import android.app.FragmentManager;

public final class Fragments
{
	private Fragments() {
	}

	public static final class Arguments
	{
		private Arguments() {
		}

		public static final String SERVER = "server";
		public static final String SERVER_FILE = "server_file";
		public static final String SERVER_SHARE = "server_share";
	}

	public static final class Operator
	{
		private final FragmentManager fragmentManager;

		public static Operator at(Activity activity) {
			return new Operator(activity);
		}

		private Operator(Activity activity) {
			this.fragmentManager = activity.getFragmentManager();
		}

		public void set(Fragment fragment, int fragmentContainerId) {
			if (isSet(fragmentContainerId)) {
				return;
			}

			fragmentManager
				.beginTransaction()
				.add(fragmentContainerId, fragment)
				.commit();
		}

		private boolean isSet(int fragmentContainerId) {
			return fragmentManager.findFragmentById(fragmentContainerId) != null;
		}

		public void replaceBackstacked(Fragment fragment, int fragmentConainerId) {
			fragmentManager
				.beginTransaction()
				.replace(fragmentConainerId, fragment)
				.addToBackStack(null)
				.commit();
		}

		public void removeBackstaced() {
			fragmentManager.popBackStack();
		}
	}
}
