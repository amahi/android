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

import android.app.ActionBar;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.squareup.otto.Subscribe;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.R;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.FileDownloadedEvent;
import org.amahi.anywhere.bus.FileSelectedEvent;
import org.amahi.anywhere.bus.ShareSelectedEvent;
import org.amahi.anywhere.fragment.FileDownloadingFragment;
import org.amahi.anywhere.fragment.GooglePlaySearchFragment;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerShare;
import org.amahi.anywhere.task.FileDownloadingTask;
import org.amahi.anywhere.util.Android;
import org.amahi.anywhere.util.Fragments;
import org.amahi.anywhere.util.Intents;
import org.amahi.anywhere.util.Mimes;

import javax.inject.Inject;

public class ServerFilesActivity extends Activity implements DrawerLayout.DrawerListener
{
	@Inject
	ServerClient serverClient;

	private ActionBarDrawerToggle navigationDrawerToggle;

	private ServerShare selectedShare;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_server_files);

		setUpInjections();

		setUpHomeNavigation();

		setUpNavigation();
	}

	private void setUpInjections() {
		AmahiApplication.from(this).inject(this);
	}

	private void setUpHomeNavigation() {
		ActionBar actionBar = getActionBar();

		actionBar.setHomeButtonEnabled(isNavigationDrawerAvailable());
		actionBar.setDisplayHomeAsUpEnabled(isNavigationDrawerAvailable());
	}

	private boolean isNavigationDrawerAvailable() {
		return !Android.isTablet(this);
	}

	private void setUpNavigation() {
		if (isNavigationDrawerAvailable()) {
			setUpNavigationDrawer();
		}

		setUpNavigationFragment();

		if (isNavigationDrawerAvailable()) {
			showNavigationDrawer();
		}
	}

	private void setUpNavigationDrawer() {
		this.navigationDrawerToggle = buildNavigationDrawerToggle();

		getDrawer().setDrawerListener(this);
		getDrawer().setDrawerShadow(R.drawable.bg_shadow_drawer, Gravity.START);
	}

	private ActionBarDrawerToggle buildNavigationDrawerToggle() {
		return new ActionBarDrawerToggle(
			this,
			getDrawer(),
			R.drawable.ic_drawer,
			R.string.menu_navigation_open,
			R.string.menu_navigation_close);
	}

	private DrawerLayout getDrawer() {
		return (DrawerLayout) findViewById(R.id.drawer_content);
	}

	@Override
	public void onDrawerOpened(View drawer) {
		navigationDrawerToggle.onDrawerOpened(drawer);

		setUpTitle(getString(R.string.application_name));
		setUpMenu();
	}

	private void setUpTitle(String title) {
		getActionBar().setTitle(title);
	}

	private void setUpMenu() {
		invalidateOptionsMenu();
	}

	@Override
	public void onDrawerClosed(View drawer) {
		navigationDrawerToggle.onDrawerClosed(drawer);

		setUpTitle(selectedShare);
		setUpMenu();
	}

	@Override
	public void onDrawerSlide(View drawer, float slideOffset) {
		navigationDrawerToggle.onDrawerSlide(drawer, slideOffset);
	}

	@Override
	public void onDrawerStateChanged(int state) {
		navigationDrawerToggle.onDrawerStateChanged(state);
	}

	private void setUpNavigationFragment() {
		Fragments.Operator.at(this).set(buildNavigationFragment(), R.id.container_navigation);
	}

	private Fragment buildNavigationFragment() {
		return Fragments.Builder.buildNavigationFragment();
	}

	private void showNavigationDrawer() {
		getDrawer().openDrawer(findViewById(R.id.container_navigation));
	}

	@Subscribe
	public void onShareSelected(ShareSelectedEvent event) {
		setUpShare(event.getShare());

		if (isNavigationDrawerAvailable()) {
			hideNavigationDrawer();
		}
	}

	private void setUpShare(ServerShare share) {
		this.selectedShare = share;

		setUpFilesFragment(share);
	}

	private void setUpFilesFragment(ServerShare share) {
		Fragments.Operator.at(this).replace(buildFilesFragment(share, null), R.id.container_files);
	}

	private void setUpTitle(ServerShare share) {
		if (share != null) {
			getActionBar().setTitle(share.getName());
		}
	}

	private void hideNavigationDrawer() {
		getDrawer().closeDrawers();
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

	private boolean isDirectory(ServerFile file) {
		return Mimes.match(file.getMime()) == Mimes.Type.DIRECTORY;
	}

	private void setUpFilesFragment(ServerShare share, ServerFile directory) {
		Fragments.Operator.at(this).replaceBackstacked(buildFilesFragment(share, directory), R.id.container_files);
	}

	private Fragment buildFilesFragment(ServerShare share, ServerFile directory) {
		return Fragments.Builder.buildServerFilesFragment(share, directory);
	}

	private void setUpFileActivity(ServerShare share, ServerFile file) {
		if (Intents.Builder.with(this).isServerFileSupported(file)) {
			startFileActivity(share, file);
			return;
		}

		if (Intents.Builder.with(this).isServerFileShareSupported(file)) {
			startFileShareActivity(share, file);
			return;
		}

		showGooglePlaySearchFragment(file);
	}

	private void startFileActivity(ServerShare share, ServerFile file) {
		Intent intent = Intents.Builder.with(this).buildServerFileIntent(share, file);
		startActivity(intent);
	}

	private Uri getFileUri(ServerShare share, ServerFile file) {
		return serverClient.getFileUri(share, file);
	}

	private void startFileShareActivity(ServerShare share, ServerFile file) {
		showFileDownloadingFragment();

		startFileDownloading(share, file);
	}

	private void showFileDownloadingFragment() {
		DialogFragment fragment = new FileDownloadingFragment();
		fragment.show(getFragmentManager(), FileDownloadingFragment.TAG);
	}

	private void startFileDownloading(ServerShare share, ServerFile file) {
		FileDownloadingTask.execute(this, file, getFileUri(share, file));
	}

	@Subscribe
	public void onFileDownloaded(FileDownloadedEvent event) {
		hideFileDownloadingFragment();

		startFileShareActivity(event.getFile(), event.getFileUri());
	}

	private void hideFileDownloadingFragment() {
		DialogFragment fragment = (DialogFragment) Fragments.Operator.at(this).find(FileDownloadingFragment.TAG);
		fragment.dismiss();
	}

	private void startFileShareActivity(ServerFile file, Uri fileUri) {
		Intent intent = Intents.Builder.with(this).buildServerFileShareIntent(file, fileUri);
		startActivity(intent);
	}

	private void showGooglePlaySearchFragment(ServerFile file) {
		GooglePlaySearchFragment fragment = GooglePlaySearchFragment.newInstance(file);
		fragment.show(getFragmentManager(), GooglePlaySearchFragment.TAG);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		if (isNavigationDrawerAvailable()) {
			navigationDrawerToggle.syncState();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.action_bar_navigation, menu);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (isNavigationDrawerAvailable()) {
			setUpMenu(menu);
		}

		return super.onPrepareOptionsMenu(menu);
	}

	private void setUpMenu(Menu menu) {
		MenuItem sortMenuItem = menu.findItem(R.id.menu_sort);
		MenuItem connectionMenuItem = menu.findItem(R.id.menu_connection);

		if (sortMenuItem != null) {
			sortMenuItem.setVisible(!isNavigationDrawerOpen());
		}

		if (connectionMenuItem != null) {
			connectionMenuItem.setVisible(!isNavigationDrawerOpen() && !isConnectionLocal());
		}
	}

	private boolean isNavigationDrawerOpen() {
		return getDrawer().isDrawerOpen(findViewById(R.id.container_navigation));
	}

	private boolean isConnectionLocal() {
		return serverClient.isConnected() && serverClient.isConnectionLocal();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem) {
		if (isNavigationDrawerAvailable() && navigationDrawerToggle.onOptionsItemSelected(menuItem)) {
			return true;
		}

		return super.onOptionsItemSelected(menuItem);
	}

	@Override
	public void onConfigurationChanged(Configuration configuration) {
		super.onConfigurationChanged(configuration);

		if (isNavigationDrawerAvailable()) {
			navigationDrawerToggle.onConfigurationChanged(configuration);
		}
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
