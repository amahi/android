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

package org.amahi.anywhere.tv.presenter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v17.leanback.widget.Presenter;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.FileOpeningEvent;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.tv.activity.ServerFileTvActivity;
import org.amahi.anywhere.util.Mimes;

import java.util.List;

public class GridItemPresenter extends Presenter {

    private static final int GRID_ITEM_WIDTH = 400;
    private static final int GRID_ITEM_HEIGHT = 300;
    private Context mContext;
    private List<ServerFile> mServerFileList;

    public GridItemPresenter(Context context, List<ServerFile> serverFileList) {
        mContext = context;
        mServerFileList = serverFileList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        TextView view = new TextView(parent.getContext());

        view.setLayoutParams(new ViewGroup.LayoutParams(GRID_ITEM_WIDTH, GRID_ITEM_HEIGHT));

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
        final ServerFile file = (ServerFile) item;
        textView.setText(file.getName());

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isDirectory(file)) {
                    mContext.startActivity(new Intent(mContext, ServerFileTvActivity.class));
                } else {
                    startFileOpening(file);
                }
            }
        });
    }

    private void startFileOpening(ServerFile file) {
        BusProvider.getBus().post(new FileOpeningEvent(file.getParentShare(), mServerFileList, file));
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {
    }

    private boolean isDirectory(ServerFile file) {
        return Mimes.match(file.getMime()) == Mimes.Type.DIRECTORY;
    }
}

