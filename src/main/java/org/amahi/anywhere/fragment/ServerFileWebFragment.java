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

import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebViewFragment;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerShare;
import org.amahi.anywhere.util.Fragments;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

public class ServerFileWebFragment extends WebViewFragment
{
	public static final Set<String> SUPPORTED_FORMATS;

	static {
		SUPPORTED_FORMATS = new HashSet<String>(Arrays.asList(
			"image/svg+xml",
			"text/html",
			"text/plain"
		));
	}

	@Inject
	ServerClient serverClient;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		setUpInjections();

		setUpFile();
	}

	private void setUpInjections() {
		AmahiApplication.from(getActivity()).inject(this);
	}

	private void setUpFile() {
		setUpFileContent();
	}

	private void setUpFileContent() {
		getWebView().loadUrl(getFileUri().toString());
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
}
