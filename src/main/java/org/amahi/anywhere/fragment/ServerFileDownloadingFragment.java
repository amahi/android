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

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;

import com.squareup.otto.Subscribe;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.R;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.FileDownloadFailedEvent;
import org.amahi.anywhere.bus.FileDownloadedEvent;
import org.amahi.anywhere.db.entities.RecentFile;
import org.amahi.anywhere.db.repositories.RecentFileRepository;
import org.amahi.anywhere.model.FileOption;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerShare;
import org.amahi.anywhere.util.Downloader;
import org.amahi.anywhere.util.Fragments;

import javax.inject.Inject;

/**
 * File downloading dialog.
 */
public class ServerFileDownloadingFragment extends DialogFragment {
    public static final String TAG = "server_file_downloading";

    @Inject
    Downloader downloader;

    @Inject
    ServerClient serverClient;

    public static ServerFileDownloadingFragment newInstance(ServerShare share, ServerFile file, @FileOption.Types int fileAction) {
        ServerFileDownloadingFragment fragment = new ServerFileDownloadingFragment();

        Bundle arguments = new Bundle();
        arguments.putParcelable(Fragments.Arguments.SERVER_SHARE, share);
        arguments.putParcelable(Fragments.Arguments.SERVER_FILE, file);
        arguments.putInt(Fragments.Arguments.FILE_OPTION, fileAction);
        fragment.setArguments(arguments);

        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ProgressDialog dialog = new ProgressDialog(getActivity());

        dialog.setMessage(getString(R.string.message_progress_file_downloading));

        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setUpInjections();

        startFileDownloading(savedInstanceState);
    }

    private void setUpInjections() {
        AmahiApplication.from(getActivity()).inject(this);
    }

    private void startFileDownloading(Bundle state) {
        if (state == null) {
            int fileOption = getArguments().getInt(Fragments.Arguments.FILE_OPTION, 0);
            downloader.startFileDownloading(getFileUri(), getFile().getName(), fileOption);
        }
    }

    private Uri getFileUri() {
        if (getShare() != null) {
            return serverClient.getFileUri(getShare(), getFile());
        } else {
            return getUriFrom(getRecentFile(getFile()));
        }
    }

    private Uri getUriFrom(RecentFile recentFile) {
        return Uri.parse(recentFile.getUri());
    }

    private RecentFile getRecentFile(ServerFile file) {
        RecentFileRepository repository = new RecentFileRepository(getActivity());
        return repository.getRecentFile(file.getUniqueKey());
    }

    private ServerShare getShare() {
        return getArguments().getParcelable(Fragments.Arguments.SERVER_SHARE);
    }

    private ServerFile getFile() {
        return getArguments().getParcelable(Fragments.Arguments.SERVER_FILE);
    }

    @Subscribe
    public void onFileDownloaded(FileDownloadedEvent event) {
        dismiss();
    }

    @Subscribe
    public void onFileDownloadFailed(FileDownloadFailedEvent event) {
        dismiss();
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
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);

        finishFileDownloading();
    }

    private void finishFileDownloading() {
        downloader.finishFileDownloading();
    }
}
