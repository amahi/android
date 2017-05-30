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

package org.amahi.anywhere.amahitv.fragment;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;

import com.squareup.otto.Subscribe;

import org.amahi.anywhere.amahitv.AmahiApplication;
import org.amahi.anywhere.amahitv.R;
import org.amahi.anywhere.amahitv.bus.BusProvider;
import org.amahi.anywhere.amahitv.bus.FileDownloadFailedEvent;
import org.amahi.anywhere.amahitv.bus.FileDownloadedEvent;
import org.amahi.anywhere.amahitv.server.client.ServerClient;
import org.amahi.anywhere.amahitv.server.model.ServerFile;
import org.amahi.anywhere.amahitv.server.model.ServerShare;
import org.amahi.anywhere.amahitv.util.Downloader;
import org.amahi.anywhere.amahitv.util.Fragments;

import javax.inject.Inject;

/**
 * File downloading dialog.
 */
public class ServerFileDownloadingFragment extends DialogFragment
{
	public static final String TAG = "server_file_downloading";

	@Inject
	Downloader downloader;

	@Inject
	ServerClient serverClient;

	public static ServerFileDownloadingFragment newInstance(ServerShare share, ServerFile file) {
		ServerFileDownloadingFragment fragment = new ServerFileDownloadingFragment();

		Bundle arguments = new Bundle();
		arguments.putParcelable(Fragments.Arguments.SERVER_SHARE, share);
		arguments.putParcelable(Fragments.Arguments.SERVER_FILE, file);
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
			downloader.startFileDownloading(getFileUri(), getFile().getName());
		}
	}

	private Uri getFileUri() {
		return serverClient.getFileUri(getShare(), getFile());
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
