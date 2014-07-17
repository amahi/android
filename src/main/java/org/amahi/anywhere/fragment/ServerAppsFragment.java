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

package org.amahi.anywhere.fragment;

import android.app.ListFragment;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.otto.Subscribe;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.R;
import org.amahi.anywhere.adapter.ServerAppsAdapter;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.ServerAppsLoadFailedEvent;
import org.amahi.anywhere.bus.ServerAppsLoadedEvent;
import org.amahi.anywhere.bus.ServerConnectionChangedEvent;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.ServerApp;
import org.amahi.anywhere.util.ViewDirector;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class ServerAppsFragment extends ListFragment
{
	private static final class State
	{
		private State() {
		}

		public static final String APPS = "apps";
	}

	@Inject
	ServerClient serverClient;

	@Override
	public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
		return layoutInflater.inflate(R.layout.fragment_server_apps, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		setUpInjections();

		setUpApps(savedInstanceState);
	}

	private void setUpInjections() {
		AmahiApplication.from(getActivity()).inject(this);
	}

	private void setUpApps(Bundle state) {
		setUpAppsAdapter();
		setUpAppsContent(state);
	}

	private void setUpAppsAdapter() {
		setListAdapter(new ServerAppsAdapter(getActivity()));
	}

	private void setUpAppsContent(Bundle state) {
		if (isAppsStateValid(state)) {
			setUpAppsState(state);
		} else {
			setUpAppsContent();
		}
	}

	private boolean isAppsStateValid(Bundle state) {
		return (state != null) && state.containsKey(State.APPS);
	}

	private void setUpAppsState(Bundle state) {
		List<ServerApp> apps = state.getParcelableArrayList(State.APPS);

		setUpAppsContent(apps);

		showAppsContent();
	}

	private void setUpAppsContent(List<ServerApp> apps) {
		getAppsAdapter().replaceWith(apps);
	}

	private ServerAppsAdapter getAppsAdapter() {
		return (ServerAppsAdapter) getListAdapter();
	}

	private void showAppsContent() {
		ViewDirector.of(this, R.id.animator).show(R.id.content);
	}

	private void setUpAppsContent() {
		if (serverClient.isConnected()) {
			serverClient.getApps();
		}
	}

	@Subscribe
	public void onServerConnectionChanged(ServerConnectionChangedEvent event) {
		serverClient.getApps();
	}

	@Subscribe
	public void onAppsLoaded(ServerAppsLoadedEvent event) {
		setUpAppsContent(event.getServerApps());

		showAppsContent();
	}

	@Subscribe
	public void onAppsLoadFailed(ServerAppsLoadFailedEvent event) {
		showAppsError();
	}

	private void showAppsError() {
		ViewDirector.of(this, R.id.animator).show(R.id.error);
	}

	@Override
	public void onResume() {
		super.onResume();

		BusProvider.getBus().register(this);
	}

	@Override
	public void onPause() {
		super.onPause();

		BusProvider.getBus().unregister(this);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		tearDownAppsState(outState);
	}

	private void tearDownAppsState(Bundle state) {
		if (areAppsLoaded()) {
			state.putParcelableArrayList(State.APPS, new ArrayList<Parcelable>(getAppsAdapter().getItems()));
		}
	}

	private boolean areAppsLoaded() {
		return getAppsAdapter() != null;
	}
}
