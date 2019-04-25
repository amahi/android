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

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.squareup.otto.Subscribe;

import org.amahi.anywhere.R;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.UploadSettingsOpeningEvent;
import org.amahi.anywhere.fragment.SettingsFragment;
import org.amahi.anywhere.fragment.UploadSettingsFragment;

/**
 * Settings activity. Shows application's settings.
 * Settings itself are provided via {@link org.amahi.anywhere.fragment.SettingsFragment}.
 */
public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setUpHomeNavigation();

        setContentView(R.layout.activity_settings);

        if (savedInstanceState == null) {
            setUpSettingsFragment();
        }
    }

    private void setUpHomeNavigation() {
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_launcher);
    }

    private void setUpSettingsFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .replace(R.id.settings_container, new SettingsFragment()).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    @Subscribe
    public void onUploadSettingsOpenEvent(UploadSettingsOpeningEvent event) {
        getSupportFragmentManager().beginTransaction()
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .replace(R.id.settings_container, new UploadSettingsFragment())
            .addToBackStack(null)
            .commit();
    }

    @Override
    protected void onResume() {
        super.onResume();

        BusProvider.getBus().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        BusProvider.getBus().unregister(this);
    }
}
