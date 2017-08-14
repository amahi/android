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
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.squareup.otto.Subscribe;

import org.amahi.anywhere.R;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.FileOpeningEvent;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerShare;
import org.amahi.anywhere.tv.fragment.ServerFileTvFragment;
import org.amahi.anywhere.util.Fragments;
import org.amahi.anywhere.util.Intents;

import java.util.List;

public class ServerFileTvActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_file_tv);
        setFirstFragment();
    }

    private void setFirstFragment() {
        getFragmentManager().beginTransaction().add(R.id.server_file_tv_container, Fragments.Builder.buildFirstTvFragment(getServerFile(), getServerShare())).commit();
    }

    private ServerFile getServerFile() {
        return getIntent().getParcelableExtra(Intents.Extras.SERVER_FILE);
    }

    private ServerShare getServerShare() {
        return getIntent().getParcelableExtra(Intents.Extras.SERVER_SHARE);
    }

    @Subscribe
    public void onFileOpening(FileOpeningEvent event) {
        setUpFile(event.getShare(), event.getFiles(), event.getFile());
    }

    private void setUpFile(ServerShare share, List<ServerFile> files, ServerFile file) {
        setUpFileActivity(share, files, file);
    }

    private void setUpFileActivity(ServerShare share, List<ServerFile> files, ServerFile file) {
        if (Intents.Builder.with(this).isServerFileSupported(file)) {
            startFileActivity(share, files, file);
        }
    }

    private void startFileActivity(ServerShare share, List<ServerFile> files, ServerFile file) {
        Intent intent = Intents.Builder.with(this).buildServerFileIntent(share, files, file);
        startActivity(intent);
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
