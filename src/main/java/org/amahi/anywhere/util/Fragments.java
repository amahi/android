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

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.amahi.anywhere.fragment.NavigationFragment;
import org.amahi.anywhere.fragment.ServerAppsFragment;
import org.amahi.anywhere.fragment.ServerFileImageFragment;
import org.amahi.anywhere.fragment.ServerFilesFragment;
import org.amahi.anywhere.fragment.ServerSharesFragment;
import org.amahi.anywhere.fragment.SettingsFragment;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerShare;

/**
 * Fragments accessor. Provides a factory for building fragments and an operator for placing them.
 */
public final class Fragments
{
	private Fragments() {
	}

	public static final class Arguments
	{
		private Arguments() {
		}

		public static final String SERVER_FILE = "server_file";
		public static final String SERVER_SHARE = "server_share";
	}

	public static final class Builder
	{
		private Builder() {
		}

		public static Fragment buildNavigationFragment() {
			return new NavigationFragment();
		}

		public static Fragment buildServerSharesFragment() {
			return new ServerSharesFragment();
		}

		public static Fragment buildServerAppsFragment() {
			return new ServerAppsFragment();
		}

		public static Fragment buildServerFilesFragment(ServerShare share, ServerFile directory) {
			Fragment filesFragment = new ServerFilesFragment();

			Bundle arguments = new Bundle();
			arguments.putParcelable(Arguments.SERVER_SHARE, share);
			arguments.putParcelable(Arguments.SERVER_FILE, directory);

			filesFragment.setArguments(arguments);

			return filesFragment;
		}

		public static Fragment buildServerFileImageFragment(ServerShare share, ServerFile file) {
			Fragment fileFragment = new ServerFileImageFragment();

			Bundle arguments = new Bundle();
			arguments.putParcelable(Arguments.SERVER_SHARE, share);
			arguments.putParcelable(Arguments.SERVER_FILE, file);

			fileFragment.setArguments(arguments);

			return fileFragment;
		}

		public static android.support.v4.app.Fragment buildSettingsFragment() {
			return new SettingsFragment();
		}
	}

	public static final class Operator
	{
		private final FragmentManager fragmentManager;

		public static Operator at(AppCompatActivity activity) {
			return new Operator(activity);
		}

		private Operator(AppCompatActivity activity) {
			this.fragmentManager = activity.getSupportFragmentManager();
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

		public void replace(Fragment fragment, int fragmentContainerId) {
			fragmentManager
				.beginTransaction()
				.replace(fragmentContainerId, fragment)
				.commit();
		}

		public void replaceBackstacked(Fragment fragment, int fragmentContainerId) {
			fragmentManager
				.beginTransaction()
				.replace(fragmentContainerId, fragment)
				.addToBackStack(null)
				.commit();
		}
	}
}
