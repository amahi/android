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

import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import org.amahi.anywhere.R;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.util.Fragments;
import org.amahi.anywhere.util.Intents;

/**
 * Application search dialog.
 */
public class GooglePlaySearchFragment extends DialogFragment implements DialogInterface.OnClickListener
{
	public static final String TAG = "google_play_search";

	public static GooglePlaySearchFragment newInstance(ServerFile file) {
		GooglePlaySearchFragment fragment = new GooglePlaySearchFragment();

		fragment.setArguments(buildArguments(file));

		return fragment;
	}

	private static Bundle buildArguments(ServerFile file) {
		Bundle arguments = new Bundle();

		arguments.putParcelable(Fragments.Arguments.SERVER_FILE, file);

		return arguments;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		return buildDialog();
	}

	private Dialog buildDialog() {
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());

		dialogBuilder.setMessage(getString(R.string.message_error_search_application));
		dialogBuilder.setPositiveButton(getString(R.string.button_search_application), this);

		return dialogBuilder.create();
	}

	@Override
	public void onClick(DialogInterface dialog, int id) {
		this.dismiss();

		startGooglePlaySearch();
	}

	private void startGooglePlaySearch() {
		String search = getFile().getMime();

		Intent intent = Intents.Builder.with(getActivity()).buildGooglePlaySearchIntent(search);
		if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
			startActivity(intent);
		}
		else {
			Snackbar.make(getView(),getString(R.string.application_not_found),Snackbar.LENGTH_SHORT).show();
		}
	}

	private ServerFile getFile() {
		return getArguments().getParcelable(Fragments.Arguments.SERVER_FILE);
	}
}
