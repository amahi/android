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
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ListView;

import org.amahi.anywhere.R;
import org.amahi.anywhere.adapter.UploadOptionsAdapter;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.UploadClickEvent;
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
        View rootView = inflater.inflate(R.layout.upload_bottom_sheet, container);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) getDialog();
                FrameLayout bottomSheet = bottomSheetDialog.findViewById(android.support.design.R.id.design_bottom_sheet);
                assert bottomSheet != null;
                BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setPeekHeight(0);
            }
        });
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpListView(view);
    }

    private ArrayList<UploadOption> getListItems() {
        ArrayList<UploadOption> uploadOptions = new ArrayList<>();

        uploadOptions.add(new UploadOption(UploadOption.CAMERA,
            getString(R.string.upload_camera),
            R.drawable.ic_camera));

        uploadOptions.add(new UploadOption(UploadOption.FILE,
            getString(R.string.upload_file),
            R.drawable.ic_cloud_upload));

        return uploadOptions;
    }

    private void setUpListView(View view) {
        UploadOptionsAdapter adapter = new UploadOptionsAdapter(getContext(), getListItems());
        ListView listView = view.findViewById(R.id.upload_options_list);
        assert listView != null;
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view1, position, id) -> {
            UploadOption uploadOption = getListItems().get(position);
            BusProvider.getBus().post(new UploadClickEvent(uploadOption.getType()));
            dismiss();
        });
    }
}
