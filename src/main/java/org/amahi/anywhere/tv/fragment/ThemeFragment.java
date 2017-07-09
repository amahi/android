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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v17.leanback.app.GuidedStepFragment;
import android.support.v17.leanback.widget.GuidanceStylist;
import android.support.v17.leanback.widget.GuidedAction;
import android.support.v4.content.ContextCompat;

import org.amahi.anywhere.R;
import org.amahi.anywhere.util.Preferences;

import java.util.ArrayList;
import java.util.List;

public class ThemeFragment extends GuidedStepFragment {

    private static final int OPTION_CHECK_SET_ID = 10;
    private static final int ACTION_BACK = 1;
    private ArrayList<String> OPTION_NAMES = new ArrayList<>();
    private ArrayList<String> OPTION_DESCRIPTIONS = new ArrayList<>();
    private ArrayList<Boolean> OPTION_CHECKED = new ArrayList<>();
    private Context mContext;
    private SharedPreferences mSharedPreferences;

    public ThemeFragment() {
    }

    @SuppressLint("ValidFragment")
    public ThemeFragment(Context context) {
        mContext = context;
    }

    @NonNull

    @Override
    public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {
        return new GuidanceStylist.Guidance(getString(R.string.pref_title_select_theme),
                getString(R.string.pref_theme_desc),
                "",
                ContextCompat.getDrawable(getActivity(), R.drawable.ic_app_logo_shadowless));
    }

    @Override
    public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {

        setTitle(actions);

        setPreference();

        setOPTION_NAMES();

        setOptionDesc();

        String prefCheck = mSharedPreferences.getString(getString(R.string.pref_key_theme), getString(R.string.pref_theme_dark));

        setChecked(prefCheck);

        setCheckedActionButtons(actions);

        setBackButton(actions);
    }

    private void setTitle(List<GuidedAction> actions) {
        String title = getString(R.string.pref_title_theme);

        String desc = getString(R.string.pref_theme_detail_desc);

        actions.add(new GuidedAction.Builder(mContext)
                .title(title)
                .description(desc)
                .multilineDescription(true)
                .infoOnly(true)
                .enabled(false)
                .build());

    }

    private void setPreference() {
        mSharedPreferences = Preferences.getPreference(mContext);
    }

    private void setOPTION_NAMES() {
        OPTION_NAMES.add(getString(R.string.pref_theme_light));
        OPTION_NAMES.add(getString(R.string.pref_theme_dark));
    }

    private void setOptionDesc() {
        for (int i = 0; i < 2; i++) {
            OPTION_DESCRIPTIONS.add("");
            setOptionCheck();
        }
    }

    private void setOptionCheck() {
        OPTION_CHECKED.add(false);
    }

    private void setChecked(String prefCheck) {
        if (prefCheck.matches(getString(R.string.pref_theme_light))) OPTION_CHECKED.set(0, true);

        if (prefCheck.matches(getString(R.string.pref_theme_dark))) OPTION_CHECKED.set(1, true);
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

        if (getSelectedActionPosition() <= 2) {
            if (OPTION_NAMES.get(getSelectedActionPosition() - 1).matches("Amahi light")) {
                Preferences.setLight(mContext, mSharedPreferences);
            }

            if (OPTION_NAMES.get(getSelectedActionPosition() - 1).matches("Amahi dark")) {
                Preferences.setDark(mContext, mSharedPreferences);
            }
        }

        if (getSelectedActionPosition() == 3) {
            getActivity().finish();
        }
    }
}
