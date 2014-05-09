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
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ViewAnimator;

import com.squareup.otto.Subscribe;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.R;
import org.amahi.anywhere.adapter.ServersAdapter;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.ServersLoadedEvent;
import org.amahi.anywhere.server.client.AmahiClient;
import org.amahi.anywhere.server.model.Server;
import org.amahi.anywhere.util.Intents;

import java.util.List;

import javax.inject.Inject;

public class ServersFragment extends ListFragment
{
	public static ServersFragment newInstance() {
		return new ServersFragment();
	}

	@Inject
	AmahiClient amahiClient;

	@Override
	public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
		return layoutInflater.inflate(R.layout.fragment_list, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		setUpInjections();

		setUpServers();
	}

	private void setUpInjections() {
		AmahiApplication.from(getActivity()).inject(this);
	}

	private void setUpServers() {
		setUpServersAdapter();
		setUpServersContent();
	}

	private void setUpServersAdapter() {
		setListAdapter(new ServersAdapter(getActivity()));
	}

	private void setUpServersContent() {
		amahiClient.getServers("TOKEN");
	}

	@Subscribe
	public void onServersLoaded(ServersLoadedEvent event) {
		setUpServersContent(event.getServers());

		showServersContent();
	}

	private void setUpServersContent(List<Server> servers) {
		getServersAdapter().replaceWith(servers);
	}

	private ServersAdapter getServersAdapter() {
		return (ServersAdapter) getListAdapter();
	}

	private void showServersContent() {
		ViewAnimator animator = (ViewAnimator) getView().findViewById(R.id.animator);
		animator.setDisplayedChild(animator.indexOfChild(getView().findViewById(R.id.content)));
	}

	@Override
	public void onListItemClick(ListView listView, View view, int position, long id) {
		super.onListItemClick(listView, view, position, id);

		Server server = getServersAdapter().getItem(position);

		startServerSharesActivity(server);
	}

	private void startServerSharesActivity(Server server) {
		Intent intent = Intents.Builder.with(getActivity()).buildServerSharesIntent(server);
		startActivity(intent);
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
}