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

import android.app.UiModeManager;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.squareup.otto.Subscribe;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.R;
import org.amahi.anywhere.bus.AppSelectedEvent;
import org.amahi.anywhere.bus.AppsSelectedEvent;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.SettingsSelectedEvent;
import org.amahi.anywhere.bus.ShareSelectedEvent;
import org.amahi.anywhere.bus.SharesSelectedEvent;
import org.amahi.anywhere.fragment.NavigationFragment;
import org.amahi.anywhere.fragment.ServerAppsFragment;
import org.amahi.anywhere.fragment.ServerSharesFragment;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.Server;
import org.amahi.anywhere.server.model.ServerApp;
import org.amahi.anywhere.server.model.ServerShare;
import org.amahi.anywhere.tv.activity.IntroActivity;
import org.amahi.anywhere.tv.activity.MainTVActivity;
import org.amahi.anywhere.util.Android;
import org.amahi.anywhere.util.Fragments;
import org.amahi.anywhere.util.Intents;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Navigation activity. This is an entry point of the application. Shows navigation between
 * main application sections (shares, apps) and shares and apps lists itself. On phones the activity
 * uses the navigation drawer, on tablets drawer is always visible.
 * The navigation itself is done via {@link org.amahi.anywhere.fragment.NavigationFragment},
 * {@link org.amahi.anywhere.fragment.ServerSharesFragment} and {@link org.amahi.anywhere.fragment.ServerAppsFragment}.
 */
public class NavigationActivity extends AppCompatActivity implements DrawerLayout.DrawerListener,
		ServerAppsFragment.passAppsEvent,
		ServerSharesFragment.passShareEvent,
		NavigationFragment.passServerEvent
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
		setContentView(R.layout.activity_navigation);

		if(isATV()){
			Boolean isFirstRun = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getBoolean("isFirstRun", true);
			if (isFirstRun) {
				startActivity(new Intent(NavigationActivity.this, IntroActivity.class));
				getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit().putBoolean("isFirstRun", false).apply();
			}

			FrameLayout containerContent = (FrameLayout)findViewById(R.id.container_content);
			FrameLayout containerNavigation = (FrameLayout)findViewById(R.id.container_navigation);
			RelativeLayout tvLoading = (RelativeLayout)findViewById(R.id.tv_loading);
			containerContent.setVisibility(View.INVISIBLE);
			containerNavigation.setVisibility(View.INVISIBLE);
			tvLoading.setVisibility(View.VISIBLE);
			getSupportActionBar().hide();
			setUpNavigationFragment();
			Fragments.Operator.at(this).replace(buildAppsFragment(), R.id.container_apps_dummy);
			setUpShares();
		}

		setUpInjections();

		setUpHomeNavigation();

		setUpNavigation(savedInstanceState);
	}

	private void setUpInjections() {
		AmahiApplication.from(this).inject(this);
	}

	private void setUpHomeNavigation() {
		getSupportActionBar().setHomeButtonEnabled(isNavigationDrawerAvailable());
		getSupportActionBar().setDisplayHomeAsUpEnabled(isNavigationDrawerAvailable());
		getSupportActionBar().setIcon(R.drawable.ic_launcher);
	}

	private boolean isNavigationDrawerAvailable() {
		return !Android.isTablet(this);
	}

	private void setUpNavigation(Bundle state) {
		if (isNavigationDrawerAvailable()) {
			setUpNavigationDrawer();
		}

		if(!isATV())setUpNavigationFragment();

		if (isNavigationDrawerAvailable() && isNavigationDrawerRequired(state)) {
			showNavigationDrawer();
		}

		setUpNavigationTitle(state);
	}

	private void setUpNavigationDrawer() {
		this.navigationDrawerToggle = buildNavigationDrawerToggle();

		getDrawer().addDrawerListener(this);
		getDrawer().setDrawerShadow(R.drawable.bg_shadow_drawer, Gravity.START);
	}

	private ActionBarDrawerToggle buildNavigationDrawerToggle() {
		return new ActionBarDrawerToggle(this,getDrawer(),R.string.menu_navigation_open,R.string.menu_navigation_close);
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
		if (isNavigationDrawerAvailable()) {
			getSupportActionBar().setTitle(title);
		}
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

		if (isNavigationDrawerAvailable() && !isNavigationDrawerOpen()) {
			setUpTitle();
		}
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
		if(!isATV())
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

	protected boolean isATV() {
		UiModeManager uiModeManager = (UiModeManager) getSystemService(UI_MODE_SERVICE);
		return (uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION);
	}

	@Override
	public void passShare(List<ServerShare> serverShares) {
		if(isATV()){

			Intent tvIntent = new Intent(this,MainTVActivity.class);


			if(serverApps!=null){
				ArrayList<ServerApp> serverAppsArrayList = new ArrayList<>(serverApps);
				tvIntent.putParcelableArrayListExtra(getString(R.string.intent_apps),serverAppsArrayList);
			}

			if(serverShares!=null) {
				ArrayList<ServerShare> serverShareArrayList = new ArrayList<>(serverShares);
				tvIntent.putParcelableArrayListExtra(getString(R.string.intent_shares),serverShareArrayList);
			}

			if(servers!=null){
				ArrayList<Server> serverArrayList = new ArrayList<>(servers);
				tvIntent.putParcelableArrayListExtra(getString(R.string.intent_servers),serverArrayList);
			}
			startActivity(tvIntent);
		}
	}

	List<ServerApp> serverApps;
	List<Server> servers;

	@Override
	public void passApps(List<ServerApp> serverApps) {
		if(isATV())
		this.serverApps = serverApps;
	}

	@Override
	public void passServer(List<Server> serverList) {
		if(isATV())
		servers = serverList;
	}
}