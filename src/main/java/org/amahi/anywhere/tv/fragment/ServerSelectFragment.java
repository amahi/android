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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v17.leanback.app.GuidedStepFragment;
import android.support.v17.leanback.widget.GuidanceStylist;
import android.support.v17.leanback.widget.GuidedAction;
import android.support.v4.content.ContextCompat;

import org.amahi.anywhere.R;
import org.amahi.anywhere.server.model.Server;
import org.amahi.anywhere.tv.activity.MainTVActivity;
import org.amahi.anywhere.util.Preferences;

import java.util.ArrayList;
import java.util.List;

public class ServerSelectFragment extends GuidedStepFragment {

    private static final int OPTION_CHECK_SET_ID = 10;
    private static final int ACTION_BACK = 0;
    private Context mContext;
    private ArrayList<Server> mServerArrayList;
    private ArrayList<String> OPTION_NAMES = new ArrayList<>();
    private ArrayList<String> OPTION_DESCRIPTIONS = new ArrayList<>();
    private ArrayList<Boolean> OPTION_CHECKED = new ArrayList<>();
    private SharedPreferences mSharedPref;

    @SuppressLint("ValidFragment")
    public ServerSelectFragment(Context context) {
        mContext = context;
    }

    public ServerSelectFragment() {
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @NonNull
    @Override
    public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {
        return new GuidanceStylist.Guidance(getString(R.string.pref_title_server_select),
                getString(R.string.pref_title_server_select_desc),
                "",
                ContextCompat.getDrawable(getActivity(), R.drawable.ic_app_logo));
    }

    @Override
    public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
        super.onCreateActions(actions, savedInstanceState);
        mSharedPref = Preferences.getPreference(mContext);
        mServerArrayList = getActivity().getIntent().getParcelableArrayListExtra(getString(R.string.intent_servers));

        setTitle(actions);
        populateData();
        String serverName = Preferences.getServerFromPref(mContext, mSharedPref);
        if (serverName == null) {
            setDefaultChecked();
        } else {
            int indexSelected = 0;
            for (int i = 0; i < mServerArrayList.size(); i++) {
                if (serverName.matches(mServerArrayList.get(i).getName())) {
                    indexSelected = i;
                    break;
                }
            }
            setFalseChecked();
            OPTION_CHECKED.set(indexSelected, true);
        }

        setCheckedActionButtons(actions);
        setBackButton(actions);
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

    private void setBackButton(List<GuidedAction> actions) {
        addAction(actions, ACTION_BACK, getString(R.string.pref_option_go_back), "");
    }

    private void addAction(List<GuidedAction> actions, long id, String title, String desc) {
        actions.add(new GuidedAction.Builder(mContext)
                .id(id)
                .title(title)
                .description(desc)
                .build());
    }

    @Override
    public void onGuidedActionClicked(GuidedAction action) {
        if (getSelectedActionPosition() <= mServerArrayList.size()) {
            String server = mServerArrayList.get(getSelectedActionPosition() - 1).getName();
            Preferences.setServertoPref(server, mContext, mSharedPref);
        } else {
            startActivity(new Intent(mContext, MainTVActivity.class));
        }
    }
}
