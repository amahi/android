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

package org.amahi.anywhere.tv.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v17.leanback.app.GuidedStepFragment;

import org.amahi.anywhere.R;
import org.amahi.anywhere.tv.fragment.ConnectionFragment;
import org.amahi.anywhere.tv.fragment.SignOutFragment;
import org.amahi.anywhere.tv.fragment.ThemeFragment;

public class SettingsActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String settingsType = getIntent().getStringExtra(Intent.EXTRA_TEXT);

        if(savedInstanceState==null){

            if(settingsType.matches(getString(R.string.pref_title_sign_out)))
                GuidedStepFragment.add(getFragmentManager(),new SignOutFragment(this));

            if(settingsType.matches(getString(R.string.pref_title_connection)))
                GuidedStepFragment.add(getFragmentManager(),new ConnectionFragment(this));

            if(settingsType.matches(getString(R.string.pref_title_select_theme)))
                GuidedStepFragment.add(getFragmentManager(),new ThemeFragment(this));
        }
    }
}
