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

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.R;
import org.amahi.anywhere.bus.AppSelectedEvent;
import org.amahi.anywhere.bus.AppsSelectedEvent;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.OfflineFilesSelectedEvent;
import org.amahi.anywhere.bus.RecentFilesSelectedEvent;
import org.amahi.anywhere.bus.SettingsSelectedEvent;
import org.amahi.anywhere.bus.ShareSelectedEvent;
import org.amahi.anywhere.bus.SharesSelectedEvent;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.ServerApp;
import org.amahi.anywhere.server.model.ServerShare;
import org.amahi.anywhere.tv.activity.IntroActivity;
import org.amahi.anywhere.util.Android;
import org.amahi.anywhere.util.CheckTV;
import org.amahi.anywhere.util.Fragments;
import org.amahi.anywhere.util.Intents;
import org.amahi.anywhere.util.LocaleHelper;
import org.amahi.anywhere.util.Preferences;

import javax.inject.Inject;

/**
 * Navigation activity. This is an entry point of the application. Shows navigation between
 * main application sections (shares, apps) and shares and apps lists itself. On phones the activity
 * uses the navigation drawer, on tablets drawer is always visible.
 * The navigation itself is done via {@link org.amahi.anywhere.fragment.NavigationFragment},
 * {@link org.amahi.anywhere.fragment.ServerSharesFragment} and {@link org.amahi.anywhere.fragment.ServerAppsFragment}.
 */
public class NavigationActivity extends AppCompatActivity implements DrawerLayout.DrawerListener {
    @Inject
    ServerClient serverClient;
    private ActionBarDrawerToggle navigationDrawerToggle;
    private String navigationTitle;
    final private int SETTINGS_ACTION = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (AmahiApplication.getInstance().isLightThemeEnabled()) {
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        if (CheckTV.isATV(this)) {
            handleTvFirstRun();
            showTvLoading();
        }

        setUpInjections();

        setUpHomeNavigation();

        setUpNavigation(savedInstanceState);
    }

    private void handleTvFirstRun() {
        Boolean isFirstRun = Preferences.getFirstRun(this);

        if (isFirstRun) {
            startActivity(new Intent(this, IntroActivity.class));
            Preferences.setFirstRun(this);
        }
    }

    private void showTvLoading() {

        inflateStubs();

        hideMobileContainers();

        hideActionBar();

        setUpNavigationFragment();

        setUpShares();
    }

    private void inflateStubs() {
        ViewStub tvLoadingStub = findViewById(R.id.view_stub_tv_loading);
        tvLoadingStub.inflate();
    }

    private void hideMobileContainers() {
        RelativeLayout tvLoading = findViewById(R.id.tv_loading);

        getContainerContent().setVisibility(View.INVISIBLE);

        getContainerNavigation().setVisibility(View.INVISIBLE);

        displayTvLoading(tvLoading);
    }

    private FrameLayout getContainerContent() {
        return findViewById(R.id.container_content);
    }

    private FrameLayout getContainerNavigation() {
        return findViewById(R.id.container_navigation);
    }

    private void displayTvLoading(RelativeLayout tvLoading) {
        tvLoading.setVisibility(View.VISIBLE);
    }

    private void hideActionBar() {
        if (getSupportActionBar()!=null) getSupportActionBar().hide();
    }

    private void setUpInjections() {
        AmahiApplication.from(this).inject(this);
    }

    private void setUpHomeNavigation() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(isNavigationDrawerAvailable());
        getSupportActionBar().setDisplayHomeAsUpEnabled(isNavigationDrawerAvailable());
    }

    private boolean isNavigationDrawerAvailable() {
        return !Android.isTablet(this);
    }

    private void setUpNavigation(Bundle state) {
        if (isNavigationDrawerAvailable()) {
            setUpNavigationDrawer();
        }

        if (!CheckTV.isATV(this)) setUpNavigationFragment();

        if (isNavigationDrawerAvailable() && isNavigationDrawerRequired(state)) {
            this.navigationTitle = getString(R.string.title_shares);
            setUpTitle();
            setUpShares();
        }

    }

    private void setUpNavigationDrawer() {
        this.navigationDrawerToggle = buildNavigationDrawerToggle();

        getDrawer().addDrawerListener(this);
        getDrawer().setDrawerShadow(R.drawable.bg_shadow_drawer, GravityCompat.START);
    }

    private ActionBarDrawerToggle buildNavigationDrawerToggle() {
        return new ActionBarDrawerToggle(this, getDrawer(), R.string.menu_navigation_open, R.string.menu_navigation_close);
    }

    private DrawerLayout getDrawer() {
        return findViewById(R.id.drawer_content);
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
        if (!CheckTV.isATV(this))
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
    public void onOfflineFilesSelected(OfflineFilesSelectedEvent event) {

        showOfflineFiles();

        if (isNavigationDrawerAvailable()) {
            hideNavigationDrawer();
        }
    }

    private void showOfflineFiles() {
        Intent intent = Intents.Builder.with(this).buildServerFilesActivityForOfflineFiles();
        startActivity(intent);
    }

    @Subscribe
    public void onRecentFilesSelected(RecentFilesSelectedEvent event) {

        showRecentFiles();

        if (isNavigationDrawerAvailable()) {
            hideNavigationDrawer();
        }
    }

    private void showRecentFiles() {
        Intent intent = Intents.Builder.with(this).buildRecentFilesActivity();
        startActivity(intent);
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
        startActivityForResult(intent, SETTINGS_ACTION);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == SETTINGS_ACTION) {
            if (resultCode == SettingsActivity.RESULT_THEME_UPDATED) {
                finish();
                startActivity(getIntent());
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
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

    private static final class State {
        public static final String NAVIGATION_TITLE = "navigation_title";
        public static final String NAVIGATION_DRAWER_VISIBLE = "navigation_drawer_visible";

        private State() {
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }
}
