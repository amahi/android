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

package org.amahi.anywhere.tv.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v17.leanback.app.BrowseFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;

import com.squareup.otto.Subscribe;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.R;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.FileOpeningEvent;
import org.amahi.anywhere.bus.ServerConnectionChangedEvent;
import org.amahi.anywhere.bus.ServerFilesLoadedEvent;
import org.amahi.anywhere.bus.ServerSharesLoadFailedEvent;
import org.amahi.anywhere.bus.ServerSharesLoadedEvent;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.Server;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerShare;
import org.amahi.anywhere.tv.presenter.GridItemPresenter;
import org.amahi.anywhere.tv.presenter.SettingsItemPresenter;
import org.amahi.anywhere.util.Intents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

public class MainTVFragment extends BrowseFragment {

    @Inject
    ServerClient serverClient;
    List<ServerShare> serverShareList;
    private FilesSort filesSort = FilesSort.MODIFICATION_TIME;
    private ArrayObjectAdapter mRowsAdapter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setUpInjections();

        setupUIElements();

        loadRows();

        loadShares();
    }

    private void setUpInjections() {
        AmahiApplication.from(getActivity()).inject(this);
    }

    private ArrayList<Server> getServers() {
        return getActivity().getIntent().getParcelableArrayListExtra(getString(R.string.intent_servers));
    }

    private void setupUIElements() {
        setTitle(getString(R.string.app_title));

        setHeadersState(HEADERS_ENABLED);

        setHeadersTransitionOnBackEnabled(true);

        setBrandColor(Color.parseColor("#0277bd"));

        setSearchAffordanceColor(Color.GREEN);
    }

    private void loadRows() {
        mRowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        addSettings(mRowsAdapter);
        addSeparator(mRowsAdapter);
    }

    private void loadShares() {
        if (serverClient.isConnected()) {
            serverClient.getShares();
        }
    }

    @Subscribe
    public void onServerConnectionChanged(ServerConnectionChangedEvent event) {
        serverClient.getShares();
    }

    @Subscribe
    public void onSharesLoaded(ServerSharesLoadedEvent event) {
        List<ServerShare> serverShareList = event.getServerShares();
        this.serverShareList = serverShareList;
        for (int i = 0; i < serverShareList.size(); i++) {
            serverClient.getFiles(serverShareList.get(i));
        }
    }

    @Subscribe
    public void onFilesLoaded(ServerFilesLoadedEvent event) {
        List<ServerFile> serverFiles = sortFiles(event.getServerFiles());
        ArrayObjectAdapter gridRowAdapter = new ArrayObjectAdapter(new GridItemPresenter(getActivity(), serverFiles));
        if (serverFiles.size() != 0) {
            String shareName = serverFiles.get(0).getParentShare().getName();
            for (int i = 0; i < serverFiles.size(); i++) {
                gridRowAdapter.add(serverFiles.get(i));
            }
            int i;

            for (i = 0; i < serverShareList.size(); i++) {
                if (shareName.matches(serverShareList.get(i).getName())) {
                    HeaderItem headerItem = new HeaderItem(shareName);
                    mRowsAdapter.add(new ListRow(headerItem, gridRowAdapter));
                    serverShareList.remove(i);
                }
            }
        }
        setAdapter(mRowsAdapter);
    }

    private void addSeparator(ArrayObjectAdapter adapter) {
        HeaderItem separator = new HeaderItem("-------------------------");
        adapter.add(new ListRow(separator, new ArrayObjectAdapter(new SettingsItemPresenter())));
    }

    private void addSettings(ArrayObjectAdapter adapter) {
        ArrayObjectAdapter gridRowAdapter;

        HeaderItem settings = new HeaderItem("Settings");
        ArrayList<Server> serverArrayList = getActivity().getIntent().getParcelableArrayListExtra(getString(R.string.intent_servers));
        gridRowAdapter = new ArrayObjectAdapter(new SettingsItemPresenter(serverArrayList));
        gridRowAdapter.add(getString(R.string.pref_title_server_select));
        gridRowAdapter.add(getString(R.string.pref_title_sign_out));
        gridRowAdapter.add(getString(R.string.pref_title_connection));
        gridRowAdapter.add(getString(R.string.pref_title_select_theme));
        adapter.add(new ListRow(settings, gridRowAdapter));
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

    @Subscribe
    public void onSharesLoadFailed(ServerSharesLoadFailedEvent event) {

    }

    @Subscribe
    public void onFileOpening(FileOpeningEvent event) {
        setUpFile(event.getShare(), event.getFiles(), event.getFile());
    }

    private void setUpFile(ServerShare share, List<ServerFile> files, ServerFile file) {
        setUpFileActivity(share, files, file);
    }

    private void setUpFileActivity(ServerShare share, List<ServerFile> files, ServerFile file) {
        if (Intents.Builder.with(getActivity()).isServerFileSupported(file)) {
            startFileActivity(share, files, file);
        }
    }

    private void startFileActivity(ServerShare share, List<ServerFile> files, ServerFile file) {
        Intent intent = Intents.Builder.with(getActivity()).buildServerFileIntent(share, files, file);
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

    private enum FilesSort {
        NAME, MODIFICATION_TIME
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
