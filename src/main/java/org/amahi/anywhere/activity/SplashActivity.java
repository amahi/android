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
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import org.amahi.anywhere.util.CheckTV;
import org.amahi.anywhere.util.Preferences;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setUpActivity();

        finish();
    }

    private void setUpActivity() {
        if (Preferences.getFirstRun(this) && !CheckTV.isATV(this)) {
            launchIntro();
        } else {
            launchNavigation();
        }
    }

    private void launchNavigation() {
        startActivity(new Intent(this, NavigationActivity.class));
    }

    private void launchIntro() {
        startActivity(new Intent(this, IntroductionActivity.class));
    }
}
