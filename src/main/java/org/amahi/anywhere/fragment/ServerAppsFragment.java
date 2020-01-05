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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

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
    private ServerAppsAdapter mServerAppsAdapter;

    private RecyclerView mRecyclerView;

    ViewAnimator viewAnimator;
    private LinearLayout mEmptyLinearLayout;
    private LinearLayout mErrorLinearLayout;
    View rootView;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
         rootView = layoutInflater.inflate(R.layout.fragment_server_apps, container, false);

        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.layout_refresh_apps);

        mRecyclerView = rootView.findViewById(R.id.list_server_apps);

        mServerAppsAdapter = new ServerAppsAdapter(getActivity());

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        mEmptyLinearLayout = rootView.findViewById(R.id.empty_server_apps);

        mErrorLinearLayout = rootView.findViewById(R.id.error);

        check();

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                setUpAppsContent();
            }
        });


        mRecyclerView.addItemDecoration(new
            DividerItemDecoration(getActivity(),
            DividerItemDecoration.VERTICAL));
        return rootView;
    }





    private void check(){
        if(!isNetworkConnected()) {
            rootView.findViewById(R.id.empty2).setVisibility(View.GONE);
            rootView.findViewById(R.id.empty1).setVisibility(View.GONE);
            rootView.findViewById(R.id.MessageError1).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.MessageError2).setVisibility(View.VISIBLE);
        }else{
            rootView.findViewById(R.id.empty2).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.empty1).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.MessageError1).setVisibility(View.GONE);
            rootView.findViewById(R.id.MessageError2).setVisibility(View.GONE);
        }
        mSwipeRefreshLayout.setRefreshing(false);
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
        mRecyclerView.setAdapter(mServerAppsAdapter);
    }

    private void setUpAppsContent(Bundle state) {
        if (isAppsStateValid(state)) {
            setUpAppsState(state);
        } else {
            setUpAppsContent();
        }
    }

    private boolean isAppsStateValid(Bundle state) {
        return (state != null) && state.containsKey(State.APPS);
    }

    private void setUpAppsState(Bundle state) {
        List<ServerApp> apps = state.getParcelableArrayList(State.APPS);
        if (apps != null) {
            mEmptyLinearLayout.setVisibility(View.GONE);
            setUpAppsContent(apps);
            showAppsContent();
        }
        else {
            check();
            mErrorLinearLayout.setVisibility(View.VISIBLE);
        }
    }


    private void setUpAppsContent(List<ServerApp> apps) {
        getAppsAdapter().replaceWith(apps);
    }

    private ServerAppsAdapter getAppsAdapter() {
        return mServerAppsAdapter;
    }

    private void showAppsContent() {
        ViewDirector.of(this, R.id.animator).show(R.id.content);
    }

    private void setUpAppsContent() {
        if (serverClient.isConnected()) {
            serverClient.getApps();
        }
    }

    @Subscribe
    public void onServerConnectionChanged(ServerConnectionChangedEvent event) {
        serverClient.getApps();
    }

    @Subscribe
    public void onAppsLoaded(ServerAppsLoadedEvent event) {
        mSwipeRefreshLayout.setRefreshing(false);
        setUpAppsContent(event.getServerApps());

        showAppsContent();
    }

    @Subscribe
    public void onAppsLoadFailed(ServerAppsLoadFailedEvent event) {
        showAppsError();
    }

    private void showAppsError() {
        mSwipeRefreshLayout.setRefreshing(false);
        check();
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
