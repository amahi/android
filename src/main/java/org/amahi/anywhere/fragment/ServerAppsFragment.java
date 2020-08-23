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

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.l4digital.fastscroll.FastScrollView;
import com.squareup.otto.Subscribe;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.R;
import org.amahi.anywhere.adapter.ServerAppsAdapter;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.ServerAppsLoadFailedEvent;
import org.amahi.anywhere.bus.ServerAppsLoadedEvent;
import org.amahi.anywhere.bus.ServerConnectionChangedEvent;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.ServerApp;
import org.amahi.anywhere.util.ViewDirector;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Apps fragment. Shows apps list.
 */
public class ServerAppsFragment extends Fragment {
    @Inject
    ServerClient serverClient;
    View rootView;

    private ServerAppsAdapter mServerAppsAdapter;
    private FastScrollView mFastScrollView;
    private LinearLayout mErrorLinearLayout;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = layoutInflater.inflate(R.layout.fragment_server_apps, container, false);

        mSwipeRefreshLayout = rootView.findViewById(R.id.layout_refresh_apps);
        mFastScrollView = rootView.findViewById(R.id.list_server_apps);

        mServerAppsAdapter = new ServerAppsAdapter(getActivity());

        mFastScrollView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        mErrorLinearLayout = rootView.findViewById(R.id.error);

        setVisibilityIfNetworkConnected();

        mSwipeRefreshLayout.setOnRefreshListener(this::setUpAppsContent);

        mFastScrollView.getRecyclerView().addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        return rootView;
    }

    private void setVisibilityIfNetworkConnected() {
        if (!isNetworkConnected()) {
            setNoAppsMsgVisibility(View.GONE);
            setConnErrorMsgVisibility(View.VISIBLE);
        } else {
            setNoAppsMsgVisibility(View.VISIBLE);
            setConnErrorMsgVisibility(View.GONE);
        }
        mSwipeRefreshLayout.setRefreshing(false);
    }

    private void setNoAppsMsgVisibility(int visibility) {
        if (rootView != null) {
            rootView.findViewById(R.id.empty1).setVisibility(visibility);
            rootView.findViewById(R.id.empty2).setVisibility(visibility);
        }
    }

    private void setConnErrorMsgVisibility(int visibility) {
        if (rootView != null) {
            rootView.findViewById(R.id.MessageError1).setVisibility(visibility);
            rootView.findViewById(R.id.MessageError2).setVisibility(visibility);
        }
    }

    private boolean isNetworkConnected() {
        try {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            return mNetworkInfo != null;

        } catch (NullPointerException e) {
            return false;

        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setUpInjections();

        setUpApps(savedInstanceState);
    }

    private void setUpInjections() {
        AmahiApplication.from(getActivity()).inject(this);
    }

    private void setUpApps(Bundle state) {
        setUpAppsAdapter();
        setUpAppsContent(state);
    }

    private void setUpAppsAdapter() {
        mFastScrollView.setAdapter(mServerAppsAdapter);
    }

    private boolean isAppsStateValid(Bundle state) {
        return (state != null) && state.containsKey(State.APPS);
    }

    private void setUpAppsContent(Bundle state) {
        if (isAppsStateValid(state)) {
            setUpAppsState(state);
        } else {
            setUpAppsContent();
        }
    }

    private void setUpAppsContent(List<ServerApp> apps) {
        getAppsAdapter().replaceWith(apps);
    }

    private void setUpAppsContent() {
        if (serverClient.isConnected()) {
            serverClient.getApps();
        }
    }

    private void setUpAppsState(Bundle state) {
        List<ServerApp> apps = state.getParcelableArrayList(State.APPS);
        if (apps != null && apps.size() != 0) {
            setUpAppsContent(apps);
            showAppsContent();
        } else {
            setVisibilityIfNetworkConnected();
            showAppsError();
        }
    }

    private ServerAppsAdapter getAppsAdapter() {
        return mServerAppsAdapter;
    }

    private void showAppsContent() {
        ViewDirector.of(this, R.id.animator).show(R.id.content);
    }

    @Subscribe
    public void onServerConnectionChanged(ServerConnectionChangedEvent event) {
        serverClient.getApps();
    }

    @Subscribe
    public void onAppsLoaded(ServerAppsLoadedEvent event) {
        mSwipeRefreshLayout.setRefreshing(false);
        setUpAppsContent(event.getServerApps());

        if (event.getServerApps() != null && event.getServerApps().size() != 0) {
            showAppsContent();
        } else {
            showAppsError();
        }
    }

    @Subscribe
    public void onAppsLoadFailed(ServerAppsLoadFailedEvent event) {
        showAppsError();
    }

    private void showAppsError() {
        mSwipeRefreshLayout.setRefreshing(false);
        setVisibilityIfNetworkConnected();
        ViewDirector.of(this, R.id.animator).show(R.id.error);
        mErrorLinearLayout.setOnClickListener(view -> {
            ViewDirector.of(getActivity(), R.id.animator).show(android.R.id.progress);
            setUpAppsContent();
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

        tearDownAppsState(outState);
    }

    private void tearDownAppsState(Bundle state) {
        if (areAppsLoaded()) {
            state.putParcelableArrayList(State.APPS, new ArrayList<Parcelable>(getAppsAdapter().getItems()));
        }
    }

    private boolean areAppsLoaded() {
        return getAppsAdapter() != null;
    }

    private static final class State {
        public static final String APPS = "apps";

        private State() {
        }
    }
}
