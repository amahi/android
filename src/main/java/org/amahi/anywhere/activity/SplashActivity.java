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

package org.amahi.anywhere.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.annotation.Nullable;

import org.amahi.anywhere.R;

public class SplashActivity extends PreferenceActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        String code = pref.getString("code", null);
        if (code == null)
            launchNavigation();
        else
            launchFingerprintNavigation();
        finish();

    }

    private void launchNavigation() {
        startActivity(new Intent(this, NavigationActivity.class));
    }

    private void launchFingerprintNavigation() {
        startActivity(new Intent(this, PasscodeActivity.class));
    }
}
