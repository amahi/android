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
import android.view.ViewGroup;
import android.widget.TextView;

import org.amahi.anywhere.R;
import org.amahi.anywhere.server.model.Server;
import org.amahi.anywhere.tv.activity.SettingsActivity;

import java.util.ArrayList;

public class SettingsItemPresenter extends Presenter {

    private static final int SETTINGS_ITEM_WIDTH = 400;
    private static final int SETTINGS_ITEM_HEIGHT = 200;
    private Context mContext;
    private ArrayList<Server> serverArrayList;

    public SettingsItemPresenter() {
    }

    public SettingsItemPresenter(ArrayList<Server> serverArrayList) {
        this.serverArrayList = serverArrayList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        mContext = parent.getContext();

        TextView view = new TextView(mContext);

        view.setLayoutParams(new ViewGroup.LayoutParams(SETTINGS_ITEM_WIDTH, SETTINGS_ITEM_HEIGHT));

        view.setFocusable(true);

        view.setFocusableInTouchMode(true);

        view.setBackgroundColor(Color.DKGRAY);

        view.setTextColor(Color.WHITE);

        view.setGravity(Gravity.CENTER);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, Object item) {
        TextView settingsTv = (TextView) viewHolder.view;

        final String settingsText = (String) item;

        settingsTv.setText(settingsText);

        settingsTv.setOnClickListener(v -> {
            if (settingsText.matches(mContext.getString(R.string.pref_title_server_select))) {
                Intent intent = new Intent(mContext, SettingsActivity.class);
                intent.putExtra(Intent.EXTRA_TEXT, mContext.getString(R.string.pref_title_server_select));
                intent.putParcelableArrayListExtra(mContext.getString(R.string.intent_servers), serverArrayList);
                mContext.startActivity(intent);
            }

            if (settingsText.matches(mContext.getString(R.string.pref_title_sign_out))) {
                mContext.startActivity(new Intent(mContext, SettingsActivity.class).putExtra(Intent.EXTRA_TEXT, mContext.getString(R.string.pref_title_sign_out)));
            } else if (settingsText.matches(mContext.getString(R.string.pref_title_connection))) {
                mContext.startActivity(new Intent(mContext, SettingsActivity.class).putExtra(Intent.EXTRA_TEXT, mContext.getString(R.string.pref_title_connection)));
            } else if (settingsText.matches(mContext.getString(R.string.pref_title_select_theme))) {
                mContext.startActivity(new Intent(mContext, SettingsActivity.class).putExtra(Intent.EXTRA_TEXT, mContext.getString(R.string.pref_title_select_theme)));
            }
        });
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {
    }
}
