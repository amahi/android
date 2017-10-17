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
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
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
import android.widget.Toast;

import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastState;
import com.google.android.gms.cast.framework.CastStateListener;
import com.google.android.gms.cast.framework.IntroductoryOverlay;
import com.squareup.otto.Subscribe;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.R;
import org.amahi.anywhere.activity.ServerFilesActivity;
import org.amahi.anywhere.adapter.FilesFilterBaseAdapter;
import org.amahi.anywhere.adapter.ServerFilesAdapter;
import org.amahi.anywhere.adapter.ServerFilesMetadataAdapter;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.FileOpeningEvent;
import org.amahi.anywhere.bus.ServerFileDeleteEvent;
import org.amahi.anywhere.bus.ServerFileSharingEvent;
import org.amahi.anywhere.bus.ServerFilesLoadFailedEvent;
import org.amahi.anywhere.bus.ServerFilesLoadedEvent;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerShare;
import org.amahi.anywhere.util.Android;
import org.amahi.anywhere.util.Fragments;
import org.amahi.anywhere.util.Mimes;
import org.amahi.anywhere.util.ViewDirector;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Files fragment. Shows files list.
 */

public class ServerFilesFragment extends Fragment implements
    SwipeRefreshLayout.OnRefreshListener,
    AdapterView.OnItemClickListener,
    AdapterView.OnItemLongClickListener,
    ActionMode.Callback,
    SearchView.OnQueryTextListener,
    FilesFilterBaseAdapter.onFilterListChange,
    EasyPermissions.PermissionCallbacks,
    CastStateListener {
    private static final int SHARE_PERMISSIONS = 101;
    @Inject
    ServerClient serverClient;
    private SearchView searchView;
    private MenuItem searchMenuItem;
    private LinearLayout mErrorLinearLayout;
    private CastContext mCastContext;
    private IntroductoryOverlay mIntroductoryOverlay;
    private MenuItem mediaRouteMenuItem;
    private ProgressDialog deleteProgressDialog;
    private int deleteFilePosition;
    private int lastCheckedFileIndex = -1;
    private FilesSort filesSort = FilesSort.MODIFICATION_TIME;
    private ActionMode filesActions;

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

        setUpCast();

        setUpFiles(savedInstanceState);

        setUpProgressDialog();
    }

    private void setUpInjections() {
        AmahiApplication.from(getActivity()).inject(this);
    }

    private void setUpCast() {
        mCastContext = CastContext.getSharedInstance(getActivity());
    }

    @Override
    public void onCastStateChanged(int newState) {
        if (newState != CastState.NO_DEVICES_AVAILABLE) {
            showIntroductoryOverlay();
        }
    }

    private void showIntroductoryOverlay() {
        if (mIntroductoryOverlay != null) {
            mIntroductoryOverlay.remove();
        }
        if ((mediaRouteMenuItem != null) && mediaRouteMenuItem.isVisible()) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    mIntroductoryOverlay = new IntroductoryOverlay
                        .Builder(getActivity(), mediaRouteMenuItem)
                        .setTitleText("Introducing Cast")
                        .setSingleTime()
                        .setOnOverlayDismissedListener(
                            new IntroductoryOverlay.OnOverlayDismissedListener() {
                                @Override
                                public void onOverlayDismissed() {
                                    mIntroductoryOverlay = null;
                                }
                            })
                        .build();
                    mIntroductoryOverlay.show();
                }
            });
        }
    }

    private void setUpFiles(Bundle state) {
        setUpFilesMenu();
        setUpFilesActions();
        setUpFilesAdapter();
        setUpFilesContent(state);
        setUpFilesContentRefreshing();
    }

    private void setUpProgressDialog() {
        deleteProgressDialog = new ProgressDialog(getContext());
        deleteProgressDialog.setMessage(getString(R.string.message_delete_progress));
        deleteProgressDialog.setIndeterminate(true);
        deleteProgressDialog.setCancelable(false);
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

    @Override
    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.menu_share:
                if (Android.isPermissionRequired()) {
                    checkSharePermissions(actionMode);
                } else {
                    startFileSharing(getCheckedFile());
                    actionMode.finish();
                }
                break;
            case R.id.menu_delete:
                deleteFile(getCheckedFile(), actionMode);
                break;
            default:
                return false;
        }

        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkSharePermissions(ActionMode actionMode) {
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(getContext(), perms)) {
            startFileSharing(getCheckedFile());
        } else {
            lastCheckedFileIndex = getListView().getCheckedItemPosition();
            EasyPermissions.requestPermissions(this, getString(R.string.share_permission),
                SHARE_PERMISSIONS, perms);
        }
        actionMode.finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        if (requestCode == SHARE_PERMISSIONS) {
            if (lastCheckedFileIndex != -1) {
                startFileSharing(getFile(lastCheckedFileIndex));
            }
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            if (requestCode == SHARE_PERMISSIONS) {
                showPermissionSnackBar(getString(R.string.share_permission_denied));
            }
        }

    }

    private void showPermissionSnackBar(String message) {
        Snackbar.make(getView(), message, Snackbar.LENGTH_LONG)
            .setAction(R.string.menu_settings, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AppSettingsDialog.Builder(ServerFilesFragment.this).build().show();
                }
            })
            .show();
    }

    private void startFileSharing(ServerFile file) {
        BusProvider.getBus().post(new ServerFileSharingEvent(getShare(), file));
    }

    private void deleteFile(final ServerFile file, final ActionMode actionMode) {
        deleteFilePosition = getListView().getCheckedItemPosition();
        new AlertDialog.Builder(getContext())
            .setTitle(R.string.message_delete_file_title)
            .setMessage(R.string.message_delete_file_body)
            .setPositiveButton(R.string.button_yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    deleteProgressDialog.show();
                    serverClient.deleteFile(getShare(), file);
                    actionMode.finish();
                }
            })
            .setNegativeButton(R.string.button_no, null)
            .show();
    }

    @Subscribe
    public void onFileDeleteEvent(ServerFileDeleteEvent fileDeleteEvent) {
        deleteProgressDialog.dismiss();
        if (fileDeleteEvent.isDeleted()) {
            if (!isMetadataAvailable()) {
                getFilesAdapter().removeFile(deleteFilePosition);
            } else {
                getFilesMetadataAdapter().removeFile(deleteFilePosition);
            }
        } else {
            Toast.makeText(getContext(), R.string.message_delete_file_error, Toast.LENGTH_SHORT).show();
        }
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

    private void setListAdapter(FilesFilterBaseAdapter adapter) {
        adapter.setFilterListChangeListener(this);
        getListView().setAdapter(adapter);
    }

    private void setUpFilesAdapter() {
        if (!isMetadataAvailable()) {
            setListAdapter(new ServerFilesAdapter(getActivity(), getActivity().getApplicationContext(), serverClient));
        } else {
            setListAdapter(new ServerFilesMetadataAdapter(getActivity(), serverClient));
        }
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
        if (serverClient.isConnected()) {
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

            if (isDirectory(getFile(filePosition))) {
                setUpTitle(getFile(filePosition).getName());
            }
        }
    }

    private void startFileOpening(ServerFile file) {
        BusProvider.getBus().post(new FileOpeningEvent(getShare(), getFiles(), file));
    }

    private void setUpTitle(String title) {
        ((ServerFilesActivity) getActivity()).getSupportActionBar().setTitle(title);
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

        mediaRouteMenuItem = CastButtonFactory.setUpMediaRouteButton(
            getActivity().getApplicationContext(),
            menu, R.id.media_route_menu_item);
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
        final int textViewID = searchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
        final AutoCompleteTextView searchTextView = (AutoCompleteTextView) searchView.findViewById(textViewID);
        try {
            Field mCursorDrawableRes = TextView.class.getDeclaredField("mCursorDrawableRes");
            mCursorDrawableRes.setAccessible(true);
            mCursorDrawableRes.set(searchTextView, R.drawable.white_cursor);
        } catch (Exception ignored) {
        }
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
        if (getView().findViewById(R.id.none_text) != null)
            getView().findViewById(R.id.none_text).setVisibility(empty ? View.VISIBLE : View.GONE);
    }

    private void collapseSearchView() {
        if (searchView.isShown()) {
            searchMenuItem.collapseActionView();
            searchView.setQuery("", false);
        }
    }

    public boolean checkForDuplicateFile(String fileName) {
        List<ServerFile> files;

        if (!isMetadataAvailable()) {
            files = getFilesAdapter().getItems();
        } else {
            files = getFilesMetadataAdapter().getItems();
        }
        for (ServerFile serverFile : files) {
            if (serverFile.getName().equals(fileName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();

        mCastContext.addCastStateListener(this);
        BusProvider.getBus().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        mCastContext.removeCastStateListener(this);
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

    private enum FilesSort {
        NAME, MODIFICATION_TIME
    }

    private static final class State {
        public static final String FILES = "files";
        public static final String FILES_SORT = "files_sort";
        private State() {
        }
    }

    private static final class FileNameComparator implements Comparator<ServerFile> {
        @Override
        public int compare(ServerFile firstFile, ServerFile secondFile) {
            return firstFile.getName().compareTo(secondFile.getName());
        }
    }

    private static final class FileModificationTimeComparator implements Comparator<ServerFile> {
        @Override
        public int compare(ServerFile firstFile, ServerFile secondFile) {
            return -firstFile.getModificationTime().compareTo(secondFile.getModificationTime());
        }
    }
}
