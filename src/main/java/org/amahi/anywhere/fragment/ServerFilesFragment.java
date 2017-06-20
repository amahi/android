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

import android.Manifest;
import android.app.SearchManager;
import android.app.UiModeManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.SearchView;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.R;
import org.amahi.anywhere.activity.ServerFilesActivity;
import org.amahi.anywhere.adapter.FilesFilterBaseAdapter;
import org.amahi.anywhere.adapter.ServerFilesAdapter;
import org.amahi.anywhere.adapter.ServerFilesMetadataAdapter;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.FileOpeningEvent;
import org.amahi.anywhere.bus.ServerFileSharingEvent;
import org.amahi.anywhere.bus.ServerFilesLoadFailedEvent;
import org.amahi.anywhere.bus.ServerFilesLoadedEvent;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerShare;
import org.amahi.anywhere.util.Fragments;
import org.amahi.anywhere.util.Mimes;
import org.amahi.anywhere.util.ViewDirector;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import static android.content.Context.UI_MODE_SERVICE;
import static android.support.v4.content.PermissionChecker.checkSelfPermission;

/**
 * Files fragment. Shows files list.
 */
public class ServerFilesFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener,
	AdapterView.OnItemClickListener,
	AdapterView.OnItemLongClickListener,
	ActionMode.Callback,
	SearchView.OnQueryTextListener,
	FilesFilterBaseAdapter.onFilterListChange
{
	private SearchView searchView;
	private MenuItem searchMenuItem;
	private LinearLayout mErrorLinearLayout;

	private static final class State
	{
		private State() {
		}

		public static final String FILES = "files";
		public static final String FILES_SORT = "files_sort";
	}

	private static final int CALLBACK_NUMBER = 100;

	private enum FilesSort
	{
		NAME, MODIFICATION_TIME
	}

	private FilesSort filesSort = FilesSort.MODIFICATION_TIME;

	private ActionMode filesActions;

	@Inject
	ServerClient serverClient;

	@Override
	public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView;
		if (!isMetadataAvailable()) {
			rootView = layoutInflater.inflate(R.layout.fragment_server_files, container, false);
		} else {
			rootView = layoutInflater.inflate(R.layout.fragment_server_files_metadata, container, false);
		}
		mErrorLinearLayout = (LinearLayout) rootView.findViewById(R.id.error);
		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		setUpInjections();

		setUpFiles(savedInstanceState);
	}

	private void setUpInjections() {
		AmahiApplication.from(getActivity()).inject(this);
	}

	private void setUpFiles(Bundle state) {
		setUpFilesMenu();
		setUpFilesActions();
		setUpFilesAdapter();
		setUpFilesContent(state);
		setUpFilesContentRefreshing();
	}

	private void setUpFilesMenu() {
		setHasOptionsMenu(true);
	}

	private void setUpFilesActions() {
		getListView().setOnItemClickListener(this);
		getListView().setOnItemLongClickListener(this);
	}

	private AbsListView getListView() {
		return (AbsListView) getView().findViewById(android.R.id.list);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> filesListView, View fileView, int filePosition, long fileId) {
		if (!areFilesActionsAvailable()) {
			getListView().clearChoices();
			getListView().setItemChecked(filePosition, true);

			getListView().startActionMode(this);

			return true;
		} else {
			return false;
		}
	}

	private boolean areFilesActionsAvailable() {
		return filesActions != null;
	}

	@Override
	public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
		this.filesActions = actionMode;

		actionMode.getMenuInflater().inflate(R.menu.action_mode_server_files, menu);

		return true;
	}

	@Override
	public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
		return false;
	}

	@Override
	public void onDestroyActionMode(ActionMode actionMode) {
		this.filesActions = null;

		clearFileChoices();
	}

	private void clearFileChoices() {
		getListView().clearChoices();
		getListView().requestLayout();
	}

	@RequiresApi(api = Build.VERSION_CODES.M)
	@Override
	public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
		switch (menuItem.getItemId()) {
			case R.id.menu_share:
				checkPermissions();
				break;

			default:
				return false;
		}

		actionMode.finish();

		return true;
	}
	@RequiresApi(api = Build.VERSION_CODES.M)
	private void checkPermissions(){
	int permissionCheck = checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);

	if (!(permissionCheck == PackageManager.PERMISSION_GRANTED)) {

		requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, CALLBACK_NUMBER);

	} else {
		startFileSharing(getCheckedFile());
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
	switch (requestCode) {
		case 100: {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				Snackbar.make(getView(),getString(R.string.share_permission_granted),Snackbar.LENGTH_LONG).show();
			} else {
				Snackbar.make(getView(),getString(R.string.share_permission_denied),Snackbar.LENGTH_LONG)
						.setAction("Permissions", new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								Intent intent = new Intent();
								intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
								Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
								intent.setData(uri);
								startActivity(intent);
							}
						})
						.show();
			}
		   }
		}
	}

	private void startFileSharing(ServerFile file) {
		BusProvider.getBus().post(new ServerFileSharingEvent(getShare(), file));
	}

	private ServerFile getCheckedFile() {
		return getFile(getListView().getCheckedItemPosition());
	}

	private ServerFile getFile(int position) {
		if (!isMetadataAvailable()) {
			return getFilesAdapter().getItem(position);
		} else {
			return getFilesMetadataAdapter().getItem(position);
		}
	}

	private boolean isMetadataAvailable() {
		return ServerShare.Tag.MOVIES.equals(getShare().getTag());
	}

	private ServerFilesAdapter getFilesAdapter() {
		return (ServerFilesAdapter) getListAdapter();
	}

	private ServerFilesMetadataAdapter getFilesMetadataAdapter() {
		return (ServerFilesMetadataAdapter) getListAdapter();
	}

	private ListAdapter getListAdapter() {
		return getListView().getAdapter();
	}

	private void setUpFilesAdapter() {
		if (!isMetadataAvailable()) {
			setListAdapter(new ServerFilesAdapter(getActivity(), serverClient));
		} else {
			setListAdapter(new ServerFilesMetadataAdapter(getActivity(), serverClient));
		}
	}

	private void setListAdapter(FilesFilterBaseAdapter adapter) {
		adapter.setFilterListChangeListener(this);
		getListView().setAdapter(adapter);
	}

	private void setUpFilesContent(Bundle state) {
		if (isFilesStateValid(state)) {
			setUpFilesState(state);
		} else {
			setUpFilesContent();
		}
	}

	private boolean isFilesStateValid(Bundle state) {
		return (state != null) && state.containsKey(State.FILES) && state.containsKey(State.FILES_SORT);
	}

	private void setUpFilesState(Bundle state) {
		List<ServerFile> files = state.getParcelableArrayList(State.FILES);

		FilesSort filesSort = (FilesSort) state.getSerializable(State.FILES_SORT);

		setUpFilesContent(files);
		setUpFilesContentSort(filesSort);

		showFilesContent();
	}

	private void setUpFilesContent(List<ServerFile> files) {
		if (!isMetadataAvailable()) {
			getFilesAdapter().replaceWith(getShare(), files);
		} else {
			getFilesMetadataAdapter().replaceWith(getShare(), getMetadataFiles(files));
		}
	}

	private List<ServerFile> getMetadataFiles(List<ServerFile> files) {
		List<ServerFile> metadataFiles = new ArrayList<ServerFile>();

		for (ServerFile file : files) {
			if (Mimes.match(file.getMime()) == Mimes.Type.DIRECTORY) {
				metadataFiles.add(file);
			}

			if (Mimes.match(file.getMime()) == Mimes.Type.VIDEO) {
				metadataFiles.add(file);
			}
		}
		return metadataFiles;
	}

	private void setUpFilesContentSort(FilesSort filesSort) {
		this.filesSort = filesSort;

		getActivity().invalidateOptionsMenu();
	}

	private void showFilesContent() {
		if (areFilesAvailable()) {
			getView().findViewById(android.R.id.list).setVisibility(View.VISIBLE);
			getView().findViewById(android.R.id.empty).setVisibility(View.INVISIBLE);
		} else {
			getView().findViewById(android.R.id.list).setVisibility(View.INVISIBLE);
			getView().findViewById(android.R.id.empty).setVisibility(View.VISIBLE);
		}

		ViewDirector.of(this, R.id.animator).show(R.id.content);
	}

	private boolean areFilesAvailable() {
		if (!isMetadataAvailable()) {
			return !getFilesAdapter().isEmpty();
		} else {
			return !getFilesMetadataAdapter().isEmpty();
		}
	}

	private void setUpFilesContent() {
        if (serverClient.isConnected()){
            if (!isDirectoryAvailable()) {
                serverClient.getFiles(getShare());
            } else {
                serverClient.getFiles(getShare(), getDirectory());
            }
        }
    }

	private boolean isDirectoryAvailable() {
		return getDirectory() != null;
	}

	private boolean isDirectory(ServerFile file) {
		return Mimes.match(file.getMime()) == Mimes.Type.DIRECTORY;
	}

	private ServerFile getDirectory() {
		return getArguments().getParcelable(Fragments.Arguments.SERVER_FILE);
	}

	private ServerShare getShare() {
		return getArguments().getParcelable(Fragments.Arguments.SERVER_SHARE);
	}

	@Subscribe
	public void onFilesLoaded(ServerFilesLoadedEvent event) {
		showFilesContent(event.getServerFiles());
	}

	private void showFilesContent(List<ServerFile> files) {
		setUpFilesContent(sortFiles(files));

		showFilesContent();

		hideFilesContentRefreshing();
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

	private void hideFilesContentRefreshing() {
		getRefreshLayout().setRefreshing(false);
	}

	private SwipeRefreshLayout getRefreshLayout() {
		return (SwipeRefreshLayout) getView().findViewById(R.id.layout_refresh);
	}

	@Subscribe
	public void onFilesLoadFailed(ServerFilesLoadFailedEvent event) {
		showFilesError();

		hideFilesContentRefreshing();
	}

	private void showFilesError() {
		ViewDirector.of(this, R.id.animator).show(R.id.error);
		mErrorLinearLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				ViewDirector.of(getActivity(), R.id.animator).show(android.R.id.progress);
				setUpFilesContent();
			}
		});
	}

	private void setUpFilesContentRefreshing() {
		SwipeRefreshLayout refreshLayout = getRefreshLayout();

		refreshLayout.setColorSchemeResources(
			android.R.color.holo_blue_light,
			android.R.color.holo_orange_light,
			android.R.color.holo_green_light,
			android.R.color.holo_red_light);

		refreshLayout.setOnRefreshListener(this);
	}

	@Override
	public void onRefresh() {
		setUpFilesContent();
	}

	@Override
	public void onItemClick(AdapterView<?> filesListView, View fileView, int filePosition, long fileId) {
		if (!areFilesActionsAvailable()) {
			collapseSearchView();
			startFileOpening(getFile(filePosition));

			if(isDirectory(getFile(filePosition))){
				setUpTitle(getFile(filePosition).getName());
			}
		}
	}

	private void startFileOpening(ServerFile file) {
		BusProvider.getBus().post(new FileOpeningEvent(getShare(), getFiles(), file));
	}

	private void setUpTitle(String title) {
		((ServerFilesActivity)getActivity()).getSupportActionBar().setTitle(title);
       }

	private List<ServerFile> getFiles() {
		if (!isMetadataAvailable()) {
			return getFilesAdapter().getItems();
		} else {
			return getFilesMetadataAdapter().getItems();
		}
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
		searchMenuItem = menu.findItem(R.id.menu_search);
		searchView = (SearchView) searchMenuItem.getActionView();

		setUpSearchView();
		setSearchCursor();
	}

	private void setUpSearchView() {
		SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
		searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
		searchView.setSubmitButtonEnabled(false);
		searchView.setOnQueryTextListener(this);
	}

	private void setSearchCursor() {
		final int textViewID = searchView.getContext().getResources().getIdentifier("android:id/search_src_text",null, null);
		final AutoCompleteTextView searchTextView = (AutoCompleteTextView) searchView.findViewById(textViewID);
		try {
			Field mCursorDrawableRes = TextView.class.getDeclaredField("mCursorDrawableRes");
			mCursorDrawableRes.setAccessible(true);
			mCursorDrawableRes.set(searchTextView, R.drawable.white_cursor);
		} catch (Exception ignored) {}
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
		if (!isMetadataAvailable()) {
			getFilesAdapter().replaceWith(getShare(), sortFiles(getFiles()));
		} else {
			getFilesMetadataAdapter().replaceWith(getShare(), sortFiles(getFiles()));
		}
	}

	@Override
	public boolean onQueryTextSubmit(String s) {
		return false;
	}

	@Override
	public boolean onQueryTextChange(String s) {
		if (!isMetadataAvailable()) {
			getFilesAdapter().getFilter().filter(s);
		} else {
			getFilesMetadataAdapter().getFilter().filter(s);
		}
		return true;
	}

	@Override
	public void isListEmpty(boolean empty) {
		if(getView().findViewById(R.id.none_text)!=null)
			getView().findViewById(R.id.none_text).setVisibility(empty?View.VISIBLE:View.GONE);
	}

	private void collapseSearchView() {
		if (searchView.isShown()) {
			searchMenuItem.collapseActionView();
			searchView.setQuery("", false);
		}
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
	public void onDestroyView() {
		super.onDestroyView();
		if (isMetadataAvailable()) {
			getFilesMetadataAdapter().tearDownCallbacks();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		tearDownFilesState(outState);
	}

	private void tearDownFilesState(Bundle state) {
		if (areFilesLoaded()) {
			state.putParcelableArrayList(State.FILES, new ArrayList<Parcelable>(getFiles()));
		}

		state.putSerializable(State.FILES_SORT, filesSort);
	}

	private boolean areFilesLoaded() {
		if (getView() == null) {
			return false;
		}

		if (!isMetadataAvailable()) {
			return getFilesAdapter() != null;
		} else {
			return getFilesMetadataAdapter() != null;
		}
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
