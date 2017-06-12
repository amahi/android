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
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v17.leanback.app.GuidedStepFragment;
import android.support.v17.leanback.widget.GuidanceStylist;
import android.support.v17.leanback.widget.GuidedAction;
import android.widget.Toast;

import org.amahi.anywhere.R;
import org.amahi.anywhere.tv.activity.SettingsActivity;

import java.util.List;

public class ThemeFragment extends GuidedStepFragment {

    private static final int OPTION_CHECK_SET_ID = 10;

    private static final String[] OPTION_NAMES = {"Amahi Light", "Amahi Dark", "Go back"};

    private static final String[] OPTION_DESCRIPTIONS = {"","",""};

    private static final int[] OPTION_DRAWABLES = {R.drawable.ic_app_logo};

    private static final boolean[] OPTION_CHECKED = {false, true, false};

    private Context mContext;

    public ThemeFragment(){}

    @SuppressLint("ValidFragment")
    public ThemeFragment(Context context){mContext=context;}

    @NonNull
    @Override
    public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {
        String title = getString(R.string.pref_title_theme);

        String breadcrumb = "";

        String description =getString(R.string.pref_theme_desc);

        Drawable icon = null;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            icon = getActivity().getDrawable(R.drawable.ic_app_logo);
        }

        return new GuidanceStylist.Guidance(title, description, breadcrumb, icon);
    }

    @Override
    public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
        String title = getString(R.string.pref_title_theme);

        String desc = getString(R.string.pref_theme_detail_desc);

        actions.add(new GuidedAction.Builder(mContext)
                .title(title)
                .description(desc)
                .multilineDescription(true)
                .infoOnly(true)
                .enabled(false)
                .build());

        for (int i = 0; i < OPTION_NAMES.length; i++) {

            addCheckedAction(actions,

                    OPTION_DRAWABLES[0],

                    getActivity(),

                    OPTION_NAMES[i],

                    OPTION_DESCRIPTIONS[i],

                    OPTION_CHECKED[i]);
        }
    }

    @Override
    public void onGuidedActionClicked(GuidedAction action) {
        //This is temporary.
        String text = OPTION_NAMES[getSelectedActionPosition() - 1] + " clicked";
        Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();

        if(text.matches(getString(R.string.pref_option_go_back)))
            startActivity(new Intent(mContext, SettingsActivity.class));
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
}
