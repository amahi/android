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

import org.amahi.anywhere.R;
import org.amahi.anywhere.activity.NavigationActivity;
import org.amahi.anywhere.server.model.Server;
import org.amahi.anywhere.tv.fragment.MainTVFragment;
import org.amahi.anywhere.util.Intents;

import java.util.ArrayList;

public class MainTVActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_tv);
        checkAndLaunch();
    }

    private void checkAndLaunch() {
        if (getServers() != null) {
            replaceFragment();
        } else {
            launchNav();
        }
    }

    private ArrayList<Server> getServers() {
        return getIntent().getParcelableArrayListExtra(Intents.Extras.INTENT_SERVERS);
    }

    private void replaceFragment() {
        getFragmentManager().beginTransaction().add(R.id.main_tv_fragment_container, new MainTVFragment()).commit();
    }

    private void launchNav() {
        startActivity(new Intent(this, NavigationActivity.class));
    }
}
