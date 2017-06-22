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
import android.support.v17.leanback.app.GuidedStepFragment;
import android.support.v17.leanback.widget.GuidanceStylist;
import android.support.v17.leanback.widget.GuidedAction;
import android.support.v4.content.ContextCompat;

import org.amahi.anywhere.R;
import org.amahi.anywhere.activity.NavigationActivity;
import org.amahi.anywhere.util.Preferences;

import java.util.ArrayList;
import java.util.List;

public class ConnectionFragment extends GuidedStepFragment {

    private static final int OPTION_CHECK_SET_ID = 10;
    private static final int ACTION_BACK = 1;
    private ArrayList<String> OPTION_NAMES = new ArrayList<>();
    private ArrayList<String> OPTION_DESCRIPTIONS = new ArrayList<>();
    private ArrayList<Boolean> OPTION_CHECKED = new ArrayList<>();
    private Context mContext;
    private SharedPreferences preference;
    private String initialSelected;

    public ConnectionFragment() {
    }

    @SuppressLint("ValidFragment")
    public ConnectionFragment(Context context) {
        mContext = context;
        preference = getTVPreference();
    }

    private SharedPreferences getTVPreference() {
        return Preferences.getTVPreference(mContext);
    }

    @NonNull
    @Override
    public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {
        return new GuidanceStylist.Guidance(getString(R.string.pref_title_connection),
                getString(R.string.pref_connection_desc),
                "",
                ContextCompat.getDrawable(getActivity(), R.drawable.ic_app_logo));
    }


    @Override
    public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
        setTitle(actions);

        populateNames();

        populateDesc();

        String selected = getServerConnectionStatus();

        initialSelected = selected;

        markSelected(selected);

        setCheckedActionButtons(actions);

        setBackButton(actions);
    }

    private void setTitle(List<GuidedAction> actions) {
        String title = getString(R.string.pref_title_connection);

        String desc = getString(R.string.pref_connection_desc);

        actions.add(new GuidedAction.Builder(mContext)
                .title(title)
                .description(desc)
                .multilineDescription(true)
                .infoOnly(true)
                .enabled(false)
                .build());
    }

    private void populateNames() {
        OPTION_NAMES.add(getString(R.string.preference_entry_server_connection_auto));
        OPTION_NAMES.add(getString(R.string.preference_entry_server_connection_remote));
        OPTION_NAMES.add(getString(R.string.preference_entry_server_connection_local));
    }

    private void populateDesc() {
        for (int i = 0; i < 3; i++) {
            OPTION_DESCRIPTIONS.add("");
            populateChecked();
        }
    }

    private void populateChecked() {
        OPTION_CHECKED.add(false);
    }

    private String getServerConnectionStatus() {
        return Preferences.getServerConnection(preference, mContext);
    }

    private void markSelected(String selected) {
        if (selected.matches(getString(R.string.preference_entry_server_connection_auto)))
            OPTION_CHECKED.set(0, true);
        if (selected.matches(getString(R.string.preference_entry_server_connection_remote)))
            OPTION_CHECKED.set(1, true);
        if (selected.matches(getString(R.string.preference_entry_server_connection_local)))
            OPTION_CHECKED.set(2, true);
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

        if (getSelectedActionPosition() <= 3) {
            if (OPTION_NAMES.get(getSelectedActionPosition() - 1).matches(getString(R.string.preference_entry_server_connection_auto))) {
                Preferences.setPrefAuto(preference, mContext);
            }

            if (OPTION_NAMES.get(getSelectedActionPosition() - 1).matches(getString(R.string.preference_entry_server_connection_remote))) {
                Preferences.setPrefRemote(preference, mContext);
            }

            if (OPTION_NAMES.get(getSelectedActionPosition() - 1).matches(getString(R.string.preference_entry_server_connection_local))) {
                Preferences.setPrefLocal(preference, mContext);
            }
        } else {
            if (initialSelected.matches(Preferences.getServerConnection(preference, mContext)))
                getActivity().finish();
            else
                startActivity(new Intent(getActivity(), NavigationActivity.class));
        }
    }
}
