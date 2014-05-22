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
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.MenuItem;

import com.squareup.otto.Subscribe;

import org.amahi.anywhere.R;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.FileSelectedEvent;
import org.amahi.anywhere.bus.ParentDirectorySelectedEvent;
import org.amahi.anywhere.bus.ShareSelectedEvent;
import org.amahi.anywhere.fragment.NavigationFragment;
import org.amahi.anywhere.fragment.ServerFilesFragment;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerShare;
import org.amahi.anywhere.util.Fragments;
import org.amahi.anywhere.util.Intents;

public class ServerFilesActivity extends Activity
{
	private ActionBarDrawerToggle navigationDrawerToogle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_files);

		setUpNavigationDrawer();
		setUpNavigationFragment();

		showNavigationDrawer();
	}

	private void setUpNavigationDrawer() {
		navigationDrawerToogle = new ActionBarDrawerToggle(
			this,
			getDrawer(),
			R.drawable.ic_drawer,
			R.string.menu_navigation_open,
			R.string.menu_navigation_close);

		getDrawer().setDrawerListener(navigationDrawerToogle);

		getDrawer().setDrawerShadow(R.drawable.bg_shadow_drawer, Gravity.START);
	}

	private DrawerLayout getDrawer() {
		return (DrawerLayout) findViewById(R.id.drawer_content);
	}

	private void setUpNavigationFragment() {
		Fragments.Operator.at(this).set(buildNavigationFragment(), R.id.container_navigation);
	}

	private Fragment buildNavigationFragment() {
		return NavigationFragment.newInstance();
	}

	private void showNavigationDrawer() {
		getDrawer().openDrawer(findViewById(R.id.container_navigation));
	}

	@Subscribe
	public void onShareSelected(ShareSelectedEvent event) {
		setUpShare(event.getShare());

		setUpTitle(event.getShare());

		hideNavigationDrawer();
	}

	private void setUpShare(ServerShare share) {
		setUpFilesFragment(share);
	}

	private void setUpFilesFragment(ServerShare share) {
		Fragments.Operator.at(this).replace(buildFilesFragment(share, null), R.id.container_files);
	}

	private void setUpTitle(ServerShare share) {
		getActionBar().setTitle(share.getName());
	}

	private void hideNavigationDrawer() {
		getDrawer().closeDrawers();
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
		setUpFile(event.getShare(), event.getFile());
	}

	private void setUpFile(ServerShare share, ServerFile file) {
		if (isDirectory(file)) {
			setUpFilesFragment(share, file);
		} else {
			setUpFileActivity(share, file);
		}
	}

	private void setUpFilesFragment(ServerShare share, ServerFile directory) {
		Fragments.Operator.at(this).replaceBackstacked(buildFilesFragment(share, directory), R.id.container_files);
	}

	private boolean isDirectory(ServerFile file) {
		return file.getMime().equals("text/directory");
	}

	private Fragment buildFilesFragment(ServerShare share, ServerFile directory) {
		return ServerFilesFragment.newInstance(share, directory);
	}

	private void setUpFileActivity(ServerShare share, ServerFile file) {
		Intent intent = Intents.Builder.with(this).buildServerFileIntent(share, file);
		startActivity(intent);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		navigationDrawerToogle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration configuration) {
		super.onConfigurationChanged(configuration);

		navigationDrawerToogle.onConfigurationChanged(configuration);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem) {
		if (navigationDrawerToogle.onOptionsItemSelected(menuItem)) {
			return true;
		}

		return super.onOptionsItemSelected(menuItem);
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
