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
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.PresenterSelector;

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
import org.amahi.anywhere.tv.presenter.IconHeaderPresenter;
import org.amahi.anywhere.tv.presenter.MainTVPresenter;
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
    private ListRow settingsRow;

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

    private void setupUIElements() {
        setTitle(getString(R.string.app_title));

        setHeadersState(HEADERS_ENABLED);

        setHeaderPresenter();

        setHeadersTransitionOnBackEnabled(true);

        setBrandColor(Color.parseColor("#0277bd"));

        setSearchAffordanceColor(Color.GREEN);
    }

    private void setHeaderPresenter() {
        setHeaderPresenterSelector(new PresenterSelector() {
            @Override
            public Presenter getPresenter(Object item) {
                return new IconHeaderPresenter();
            }
        });
    }

    private void loadRows() {
        mRowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        addSettings(mRowsAdapter);
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
        ListRow listRow = null;
        ArrayObjectAdapter gridRowAdapter = new ArrayObjectAdapter(new MainTVPresenter(getActivity(), serverClient, serverFiles));
        if (serverFiles.size() != 0) {
            String shareName = serverFiles.get(0).getParentShare().getName();
            for (int i = 0; i < serverFiles.size(); i++) {
                gridRowAdapter.add(serverFiles.get(i));
            }
            for (int i = 0; i < serverShareList.size(); i++) {
                if (shareName.matches(serverShareList.get(i).getName())) {
                    HeaderItem headerItem = new HeaderItem(shareName);
                    listRow = new ListRow(headerItem, gridRowAdapter);
                    mRowsAdapter.add(listRow);
                    serverShareList.remove(i);
                    break;
                }
            }
        }

        if (listRow != null) {
            int index1 = mRowsAdapter.indexOf(listRow);
            int index2 = mRowsAdapter.indexOf(settingsRow);
            mRowsAdapter.replace(index1, settingsRow);
            mRowsAdapter.replace(index2, listRow);
        }
        sortHeaders();
        setAdapter(mRowsAdapter);
    }

    private void addSettings(ArrayObjectAdapter adapter) {
        ArrayObjectAdapter gridRowAdapter;

        HeaderItem settings = new HeaderItem("Settings");
        ArrayList<Server> serverArrayList = getActivity().getIntent().getParcelableArrayListExtra(getString(R.string.intent_servers));
        gridRowAdapter = new ArrayObjectAdapter(new SettingsItemPresenter(serverArrayList));
        gridRowAdapter.add(getString(R.string.pref_title_server_select));
        gridRowAdapter.add(getString(R.string.pref_title_sign_out));
        gridRowAdapter.add(getString(R.string.pref_title_connection));
//        Note - @octacode: Theme settings haven't been implemented yet.
//        gridRowAdapter.add(getString(R.string.pref_title_select_theme));
        settingsRow = new ListRow(settings, gridRowAdapter);
        adapter.add(0, settingsRow);
    }

    private void sortHeaders() {
        for (int i = 0; i < mRowsAdapter.size() - 1; i++) {
            for (int j = i + 1; j < mRowsAdapter.size() - 1; j++) {
                ListRow listRow1 = (ListRow) mRowsAdapter.get(i);
                ListRow listRow2 = (ListRow) mRowsAdapter.get(j);
                if ((int) listRow2.getHeaderItem().getName().charAt(0) < (int) listRow1.getHeaderItem().getName().charAt(0)) {
                    int index1 = mRowsAdapter.indexOf(listRow1);
                    int index2 = mRowsAdapter.indexOf(listRow2);
                    mRowsAdapter.replace(index1, listRow2);
                    mRowsAdapter.replace(index2, listRow1);
                }
            }
        }
    }

    private List<ServerFile> sortFiles(List<ServerFile> files) {
        List<ServerFile> sortedFiles = new ArrayList<>(files);

        Collections.sort(sortedFiles, getFilesComparator());

        return sortedFiles;
    }

    private Comparator<ServerFile> getFilesComparator() {
        switch (filesSort) {
            case NAME:
                return ServerFile::compareByName;

            case MODIFICATION_TIME:
                return ServerFile::compareByModificationTime;

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
}
