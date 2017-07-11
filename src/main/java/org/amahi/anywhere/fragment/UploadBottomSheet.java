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

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.amahi.anywhere.R;
import org.amahi.anywhere.adapter.UploadOptionsAdapter;
import org.amahi.anywhere.model.UploadOption;

import java.util.ArrayList;

/**
 * Bottom sheet component for showing upload related options.
 * Extends {@link android.support.design.widget.BottomSheetDialog}
 */
public class UploadBottomSheet extends BottomSheetDialogFragment {

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
							 @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.upload_bottom_sheet, container);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		setUpListView(view);
	}

	private ArrayList<UploadOption> getListItems() {
		ArrayList<UploadOption> uploadOptions = new ArrayList<>();
		uploadOptions.add(new UploadOption(getString(R.string.upload_camera),
				R.drawable.ic_camera));
		uploadOptions.add(new UploadOption(getString(R.string.upload_photo),
				R.drawable.ic_cloud_upload));
		return uploadOptions;
	}

	private void setUpListView(View view) {
		UploadOptionsAdapter adapter = new UploadOptionsAdapter(getContext(), getListItems());
		ListView listView = (ListView) view.findViewById(R.id.upload_options_list);
		assert listView != null;
		listView.setAdapter(adapter);
	}
}
