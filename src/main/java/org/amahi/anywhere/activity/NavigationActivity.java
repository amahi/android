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
import android.app.Fragment;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;

import com.squareup.otto.Subscribe;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.R;
import org.amahi.anywhere.bus.AppSelectedEvent;
import org.amahi.anywhere.bus.AppsSelectedEvent;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.SettingsSelectedEvent;
import org.amahi.anywhere.bus.ShareSelectedEvent;
import org.amahi.anywhere.bus.SharesSelectedEvent;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.ServerApp;
import org.amahi.anywhere.server.model.ServerShare;
import org.amahi.anywhere.util.Android;
import org.amahi.anywhere.util.Fragments;
import org.amahi.anywhere.util.Intents;

import javax.inject.Inject;

public class NavigationActivity extends Activity implements DrawerLayout.DrawerListener
{
	private static final class State
	{
		private State() {
		}

		public static final String NAVIGATION_TITLE = "navigation_title";
		public static final String NAVIGATION_DRAWER_VISIBLE = "navigation_drawer_visible";
	}

	@Inject
	ServerClient serverClient;

	private ActionBarDrawerToggle navigationDrawerToggle;
	private String navigationTitle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_server);

		setUpInjections();

		setUpHomeNavigation();

		setUpNavigation(savedInstanceState);
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

	private void setUpNavigation(Bundle state) {
		if (isNavigationDrawerAvailable()) {
			setUpNavigationDrawer();
		}

		setUpNavigationTitle(state);
		setUpNavigationFragment();

		if (isNavigationDrawerAvailable() && isNavigationDrawerRequired(state)) {
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
	}

	private void setUpTitle(String title) {
		getActionBar().setTitle(title);
	}

	@Override
	public void onDrawerClosed(View drawer) {
		navigationDrawerToggle.onDrawerClosed(drawer);

		setUpTitle();
	}

	@Override
	public void onDrawerSlide(View drawer, float slideOffset) {
		navigationDrawerToggle.onDrawerSlide(drawer, slideOffset);
	}

	@Override
	public void onDrawerStateChanged(int state) {
		navigationDrawerToggle.onDrawerStateChanged(state);
	}

	private void setUpNavigationTitle(Bundle state) {
		this.navigationTitle = getNavigationTitle(state);

		setUpTitle();
	}

	private String getNavigationTitle(Bundle state) {
		if (isNavigationStateValid(state)) {
			return state.getString(State.NAVIGATION_TITLE);
		} else {
			return getString(R.string.application_name);
		}
	}

	private boolean isNavigationStateValid(Bundle state) {
		return (state != null) && state.containsKey(State.NAVIGATION_TITLE);
	}

	private void setUpNavigationFragment() {
		Fragments.Operator.at(this).set(buildNavigationFragment(), R.id.container_navigation);
	}

	private Fragment buildNavigationFragment() {
		return Fragments.Builder.buildNavigationFragment();
	}

	private boolean isNavigationDrawerRequired(Bundle state) {
		return (state == null) || state.getBoolean(State.NAVIGATION_DRAWER_VISIBLE);
	}

	private void showNavigationDrawer() {
		getDrawer().openDrawer(findViewById(R.id.container_navigation));
	}

	@Subscribe
	public void onSharesSelected(SharesSelectedEvent event) {
		this.navigationTitle = getString(R.string.title_shares);

		if (isNavigationDrawerAvailable()) {
			setUpTitle();
		}

		setUpShares();

		if (isNavigationDrawerAvailable()) {
			hideNavigationDrawer();
		}
	}

	private void setUpTitle() {
		setUpTitle(navigationTitle);
	}

	private void setUpShares() {
		Fragments.Operator.at(this).replace(buildSharesFragment(), R.id.container_content);
	}

	private Fragment buildSharesFragment() {
		return Fragments.Builder.buildServerSharesFragment();
	}

	private void hideNavigationDrawer() {
		getDrawer().closeDrawers();
	}

	@Subscribe
	public void onAppsSelected(AppsSelectedEvent event) {
		this.navigationTitle = getString(R.string.title_apps);

		if (isNavigationDrawerAvailable()) {
			setUpTitle();
		}

		setUpApps();

		if (isNavigationDrawerAvailable()) {
			hideNavigationDrawer();
		}
	}

	private void setUpApps() {
		Fragments.Operator.at(this).replace(buildAppsFragment(), R.id.container_content);
	}

	private Fragment buildAppsFragment() {
		return Fragments.Builder.buildServerAppsFragment();
	}

	@Subscribe
	public void onShareSelected(ShareSelectedEvent event) {
		setUpShare(event.getShare());
	}

	private void setUpShare(ServerShare share) {
		Intent intent = Intents.Builder.with(this).buildServerFilesActivity(share);
		startActivity(intent);
	}

	@Subscribe
	public void onAppSelected(AppSelectedEvent event) {
		setUpApp(event.getApp());
	}

	private void setUpApp(ServerApp app) {
		Intent intent = Intents.Builder.with(this).buildServerAppAcitivity(app);
		startActivity(intent);
	}

	@Subscribe
	public void onSettingsSelected(SettingsSelectedEvent event) {
		setUpSettings();
	}

	private void setUpSettings() {
		Intent intent = Intents.Builder.with(this).buildSettingsIntent();
		startActivity(intent);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		if (isNavigationDrawerAvailable()) {
			navigationDrawerToggle.syncState();
		}
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
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		tearDownNavigationState(outState);
	}

	private void tearDownNavigationState(Bundle state) {
		state.putString(State.NAVIGATION_TITLE, navigationTitle);
		state.putBoolean(State.NAVIGATION_DRAWER_VISIBLE, isNavigationDrawerAvailable() && isNavigationDrawerOpen());
	}

	private boolean isNavigationDrawerOpen() {
		return getDrawer().isDrawerOpen(findViewById(R.id.container_navigation));
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
