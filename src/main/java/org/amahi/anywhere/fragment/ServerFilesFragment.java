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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ViewAnimator;

import com.squareup.otto.Subscribe;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.R;
import org.amahi.anywhere.adapter.ServerFilesAdapter;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.FileSelectedEvent;
import org.amahi.anywhere.bus.ServerFilesLoadedEvent;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerShare;
import org.amahi.anywhere.util.Fragments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

public class ServerFilesFragment extends ListFragment
{
	private static enum FilesSort
	{
		NAME, MODIFICATION_TIME
	}

	private FilesSort filesSort = FilesSort.MODIFICATION_TIME;

	@Inject
	ServerClient serverClient;

	@Override
	public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
		return layoutInflater.inflate(R.layout.fragment_server_files, container, false);
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
		setUpFilesMenu();
		setUpFilesAdapter();
		setUpFilesContent();
	}

	private void setUpFilesMenu() {
		setHasOptionsMenu(true);
	}

	private void setUpFilesAdapter() {
		setListAdapter(new ServerFilesAdapter(getActivity()));
	}

	private void setUpFilesContent() {
		if (!isDirectoryAvailable()) {
			serverClient.getFiles(getShare());
		} else {
			serverClient.getFiles(getShare(), getDirectory());
		}
	}

	private boolean isDirectoryAvailable() {
		return getDirectory() != null;
	}

	private ServerFile getDirectory() {
		return getArguments().getParcelable(Fragments.Arguments.SERVER_FILE);
	}

	private ServerShare getShare() {
		return getArguments().getParcelable(Fragments.Arguments.SERVER_SHARE);
	}

	@Subscribe
	public void onFilesLoaded(ServerFilesLoadedEvent event) {
		setUpFilesContent(event.getServerFiles());

		showFilesContent();
	}

	private void setUpFilesContent(List<ServerFile> files) {
		getFilesAdapter().replaceWith(sortFiles(files));
	}

	private ServerFilesAdapter getFilesAdapter() {
		return (ServerFilesAdapter) getListAdapter();
	}

	private List<ServerFile> sortFiles(List<ServerFile> files) {
		List<ServerFile> sortedFiles = new ArrayList<ServerFile>(files);

		Collections.sort(sortedFiles, getFilesComparator());

		return sortedFiles;
	}

	private Comparator<ServerFile> getFilesComparator() {
		switch (filesSort) {
			case NAME:
				return new FileNameComparator();

			case MODIFICATION_TIME:
				return new FileModificationTimeComparator();

			default:
				return null;
		}
	}

	private void showFilesContent() {
		ViewAnimator animator = (ViewAnimator) getView().findViewById(R.id.animator);
		animator.setDisplayedChild(animator.indexOfChild(getView().findViewById(R.id.content)));
	}

	@Override
	public void onListItemClick(ListView listView, View view, int position, long id) {
		super.onListItemClick(listView, view, position, id);

		BusProvider.getBus().post(new FileSelectedEvent(getShare(), getFile(position)));
	}

	private ServerFile getFile(int position) {
		return getFilesAdapter().getItem(position);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
		super.onCreateOptionsMenu(menu, menuInflater);

		menuInflater.inflate(R.menu.action_bar_server_files, menu);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		setUpFilesContentSortIcon(menu.findItem(R.id.menu_sort));
	}

	private void setUpFilesContentSortIcon(MenuItem menuItem) {
		switch (filesSort) {
			case NAME:
				menuItem.setIcon(R.drawable.ic_menu_sort_name);
				break;

			case MODIFICATION_TIME:
				menuItem.setIcon(R.drawable.ic_menu_sort_modification_time);
				break;

			default:
				break;
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem) {
		switch (menuItem.getItemId()) {
			case R.id.menu_sort:
				setUpFilesContentSortSwitched();
				setUpFilesContentSortIcon(menuItem);
				return true;

			default:
				return super.onOptionsItemSelected(menuItem);
		}
	}

	private void setUpFilesContentSortSwitched() {
		switch (filesSort) {
			case NAME:
				filesSort = FilesSort.MODIFICATION_TIME;
				break;

			case MODIFICATION_TIME:
				filesSort = FilesSort.NAME;
				break;

			default:
				break;
		}

		setUpFilesContentSort();
	}

	private void setUpFilesContentSort() {
		getFilesAdapter().replaceWith(sortFiles(getFilesAdapter().getItems()));
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

	private static final class FileNameComparator implements Comparator<ServerFile>
	{
		@Override
		public int compare(ServerFile firstFile, ServerFile secondFile) {
			return firstFile.getName().compareTo(secondFile.getName());
		}
	}

	private static final class FileModificationTimeComparator implements Comparator<ServerFile>
	{
		@Override
		public int compare(ServerFile firstFile, ServerFile secondFile) {
			return -firstFile.getModificationTime().compareTo(secondFile.getModificationTime());
		}
	}
}
