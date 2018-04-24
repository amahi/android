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
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.R;
import org.amahi.anywhere.adapter.ServerSharesAdapter;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.ServerConnectionChangedEvent;
import org.amahi.anywhere.bus.ServerConnectionFailedEvent;
import org.amahi.anywhere.bus.ServerReconnectEvent;
import org.amahi.anywhere.bus.ServerSharesLoadFailedEvent;
import org.amahi.anywhere.bus.ServerSharesLoadedEvent;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.ServerShare;
import org.amahi.anywhere.util.ViewDirector;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Shares fragment. Shows shares list.
 */
public class ServerSharesFragment extends Fragment implements
    SwipeRefreshLayout.OnRefreshListener {

    @Inject
    ServerClient serverClient;
    private RecyclerView mRecyclerView;

    private ServerSharesAdapter mServerSharesAdapter;

    private LinearLayout mEmptyLinearLayout;

    private LinearLayout mErrorLinearLayout;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = layoutInflater.inflate(R.layout.fragment_server_shares, container, false);

        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.layout_refresh);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.list);

        mServerSharesAdapter = new ServerSharesAdapter(getActivity());

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()
            , LinearLayoutManager.VERTICAL, false));

        mEmptyLinearLayout = (LinearLayout) rootView.findViewById(R.id.empty);

        mErrorLinearLayout = (LinearLayout) rootView.findViewById(R.id.error);

        setSwipeToRefresh();

        mRecyclerView.addItemDecoration(new
            DividerItemDecoration(getActivity(),
            DividerItemDecoration.VERTICAL));

        return rootView;
    }

    private void setSwipeToRefresh() {
        mSwipeRefreshLayout.setOnRefreshListener(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setUpInjections();

        setUpShares(savedInstanceState);
    }

    private void setUpInjections() {
        AmahiApplication.from(getActivity()).inject(this);
    }

    private void setUpShares(Bundle state) {
        setUpSharesAdapter();

        setUpSharesContent(state);
    }

    private void setUpSharesAdapter() {
        mRecyclerView.setAdapter(mServerSharesAdapter);
    }

    private void setUpSharesContent(Bundle state) {
        if (isSharesStateValid(state)) {
            setUpSharesState(state);
        } else {
            setUpSharesContent();
        }
    }

    private boolean isSharesStateValid(Bundle state) {
        return (state != null) && (state.containsKey(State.SHARES));
    }

    private void setUpSharesState(Bundle state) {
        List<ServerShare> shares = state.getParcelableArrayList(State.SHARES);
        if (shares != null) {
            setUpSharesContent(shares);

            showSharesContent();

            mEmptyLinearLayout.setVisibility(View.GONE);
        } else {

            mEmptyLinearLayout.setVisibility(View.VISIBLE);

        }
    }

    private void setUpSharesContent(List<ServerShare> shares) {
        getSharesAdapter().replaceWith(shares);
    }

    private ServerSharesAdapter getSharesAdapter() {
        return mServerSharesAdapter;
    }

    private void showSharesContent() {
        ViewDirector.of(getActivity(), R.id.animator).show(R.id.content);
    }

    private void setUpSharesContent() {
        if (serverClient.isConnected()) {
            serverClient.getShares();
        }
    }

    @Subscribe
    public void onServerConnectionFailed(ServerConnectionFailedEvent event) {
        Toast.makeText(getContext(), getResources()
            .getString(R.string.message_error_amahi_anywhere_app), Toast.LENGTH_LONG).show();
        showConnectionError();
    }

    private void showConnectionError() {
        ViewDirector.of(getActivity(), R.id.animator).show(R.id.error);
        mErrorLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ViewDirector.of(getActivity(), R.id.animator).show(android.R.id.progress);
                retryServerConnection();
            }
        });
    }

    private void retryServerConnection() {
        BusProvider.getBus().post(new ServerReconnectEvent());
    }

    @Subscribe
    public void onServerConnectionChanged(ServerConnectionChangedEvent event) {
        setUpSharesContent();
    }

    @Subscribe
    public void onSharesLoaded(ServerSharesLoadedEvent event) {
        mSwipeRefreshLayout.setRefreshing(false);
        setUpSharesContent(event.getServerShares());
        showSharesContent();
    }

    @Subscribe
    public void onSharesLoadFailed(ServerSharesLoadFailedEvent event) {
        showSharesError();
    }

    private void showSharesError() {
        mSwipeRefreshLayout.setRefreshing(false);
        ViewDirector.of(getActivity(), R.id.animator).show(R.id.error);
        mErrorLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ViewDirector.of(getActivity(), R.id.animator).show(android.R.id.progress);
                setUpSharesContent();
            }
        });
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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        tearDownSharesState(outState);
    }

    private void tearDownSharesState(Bundle state) {
        if (areSharesLoaded()) {
            state.putParcelableArrayList(State.SHARES, new ArrayList<Parcelable>(getSharesAdapter().getItems()));
        }
    }

    private boolean areSharesLoaded() {
        return getSharesAdapter() != null;
    }

    @Override
    public void onRefresh() {
        setUpSharesContent();
    }

    private static final class State {
        public static final String SHARES = "shares";

        private State() {
        }
    }
}
