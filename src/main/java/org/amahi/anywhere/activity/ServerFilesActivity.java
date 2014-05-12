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

package org.amahi.anywhere.activity;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;

import com.squareup.otto.Subscribe;

import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.FileSelectedEvent;
import org.amahi.anywhere.bus.ParentDirectorySelectedEvent;
import org.amahi.anywhere.fragment.ServerFilesFragment;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerShare;
import org.amahi.anywhere.util.Fragments;
import org.amahi.anywhere.util.Intents;

public class ServerFilesActivity extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setUpTitle();
		setUpFragment();
	}

	private void setUpTitle() {
		getActionBar().setTitle(getShare().getName());
	}

	private ServerShare getShare() {
		return getIntent().getParcelableExtra(Intents.Extras.SERVER_SHARE);
	}

	private void setUpFragment() {
		Fragments.Operator.at(this).set(buildFragment(), android.R.id.content);
	}

	private Fragment buildFragment() {
		return ServerFilesFragment.newInstance(getShare(), null);
	}

	@Subscribe
	public void onParentDirectorySelected(ParentDirectorySelectedEvent event) {
		tearDownFragment();
	}

	private void tearDownFragment() {
		Fragments.Operator.at(this).removeBackstaced();
	}

	@Subscribe
	public void onFileSelected(FileSelectedEvent event) {
		setUpFile(event.getFile());
	}

	private void setUpFile(ServerFile file) {
		if (isDirectory(file)) {
			setUpFragment(file);
		}
	}

	private boolean isDirectory(ServerFile file) {
		return file.getMime().equals("text/directory");
	}

	private void setUpFragment(ServerFile directory) {
		Fragments.Operator.at(this).replaceBackstacked(buildFragment(directory), android.R.id.content);
	}

	private Fragment buildFragment(ServerFile directory) {
		return ServerFilesFragment.newInstance(getShare(), directory);
	}

	@Override
	protected void onResume() {
		super.onResume();

		BusProvider.getBus().register(this);
	}

	@Override
	protected void onPause() {
		super.onPause();

		BusProvider.getBus().unregister(this);
	}
}
