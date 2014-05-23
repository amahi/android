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

import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ViewAnimator;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.R;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerShare;
import org.amahi.anywhere.util.Fragments;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

public class ServerFileImageFragment extends Fragment implements Callback
{
	public static final Set<String> SUPPORTED_FORMATS;

	static {
		SUPPORTED_FORMATS = new HashSet<String>(Arrays.asList(
			"image/bmp",
			"image/jpeg",
			"image/gif",
			"image/png",
			"image/webp"
		));
	}

	@Inject
	ServerClient serverClient;

	@Override
	public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
		return layoutInflater.inflate(R.layout.fragment_server_file_image, container, false);
	}

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
		Picasso
			.with(getActivity())
			.load(getFileUri())
			.fit()
			.centerInside()
			.into(getImageView(), this);
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

	private ImageView getImageView() {
		return (ImageView) getView().findViewById(R.id.image);
	}

	@Override
	public void onSuccess() {
		showFileContent();
	}

	private void showFileContent() {
		ViewAnimator animator = (ViewAnimator) getView().findViewById(R.id.animator);
		animator.setDisplayedChild(animator.indexOfChild(getView().findViewById(R.id.image)));
	}

	@Override
	public void onError() {
	}
}
