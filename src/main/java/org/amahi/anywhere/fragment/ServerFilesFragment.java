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
import org.amahi.anywhere.adapter.ServerFilesAdapter;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.ServerFilesLoadedEvent;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerShare;
import org.amahi.anywhere.util.Fragments;
import org.amahi.anywhere.util.Intents;

import java.util.List;

import javax.inject.Inject;

public class ServerFilesFragment extends ListFragment
{
	public static ServerFilesFragment newInstance(ServerShare share, ServerFile directory) {
		ServerFilesFragment fragment = new ServerFilesFragment();

		fragment.setArguments(buildArguments(share, directory));

		return fragment;
	}

	private static Bundle buildArguments(ServerShare share, ServerFile directory) {
		Bundle arguments = new Bundle();

		arguments.putParcelable(Fragments.Arguments.SERVER_SHARE, share);
		arguments.putParcelable(Fragments.Arguments.SERVER_FILE, directory);

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

		setUpFiles();
	}

	private void setUpInjections() {
		AmahiApplication.from(getActivity()).inject(this);
	}

	private void setUpFiles() {
		setUpFilesAdapter();
		setUpFilesContent();
	}

	private void setUpFilesAdapter() {
		setListAdapter(new ServerFilesAdapter(getActivity()));
	}

	private void setUpFilesContent() {
		serverClient.getFiles(getShare(), getDirectory());
	}

	private ServerShare getShare() {
		return getArguments().getParcelable(Fragments.Arguments.SERVER_SHARE);
	}

	private ServerFile getDirectory() {
		return getArguments().getParcelable(Fragments.Arguments.SERVER_FILE);
	}

	@Subscribe
	public void onFilesLoaded(ServerFilesLoadedEvent event) {
		setUpFilesContent(event.getServerFiles());

		showFilesContent();
	}

	private void setUpFilesContent(List<ServerFile> files) {
		getFilesAdapter().replaceWith(files);
	}

	private ServerFilesAdapter getFilesAdapter() {
		return (ServerFilesAdapter) getListAdapter();
	}

	private void showFilesContent() {
		ViewAnimator animator = (ViewAnimator) getView().findViewById(R.id.animator);
		animator.setDisplayedChild(animator.indexOfChild(getView().findViewById(R.id.content)));
	}

	@Override
	public void onListItemClick(ListView listView, View view, int position, long id) {
		super.onListItemClick(listView, view, position, id);

		ServerFile file = getFilesAdapter().getItem(position);

		if (!file.getMime().equals("text/directory")) {
			return;
		}

		startServerFilesActivity(file);
	}

	private void startServerFilesActivity(ServerFile directory) {
		Intent intent = Intents.Builder.with(getActivity()).buildServerFilesIntent(getShare(), directory);
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
