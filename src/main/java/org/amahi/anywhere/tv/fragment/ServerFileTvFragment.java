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

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v17.leanback.app.VerticalGridFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.VerticalGridPresenter;

import com.squareup.otto.Subscribe;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.ServerFilesLoadedEvent;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerShare;
import org.amahi.anywhere.tv.presenter.ServerFileTvPresenter;
import org.amahi.anywhere.util.Intents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

public class ServerFileTvFragment extends VerticalGridFragment {

    @Inject
    ServerClient serverClient;

    private ServerShare mServerShare;
    private ServerFile mServerFile;
    private ArrayObjectAdapter mAdapter;
    private List<ServerFile> mServerFileList;
    private FilesSort filesSort = FilesSort.MODIFICATION_TIME;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpInjections();
        mServerShare = getArguments().getParcelable(Intents.Extras.SERVER_SHARE);
        mServerFile = getArguments().getParcelable(Intents.Extras.SERVER_FILE);
        setTitle(mServerFile.getName());

        setUpFragment();
    }

    private void setUpInjections() {
        AmahiApplication.from(getActivity()).inject(this);
    }

    private void setUpFragment() {
        VerticalGridPresenter gridPresenter = new VerticalGridPresenter();
        gridPresenter.setNumberOfColumns(4);
        setGridPresenter(gridPresenter);
        setContent();
        mAdapter = new ArrayObjectAdapter(new ServerFileTvPresenter(getFragmentManager(), mServerFileList));
    }

    private void setContent() {
        if (serverClient.isConnected()) {
            serverClient.getFiles(mServerShare, mServerFile);
        }
    }

    @Subscribe
    public void onFilesLoaded(ServerFilesLoadedEvent event) {
        mServerFileList = sortFiles(event.getServerFiles());
        for (int i = 0; i < mServerFileList.size(); i++)
            mAdapter.add(mServerFileList.get(i));
        setAdapter(mAdapter);
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
