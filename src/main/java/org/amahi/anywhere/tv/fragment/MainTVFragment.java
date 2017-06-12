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

import android.graphics.Color;
import android.os.Bundle;
import android.support.v17.leanback.app.BrowseFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;

import org.amahi.anywhere.R;
import org.amahi.anywhere.tv.presenter.CardPresenter;
import org.amahi.anywhere.tv.presenter.GridItemPresenter;
import org.amahi.anywhere.tv.presenter.SettingsItemPresenter;
import org.amahi.anywhere.server.model.Server;
import org.amahi.anywhere.server.model.ServerApp;
import org.amahi.anywhere.server.model.ServerShare;

import java.util.ArrayList;

public class MainTVFragment extends BrowseFragment {

    private ArrayList<ServerApp> serverAppArrayList;
    private ArrayList<ServerShare> serverShareArrayList;
    private ArrayList<Server> serverArrayList;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        serverAppArrayList = getActivity().getIntent().getParcelableArrayListExtra(getString(R.string.intent_apps));

        serverShareArrayList = getActivity().getIntent().getParcelableArrayListExtra(getString(R.string.intent_shares));

        serverArrayList = getActivity().getIntent().getParcelableArrayListExtra(getString(R.string.intent_servers));

        setupUIElements();

        loadRows(0);
    }

    private void setupUIElements() {
        setTitle(getString(R.string.app_title));

        setHeadersState(HEADERS_ENABLED);

        setHeadersTransitionOnBackEnabled(true);

        setBrandColor(Color.parseColor("#0277bd"));

        setSearchAffordanceColor(Color.GREEN);
    }

    private void loadRows(int index) {
        ArrayObjectAdapter mRowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());

        HeaderItem server = new HeaderItem(index, getString(R.string.row_servers));

        HeaderItem shares = new HeaderItem(index, getString(R.string.row_shares));

        HeaderItem apps = new HeaderItem(index, getString(R.string.row_apps));

        HeaderItem settings = new HeaderItem(index,getString(R.string.row_preferences));

        GridItemPresenter mGridPresenter = new GridItemPresenter();

        ArrayObjectAdapter gridRowAdapter = new ArrayObjectAdapter(mGridPresenter);

        if(serverArrayList!=null)
            for(int i=0;i<serverArrayList.size();i++) gridRowAdapter.add(serverArrayList.get(i).getName());

        mRowsAdapter.add(new ListRow(server, gridRowAdapter));

        setAdapter(mRowsAdapter);

        gridRowAdapter = new ArrayObjectAdapter(mGridPresenter);

        if(serverShareArrayList!=null)
            for(int i=0;i<serverShareArrayList.size();i++) gridRowAdapter.add(i,serverShareArrayList.get(i).getName());

        mRowsAdapter.add(new ListRow(shares, gridRowAdapter));

        gridRowAdapter = new ArrayObjectAdapter(new CardPresenter());

        if(serverAppArrayList!=null)
            for(int i=0;i<serverAppArrayList.size();i++) gridRowAdapter.add(i,serverAppArrayList.get(i));

        mRowsAdapter.add(new ListRow(apps,gridRowAdapter));

        SettingsItemPresenter settingsItemPresenter = new SettingsItemPresenter();

        gridRowAdapter = new ArrayObjectAdapter(settingsItemPresenter);

        gridRowAdapter.add(getString(R.string.pref_title_sign_out));

        gridRowAdapter.add(getString(R.string.pref_title_connection));

        gridRowAdapter.add(getString(R.string.pref_title_select_theme));

        mRowsAdapter.add(new ListRow(settings,gridRowAdapter));
    }
}