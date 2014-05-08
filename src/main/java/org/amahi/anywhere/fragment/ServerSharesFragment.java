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
import org.amahi.anywhere.adapter.ServerSharesAdapter;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.ServerConnectedEvent;
import org.amahi.anywhere.bus.ServerSharesLoadedEvent;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.Server;
import org.amahi.anywhere.server.model.ServerShare;
import org.amahi.anywhere.util.Fragments;
import org.amahi.anywhere.util.Intents;

import java.util.List;

import javax.inject.Inject;

public class ServerSharesFragment extends ListFragment
{
	public static ServerSharesFragment newInstance(Server server) {
		ServerSharesFragment fragment = new ServerSharesFragment();

		fragment.setArguments(buildArguments(server));

		return fragment;
	}

	private static Bundle buildArguments(Server server) {
		Bundle arguments = new Bundle();

		arguments.putParcelable(Fragments.Arguments.SERVER, server);

		return arguments;
	}

	@Inject
	ServerClient serverClient;

	@Override
	public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
		return layoutInflater.inflate(R.layout.fragment_list, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		setUpInjections();

		setUpShares();
	}

	private void setUpInjections() {
		AmahiApplication.from(getActivity()).inject(this);
	}

	private void setUpShares() {
		setUpSharesAdapter();

		setUpServerConnection();
	}

	private void setUpSharesAdapter() {
		setListAdapter(new ServerSharesAdapter(getActivity()));
	}

	private void setUpServerConnection() {
		if (!serverClient.isConnected(getServer())) {
			serverClient.connect(getServer());
		} else {
			setUpSharesContent();
		}
	}

	private Server getServer() {
		return getArguments().getParcelable(Fragments.Arguments.SERVER);
	}

	@Subscribe
	public void onServerConnected(ServerConnectedEvent event) {
		setUpSharesContent();
	}

	private void setUpSharesContent() {
		serverClient.getShares();
	}

	@Subscribe
	public void onSharesLoaded(ServerSharesLoadedEvent event) {
		setUpSharesContent(event.getServerShares());

		showSharesContent();
	}

	private void setUpSharesContent(List<ServerShare> shares) {
		getSharesAdapter().replaceWith(shares);
	}

	private ServerSharesAdapter getSharesAdapter() {
		return (ServerSharesAdapter) getListAdapter();
	}

	private void showSharesContent() {
		ViewAnimator animator = (ViewAnimator) getView().findViewById(R.id.animator);
		animator.setDisplayedChild(animator.indexOfChild(getView().findViewById(R.id.content)));
	}

	@Override
	public void onListItemClick(ListView listView, View view, int position, long id) {
		super.onListItemClick(listView, view, position, id);

		ServerShare share = getSharesAdapter().getItem(position);

		startServerFilesActivity(share);
	}

	private void startServerFilesActivity(ServerShare share) {
		Intent intent = Intents.Builder.with(getActivity()).buildServerFilesIntent(share, null);
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
