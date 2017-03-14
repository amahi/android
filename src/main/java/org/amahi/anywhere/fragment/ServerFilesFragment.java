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
import android.app.ActionBar;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.RequiresApi;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.R;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import static android.support.v4.content.PermissionChecker.checkSelfPermission;
import static android.view.View.GONE;

/**
 * Files fragment. Shows files list.
 */
public class ServerFilesFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener,
	AdapterView.OnItemClickListener,
	AdapterView.OnItemLongClickListener,
	ActionMode.Callback
{
	private static final class State
	{
		private State() {
		}

		public static final String FILES = "files";
		public static final String FILES_SORT = "files_sort";
	}

	private static final int CALLBACK_NUMBER = 100;

	private boolean isSearchOpen = false;

	private enum FilesSort
	{
		NAME, MODIFICATION_TIME
	}

	private FilesSort filesSort = FilesSort.MODIFICATION_TIME;

	private ActionMode filesActions;

	private EditText editSearch;

	private TextView noText;

	@Inject
	ServerClient serverClient;

	@Override
	public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
		if (!isMetadataAvailable()) {
			View rootView = layoutInflater.inflate(R.layout.fragment_server_files, container, false);
			noText = (TextView)rootView.findViewById(R.id.none_text);
			return rootView;
		} else {
			View rootView = layoutInflater.inflate(R.layout.fragment_server_files_metadata, container, false);
			noText = (TextView)rootView.findViewById(R.id.none_text);
			return rootView;
		}
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
		if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
		} else {
		requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, CALLBACK_NUMBER);
		}
	} else {
		startFileSharing(getCheckedFile());
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
	switch (requestCode) {
		case 100: {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				Toast.makeText(getActivity().getApplicationContext(),"Storage permission has been enabled please reshare to download",Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(getActivity().getApplicationContext(),"You have denied the permission please enable permission in settings for media access",Toast.LENGTH_LONG).show();
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

	private void setListAdapter(ListAdapter adapter) {
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

				clearFileChoices();

				startFileOpening(getFile(filePosition));

				if(isSearchOpen)
					closeSearch();
		}
	}

	private void startFileOpening(ServerFile file) {
		BusProvider.getBus().post(new FileOpeningEvent(getShare(), getFiles(), file));
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

			case R.id.menu_search:
				isSearchOpen = true;
				ActionBar actionBar = (getActivity()).getActionBar();
				actionBar.setDisplayShowCustomEnabled(true);
				final List<ServerFile> allFiles = getFiles();
				actionBar.setCustomView(R.layout.search_bar);
				actionBar.setDisplayShowTitleEnabled(false);
				actionBar.setDisplayShowHomeEnabled(false);
				editSearch = (EditText) actionBar.getCustomView().findViewById(R.id.editSearch);
				editSearch.setHint("Search");
				editSearch.setGravity(Gravity.LEFT);
				editSearch.setLongClickable(false);
				editSearch.addTextChangedListener(new TextWatcher() {
					@Override
					public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
						//Intentionally left blank.
					}

					@Override
					public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
						//Intentionally left blank.
					}

					@Override
					public void afterTextChanged(Editable editable) {
						List<ServerFile> searchList = new ArrayList<ServerFile>();
						searchList.clear();
						for(int i=0;i<allFiles.size();i++){
							if(allFiles.get(i).getName().toLowerCase().contains(editable.toString().toLowerCase()))
								searchList.add(allFiles.get(i));
						}
						setUpFilesContent(searchList);
						if(searchList.size()==0)
							noText.setVisibility(View.VISIBLE);
						else
							noText.setVisibility(View.INVISIBLE);
					}
				});
					editSearch.setOnKeyListener(new View.OnKeyListener() {
					@Override
					public boolean onKey(View v, int keyCode, KeyEvent event) {
						if (keyCode == KeyEvent.KEYCODE_BACK) {
							editSearch.onKeyPreIme(keyCode, event);
							if (isSearchOpen) {
								closeSearch();
								setUpFilesContent(allFiles);
							}
							return true;
						}
					return false;
					}
				});

				editSearch.requestFocus();
				InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(editSearch, InputMethodManager.SHOW_IMPLICIT);
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

	public void closeSearch() {
		if (isSearchOpen) {
			editSearch.setText("");
			ActionBar actionBar = (getActivity()).getActionBar();
			InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(editSearch.getWindowToken(), 0);
			isSearchOpen = false;
			actionBar.setDisplayShowCustomEnabled(false);
			actionBar.setDisplayShowTitleEnabled(true);
			actionBar.setDisplayShowHomeEnabled(true);
		}
	}
}
