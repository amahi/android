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
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
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
import org.amahi.anywhere.adapter.FilesFilterAdapter;
import org.amahi.anywhere.adapter.ServerFilesAdapter;
import org.amahi.anywhere.adapter.ServerFilesMetadataAdapter;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.FileOpeningEvent;
import org.amahi.anywhere.bus.FileOptionClickEvent;
import org.amahi.anywhere.bus.OfflineCanceledEvent;
import org.amahi.anywhere.bus.OfflineFileDeleteEvent;
import org.amahi.anywhere.bus.ServerFileDeleteEvent;
import org.amahi.anywhere.bus.ServerFileDownloadingEvent;
import org.amahi.anywhere.bus.ServerFileSharingEvent;
import org.amahi.anywhere.bus.ServerFilesLoadFailedEvent;
import org.amahi.anywhere.bus.ServerFilesLoadedEvent;
import org.amahi.anywhere.db.entities.OfflineFile;
import org.amahi.anywhere.db.repositories.OfflineFileRepository;
import org.amahi.anywhere.model.FileOption;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerShare;
import org.amahi.anywhere.util.Android;
import org.amahi.anywhere.util.Downloader;
import org.amahi.anywhere.util.Fragments;
import org.amahi.anywhere.util.Intents;
import org.amahi.anywhere.util.Mimes;
import org.amahi.anywhere.util.Preferences;
import org.amahi.anywhere.util.ServerFileClickListener;
import org.amahi.anywhere.util.ViewDirector;

import java.io.File;
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
    ServerFileClickListener,
    SearchView.OnQueryTextListener,
    FilesFilterAdapter.onFilterListChange,
    EasyPermissions.PermissionCallbacks,
    CastStateListener {
    public final static int EXTERNAL_STORAGE_PERMISSION = 101;

    public static final int SORT_MODIFICATION_TIME = 0;
    public static final int SORT_NAME = 1;

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
    private int lastSelectedFilePosition = -1;

    @Types
    private int filesSort = SORT_MODIFICATION_TIME;

    private OfflineFileRepository mOfflineFileRepo;
    private @FileOption.Types
    int selectedFileOption;

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView;
        if (!isMetadataAvailable()) {
            rootView = layoutInflater.inflate(R.layout.fragment_server_files, container, false);
        } else {
            rootView = layoutInflater.inflate(R.layout.fragment_server_files_metadata, container, false);
        }
        mErrorLinearLayout = rootView.findViewById(R.id.error);
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
            new Handler().post(() -> {
                mIntroductoryOverlay = new IntroductoryOverlay
                    .Builder(getActivity(), mediaRouteMenuItem)
                    .setTitleText("Introducing Cast")
                    .setSingleTime()
                    .setOnOverlayDismissedListener(
                        () -> mIntroductoryOverlay = null)
                    .build();
                mIntroductoryOverlay.show();
            });
        }
    }

    private void setUpFiles(Bundle state) {
        setUpFilesMenu();
        setUpFilesAdapter();
        setUpFilesActions();
        setUpDefaults();
        setUpOfflineFileDatabase();
        setUpFilesContent(state);
        setUpFilesContentRefreshing();
    }

    private void setUpDefaults() {
        filesSort = Preferences.getSortOption(getContext());
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
        getListAdapter().setOnClickListener(this);
    }

    private RecyclerView getRecyclerView() {
        return (RecyclerView) getView().findViewById(android.R.id.list);
    }

    @Subscribe
    public void onFileOptionSelected(FileOptionClickEvent event) {
        selectedFileOption = event.getFileOption();
        switch (selectedFileOption) {
            case FileOption.DOWNLOAD:
                if (Android.isPermissionRequired()) {
                    checkWritePermissions();
                } else {
                    startFileDownloading();
                }
                break;
            case FileOption.SHARE:
                if (Android.isPermissionRequired()) {
                    checkWritePermissions();
                } else {
                    startFileSharing();
                }
                break;
            case FileOption.DELETE:
                deleteFile();
                break;
            case FileOption.OFFLINE_ENABLED:
                if (Android.isPermissionRequired()) {
                    checkWritePermissions();
                } else {
                    changeOfflineState(true);
                }
                break;
            case FileOption.OFFLINE_DISABLED:
                changeOfflineState(false);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkWritePermissions() {
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(getContext(), perms)) {
            handleFileOptionsWithPermissionGranted();
        } else {
            lastSelectedFilePosition = getListAdapter().getSelectedPosition();
            EasyPermissions.requestPermissions(this, getString(R.string.share_permission),
                EXTERNAL_STORAGE_PERMISSION, perms);
        }
    }

    private void handleFileOptionsWithPermissionGranted() {

        switch (selectedFileOption) {
            case FileOption.DOWNLOAD:
                startFileDownloading();
                break;
            case FileOption.SHARE:
                startFileSharing();
                break;
            case FileOption.OFFLINE_ENABLED:
                changeOfflineState(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        if (lastSelectedFilePosition != -1) {
            handleFileOptionsWithPermissionGranted();
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            if (requestCode == EXTERNAL_STORAGE_PERMISSION) {
                showPermissionSnackBar(getString(R.string.share_permission_denied));
            }
        }
    }

    private void showPermissionSnackBar(String message) {
        Snackbar.make(getView(), message, Snackbar.LENGTH_LONG)
            .setAction(R.string.menu_settings, v -> new AppSettingsDialog.Builder(ServerFilesFragment.this).build().show())
            .show();
    }

    private void startFileDownloading() {
        BusProvider.getBus().post(new ServerFileDownloadingEvent(getShare(), getCheckedFile()));
    }

    private void startFileSharing() {
        BusProvider.getBus().post(new ServerFileSharingEvent(getShare(), getCheckedFile()));
    }

    private ServerFile getOfflineServerFile(OfflineFile offlineFile) {
        return new ServerFile(offlineFile.getName(), offlineFile.getTimeStamp(), offlineFile.getMime());
    }

    private void deleteFile() {
        if (!isOfflineFragment()) {
            deleteFilePosition = getListAdapter().getSelectedPosition();
            new AlertDialog.Builder(getContext())
                .setTitle(R.string.message_delete_file_title)
                .setMessage(R.string.message_delete_file_body)
                .setPositiveButton(R.string.button_yes, (dialog, which) -> {
                    deleteProgressDialog.show();
                    serverClient.deleteFile(getShare(), getCheckedFile());
                })
                .setNegativeButton(R.string.button_no, null)
                .show();
        } else {
            BusProvider.getBus().post(new OfflineFileDeleteEvent(getCheckedFile()));
        }
    }

    private void changeOfflineState(boolean enable) {
        if (enable) {
            startDownloadService(getCheckedFile());
        } else {
            deleteFileFromOfflineStorage();
        }

        updateCurrentFileOfflineState(enable);
    }

    private void deleteFileFromOfflineStorage() {
        OfflineFile offlineFile = mOfflineFileRepo.getOfflineFile(getCheckedFile().getName(), getCheckedFile().getModificationTime().getTime());
        if (offlineFile != null) {
            if (offlineFile.getState() == OfflineFile.DOWNLOADING) {
                stopDownloading(offlineFile.getDownloadId());
            }
            File file = new File(getContext().getFilesDir(), Downloader.OFFLINE_PATH + "/" + getCheckedFile().getName());
            if (file.exists()) {
                file.delete();
            }
            mOfflineFileRepo.delete(offlineFile);
            Snackbar.make(getRecyclerView(), R.string.message_offline_file_deleted, Snackbar.LENGTH_SHORT)
                .show();
        }
    }

    private void stopDownloading(long downloadId) {
        DownloadManager dm = (DownloadManager) getContext().getSystemService(Context.DOWNLOAD_SERVICE);
        if (dm != null) {
            dm.remove(downloadId);
            BusProvider.getBus().post(new OfflineCanceledEvent(downloadId));
        }
    }

    private void startDownloadService(ServerFile file) {
        Intent downloadService = Intents.Builder.with(getContext()).buildDownloadServiceIntent(file, getShare());
        getContext().startService(downloadService);
    }

    private void updateCurrentFileOfflineState(boolean enable) {
        ServerFile serverFile = getCheckedFile();
        serverFile.setOffline(enable);
    }

    @Subscribe
    public void onFileDeleteEvent(ServerFileDeleteEvent fileDeleteEvent) {
        if (deleteProgressDialog != null) {
            deleteProgressDialog.dismiss();
        }
        if (fileDeleteEvent.isDeleted()) {
            if (!isMetadataAvailable()) {
                if (!isOfflineFragment()) {
                    getFilesAdapter().removeFile(deleteFilePosition);
                } else {
                    getListAdapter().removeFile(getListAdapter().getSelectedPosition());
                }
                getFilesAdapter().setSelectedPosition(RecyclerView.NO_POSITION);
            } else {
                getFilesMetadataAdapter().removeFile(deleteFilePosition);
                getFilesMetadataAdapter().setSelectedPosition(RecyclerView.NO_POSITION);
            }
            showFilesContent();
        } else {
            Toast.makeText(getContext(), R.string.message_delete_file_error, Toast.LENGTH_SHORT).show();
        }
    }

    private ServerFile getCheckedFile() {
        return getListAdapter().getItem(getListAdapter().getSelectedPosition());
    }

    private ServerFile getFile(int position) {
        if (!isMetadataAvailable()) {
            return getFilesAdapter().getItem(position);
        } else {
            return getFilesMetadataAdapter().getItem(position);
        }
    }

    private boolean isMetadataAvailable() {
        return getShare() != null && ServerShare.Tag.MOVIES.equals(getShare().getTag());
    }

    private ServerFilesAdapter getFilesAdapter() {
        return (ServerFilesAdapter) getListAdapter();
    }

    private ServerFilesMetadataAdapter getFilesMetadataAdapter() {
        return (ServerFilesMetadataAdapter) getListAdapter();
    }

    private FilesFilterAdapter getListAdapter() {
        return (FilesFilterAdapter) getRecyclerView().getAdapter();
    }

    private void setListAdapter(FilesFilterAdapter adapter) {
        adapter.setFilterListChangeListener(this);
        getRecyclerView().setAdapter(adapter);
    }

    private void setUpFilesAdapter() {
        setUpRecyclerLayout();

        if (!isMetadataAvailable()) {
            setListAdapter(new ServerFilesAdapter(getActivity(), serverClient));
        } else {
            setListAdapter(new ServerFilesMetadataAdapter(getActivity(), serverClient));
        }
    }

    private void setUpRecyclerLayout() {
        if (!isMetadataAvailable()) {
            getRecyclerView().setLayoutManager(new LinearLayoutManager(getActivity()));
        } else {
            if (isLandscapeOrientation()) {
                getRecyclerView().setLayoutManager(new GridLayoutManager(getActivity(), calculateNoOfColumns(getActivity())));
            } else {
                getRecyclerView().setLayoutManager(new GridLayoutManager(getActivity(), 2));
            }
        }

        addListItemDivider();
    }

    private void addListItemDivider() {
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
            getRecyclerView().getContext(),
            DividerItemDecoration.VERTICAL);

        getRecyclerView().addItemDecoration(dividerItemDecoration);
    }

    public int calculateNoOfColumns(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        return (int) ((dpWidth - 32) / 157);
    }

    public boolean isLandscapeOrientation() {
        int screenOrientation = getActivity().getResources().getConfiguration().orientation;
        return screenOrientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    private void setUpFilesContent(Bundle state) {

        if (!isOfflineFragment()) {
            if (isFilesStateValid(state)) {
                setUpFilesState(state);
            } else {
                setUpFilesContent();
            }
        } else {
            getListAdapter().setAdapterMode(FilesFilterAdapter.AdapterMode.OFFLINE);
            getFilesAdapter().setUpDownloadReceiver();
            showOfflineFiles();
        }
    }

    private boolean isOfflineFragment() {
        return getArguments().getBoolean(Fragments.Arguments.IS_OFFLINE_FRAGMENT);
    }

    private boolean isFilesStateValid(Bundle state) {
        return (state != null) && state.containsKey(State.FILES);
    }

    private void setUpFilesState(Bundle state) {
        List<ServerFile> files = state.getParcelableArrayList(State.FILES);

        setUpFilesContent(files);
        setUpFilesContentSort(filesSort);
        getListAdapter().setSelectedPosition(state.getInt(State.SELECTED_ITEM, -1));

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
        List<ServerFile> metadataFiles = new ArrayList<>();

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

    private void setUpFilesContentSort(@Types int filesSort) {
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
        return !getListAdapter().isEmpty();
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

    private void showOfflineFiles() {
        List<ServerFile> serverFiles = prepareServerFilesFrom(mOfflineFileRepo.getAllOfflineFiles());
        getListAdapter().replaceWith(null, serverFiles);
        showFilesContent();
        hideFilesContentRefreshing();
    }

    private List<ServerFile> prepareServerFilesFrom(List<OfflineFile> files) {
        List<ServerFile> serverFiles = new ArrayList<>();

        for (OfflineFile offlineFile : files) {
            ServerFile serverFile = new ServerFile(offlineFile.getName(), offlineFile.getTimeStamp(), offlineFile.getMime());
            serverFiles.add(serverFile);
        }

        return sortFiles(serverFiles);
    }

    @Subscribe
    public void onFilesLoaded(ServerFilesLoadedEvent event) {
        showFilesContent(event.getServerFiles());
    }

    private void showFilesContent(List<ServerFile> files) {
        setUpFilesContent(checkOfflineFiles(sortFiles(files)));

        showFilesContent();

        hideFilesContentRefreshing();
    }

    private List<ServerFile> sortFiles(List<ServerFile> files) {
        List<ServerFile> sortedFiles = new ArrayList<>(files);

        Collections.sort(sortedFiles, getFilesComparator());

        return sortedFiles;
    }

    private Comparator<ServerFile> getFilesComparator() {
        switch (filesSort) {
            case SORT_NAME:
                return new FileNameComparator();

            case SORT_MODIFICATION_TIME:
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
        mErrorLinearLayout.setOnClickListener(view -> {
            ViewDirector.of(getActivity(), R.id.animator).show(android.R.id.progress);
            setUpFilesContent();
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

    private void setUpOfflineFileDatabase() {
        mOfflineFileRepo = new OfflineFileRepository(getContext());
    }

    private List<ServerFile> checkOfflineFiles(List<ServerFile> serverFiles) {
        for (ServerFile file : serverFiles) {
            OfflineFile offlineFile = mOfflineFileRepo.getOfflineFile(file.getName(), file.getModificationTime().getTime());
            if (offlineFile != null) {
                file.setOffline(true);
                if (offlineFile.getTimeStamp() < file.getModificationTime().getTime()) {
                    offlineFile.setState(OfflineFile.OUT_OF_DATE);
                    mOfflineFileRepo.update(offlineFile);
                    startDownloadService(file);
                }
            }
        }

        return serverFiles;
    }

    @Override
    public void onRefresh() {
        if (!isOfflineFragment()) {
            setUpFilesContent();
        } else {
            showOfflineFiles();
        }
    }

    private void startFileOpening(int position) {
        if (!isOfflineFragment()) {
            BusProvider.getBus().post(new FileOpeningEvent(getShare(), getFiles(), getFile(position)));
        } else {
            ServerFile offlineServerFile = getListAdapter().getItem(position);
            ArrayList<ServerFile> serverFiles = new ArrayList<>();
            serverFiles.add(offlineServerFile);
            BusProvider.getBus().post(new FileOpeningEvent(null, serverFiles, offlineServerFile));
        }
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
        final AutoCompleteTextView searchTextView = searchView.findViewById(textViewID);
        try {
            Field mCursorDrawableRes = TextView.class.getDeclaredField("mCursorDrawableRes");
            mCursorDrawableRes.setAccessible(true);
            mCursorDrawableRes.set(searchTextView, R.drawable.white_cursor);
        } catch (Exception ignored) {
        }
    }

    private void setUpFilesContentSortIcon(MenuItem menuItem) {
        switch (filesSort) {
            case SORT_NAME:
                menuItem.setIcon(R.drawable.ic_menu_sort_name);
                break;

            case SORT_MODIFICATION_TIME:
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
            case SORT_NAME:
                filesSort = SORT_MODIFICATION_TIME;
                break;

            case SORT_MODIFICATION_TIME:
                filesSort = SORT_NAME;
                break;

            default:
                break;
        }

        saveSortOption();

        setUpFilesContentSort();
    }

    private void saveSortOption() {
        Preferences.setSortOption(getContext(), filesSort);
    }

    private void setUpFilesContentSort() {
        if (!isMetadataAvailable()) {
            getFilesAdapter().replaceWith(getShare(), checkOfflineFiles(sortFiles(getFiles())));
        } else {
            getFilesMetadataAdapter().replaceWith(getShare(), checkOfflineFiles(sortFiles(getFiles())));
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
        } else {
            getFilesAdapter().tearDownCallbacks();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        tearDownFilesState(outState);
        outState.putInt(State.SELECTED_ITEM, lastSelectedFilePosition);
    }

    private void tearDownFilesState(Bundle state) {
        if (areFilesLoaded()) {
            state.putParcelableArrayList(State.FILES, new ArrayList<Parcelable>(getFiles()));
        }
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

    private void setItemSelected(int position) {
        lastSelectedFilePosition = position;
        getListAdapter().setSelectedPosition(position);
    }

    @Override
    public void onItemClick(View view, int filePosition) {
        collapseSearchView();
        startFileOpening(filePosition);

        if (!isOfflineFragment() && isDirectory(getFile(filePosition))) {
            setUpTitle(getFile(filePosition).getName());
        }
    }

    @Override
    public void onMoreOptionClick(View view, int position) {
        setItemSelected(position);
        if (getListAdapter().getAdapterMode() != FilesFilterAdapter.AdapterMode.OFFLINE) {
            Fragments.Builder.buildFileOptionsDialogFragment(getContext(), getCheckedFile())
                .show(getChildFragmentManager(), "file_options_dialog");
        } else {
            Fragments.Builder.buildOfflineFileOptionsDialogFragment()
                .show(getChildFragmentManager(), "file_options_dialog");
        }
    }

    @Override
    public void isListEmpty(boolean empty) {
        if (getView().findViewById(R.id.none_text) != null)
            getView().findViewById(R.id.none_text).setVisibility(empty ? View.VISIBLE : View.GONE);
    }

    @IntDef({SORT_MODIFICATION_TIME, SORT_NAME})
    public @interface Types {
    }

    private static final class State {
        public static final String FILES = "files";
        public static final String SELECTED_ITEM = "selected_item";

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
