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

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v17.leanback.app.VerticalGridFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.VerticalGridPresenter;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.R;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.FileOpeningEvent;
import org.amahi.anywhere.bus.ServerFilesLoadedEvent;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerShare;
import org.amahi.anywhere.util.Intents;
import org.amahi.anywhere.util.Mimes;

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
        mAdapter = new ArrayObjectAdapter(new ServerFileTvPresenter());
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
        List<ServerFile> sortedFiles = new ArrayList<>(files);

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

    private class ServerFileTvPresenter extends Presenter {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent) {
            TextView view = new TextView(parent.getContext());
            view.setLayoutParams(new ViewGroup.LayoutParams(400, 300));
            view.setFocusable(true);
            view.setFocusableInTouchMode(true);
            view.setBackgroundColor(Color.DKGRAY);
            view.setTextColor(Color.WHITE);
            view.setTextSize(20);
            view.setGravity(Gravity.CENTER);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, Object item) {
            TextView textView = (TextView) viewHolder.view;
            final ServerFile serverFile = (ServerFile) item;
            textView.setText(serverFile.getName());
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isDirectory(serverFile)) {
                        setFragment(serverFile, serverFile.getParentShare());
                    } else {
                        startFileOpening(serverFile);
                    }
                }
            });
        }

        private void setFragment(ServerFile serverFile, ServerShare serverShare) {
            getFragmentManager().beginTransaction().replace(R.id.server_file_tv_container, buildTvFragment(serverFile, serverShare), getClass().getSimpleName()).addToBackStack(getClass().getSimpleName()).commit();
        }

        private Fragment buildTvFragment(ServerFile serverFile, ServerShare serverShare) {
            Fragment fragment = new ServerFileTvFragment();
            Bundle bundle = new Bundle();
            bundle.putParcelable(Intents.Extras.SERVER_FILE, serverFile);
            bundle.putParcelable(Intents.Extras.SERVER_SHARE, serverShare);
            fragment.setArguments(bundle);
            return fragment;
        }

        private void startFileOpening(ServerFile file) {
            BusProvider.getBus().post(new FileOpeningEvent(file.getParentShare(), mServerFileList, file));
        }

        private boolean isDirectory(ServerFile file) {
            return Mimes.match(file.getMime()) == Mimes.Type.DIRECTORY;
        }

        @Override
        public void onUnbindViewHolder(ViewHolder viewHolder) {
        }
    }
}
