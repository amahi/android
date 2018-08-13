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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v17.leanback.app.GuidedStepFragment;
import android.support.v17.leanback.widget.GuidanceStylist;
import android.support.v17.leanback.widget.GuidedAction;
import android.support.v4.content.ContextCompat;

import com.squareup.otto.Subscribe;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.R;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.ServerAuthenticationStartEvent;
import org.amahi.anywhere.bus.ServerConnectedEvent;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.Server;
import org.amahi.anywhere.util.Intents;
import org.amahi.anywhere.util.Preferences;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import static android.app.Activity.RESULT_OK;
import static org.amahi.anywhere.activity.NavigationActivity.PIN_REQUEST_CODE;

public class ServerSelectFragment extends GuidedStepFragment {

    private static final int OPTION_CHECK_SET_ID = 10;

    @Inject
    ServerClient serverClient;

    private int indexSelected = 0;
    private Context mContext;
    private ArrayList<Server> mServerArrayList;
    private ArrayList<String> OPTION_NAMES = new ArrayList<>();
    private ArrayList<String> OPTION_DESCRIPTIONS = new ArrayList<>();
    private ArrayList<Boolean> OPTION_CHECKED = new ArrayList<>();

    @SuppressLint("ValidFragment")
    public ServerSelectFragment(Context context) {
        mContext = context;
    }

    public ServerSelectFragment() {
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setUpInjections();
    }

    private void setUpInjections() {
        AmahiApplication.from(getActivity()).inject(this);
    }

    @NonNull
    @Override
    public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {
        return new GuidanceStylist.Guidance(getString(R.string.pref_title_server_select),
            getString(R.string.pref_title_server_select_desc),
            "",
            ContextCompat.getDrawable(getActivity(), R.drawable.ic_app_logo_shadowless));
    }

    @Override
    public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
        super.onCreateActions(actions, savedInstanceState);
        mServerArrayList = getActivity().getIntent().getParcelableArrayListExtra(getString(R.string.intent_servers));

        setTitle(actions);
        populateData();
        String serverSession = Preferences.getServerSession(mContext);
        if (serverSession == null) {
            setDefaultChecked();
        } else {
            for (int i = 0; i < mServerArrayList.size(); i++) {
                if (serverSession.equals(mServerArrayList.get(i).getSession())) {
                    indexSelected = i;
                    break;
                }
            }
            setFalseChecked();
            OPTION_CHECKED.set(indexSelected, true);
        }

        setCheckedActionButtons(actions);
    }

    private void setTitle(List<GuidedAction> actions) {
        String title = getString(R.string.pref_title_server_active_list);

        String desc = getString(R.string.pref_server_active_list_desc);

        actions.add(new GuidedAction.Builder(mContext)
            .title(title)
            .description(desc)
            .multilineDescription(true)
            .infoOnly(true)
            .enabled(false)
            .build());
    }

    private void populateData() {
        for (int i = 0; i < mServerArrayList.size(); i++) {
            setName(i);
            setDesc();
        }
    }

    private void setName(int i) {
        OPTION_NAMES.add(mServerArrayList.get(i).getName());
    }

    private void setDesc() {
        OPTION_DESCRIPTIONS.add("");
    }

    private void setDefaultChecked() {
        OPTION_CHECKED.add(true);
        for (int i = 1; i < mServerArrayList.size(); i++)
            OPTION_CHECKED.add(false);
    }

    private void setFalseChecked() {
        if (OPTION_CHECKED != null) {
            setFalse();
        } else {
            OPTION_CHECKED.clear();
            setFalse();
        }
    }

    private void setFalse() {
        for (int i = 0; i < mServerArrayList.size(); i++)
            OPTION_CHECKED.add(false);
    }

    private void setCheckedActionButtons(List<GuidedAction> actions) {
        for (int i = 0; i < OPTION_NAMES.size(); i++) {
            addCheckedAction(actions,

                R.drawable.ic_app_logo,

                getActivity(),

                OPTION_NAMES.get(i),

                OPTION_DESCRIPTIONS.get(i),

                OPTION_CHECKED.get(i));
        }
    }

    private void addCheckedAction(List<GuidedAction> actions, int iconResId, Context context,
                                  String title, String desc, boolean checked) {

        GuidedAction guidedAction = new GuidedAction.Builder(context)
            .title(title)
            .description(desc)
            .checkSetId(OPTION_CHECK_SET_ID)
            .icon(iconResId)
            .build();

        guidedAction.setChecked(checked);

        actions.add(guidedAction);
    }

    @Override
    public void onGuidedActionClicked(GuidedAction action) {
        if (getSelectedActionPosition() <= mServerArrayList.size()) {
            if (indexSelected == (getSelectedActionPosition() - 1))
                getActivity().finish();
            else {
                setUpServerConnection(getSelectedServer());
            }
        }
    }

    private Server getSelectedServer() {
        return mServerArrayList.get(getSelectedActionPosition() - 1);
    }

    private void setUpServerConnection(Server server) {
        if (serverClient.isConnected(server)) {
            setUpServerConnection();
        } else {
            serverClient.connect(getActivity(), server);
        }
    }

    private void setUpServerConnection() {
        if (!isConnectionAvailable() || isConnectionAuto()) {
            serverClient.connectAuto();
            return;
        }

        if (isConnectionLocal()) {
            serverClient.connectLocal();
        } else {
            serverClient.connectRemote();
        }
    }

    private boolean isConnectionAvailable() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        return preferences.contains(getString(R.string.preference_key_server_connection));
    }

    private boolean isConnectionAuto() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String preferenceConnection = preferences.getString(getString(R.string.preference_key_server_connection), null);

        return preferenceConnection.equals(getString(R.string.preference_key_server_connection_auto));
    }

    private boolean isConnectionLocal() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String preferenceConnection = preferences.getString(getString(R.string.preference_key_server_connection), null);

        return preferenceConnection.equals(getString(R.string.preference_key_server_connection_local));
    }

    @Subscribe
    public void onServerConnected(ServerConnectedEvent event) {
        setUpServerConnection();
    }

    @Subscribe
    public void onAuthenticationStart(ServerAuthenticationStartEvent event) {
        Server server = getSelectedServer();
        if (server == null) {
            return;
        }
        if (server.getName().equals(getString(R.string.demo_server_name))) {
            Preferences.setServerName(getActivity(), server.getName());
            Preferences.setServerSession(getActivity(), server.getSession());
            Preferences.setServerToken(getActivity(), null);
            launchTV();
            return;
        }

        authenticateHdaUser();
    }

    private void authenticateHdaUser() {
        startAuthenticationActivity();
    }

    private void startAuthenticationActivity() {
        startActivityForResult(Intents.Builder.with(getActivity()).buildPINAuthenticationIntent(getSelectedServer()), PIN_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PIN_REQUEST_CODE && resultCode == RESULT_OK) {
            launchTV();
        }
    }

    private void launchTV() {
        Intent tvIntent = Intents.Builder.with(getActivity()).buildTVActivity(mServerArrayList,
            getString(R.string.intent_servers));
        startActivity(tvIntent);
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
}
