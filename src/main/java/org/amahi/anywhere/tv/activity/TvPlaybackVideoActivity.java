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
import android.app.AlertDialog;
import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v17.leanback.widget.PlaybackControlsRow;
import android.view.KeyEvent;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.R;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerShare;
import org.amahi.anywhere.tv.fragment.TvPlaybackVideoFragment;
import org.amahi.anywhere.util.Intents;

import java.util.ArrayList;

import javax.inject.Inject;

import static org.amahi.anywhere.util.Fragments.Builder.buildVideoFragment;

public class TvPlaybackVideoActivity extends Activity {

    @Inject
    ServerClient serverClient;

    private Fragment fragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tv_video_playback);
        setUpInjections();
        fragment = buildVideoFragment(getFile(), getShare(), getFiles());
        replaceFragment();
    }

    private void setUpInjections() {
        AmahiApplication.from(this).inject(this);
    }

    private void replaceFragment() {
        getFragmentManager().beginTransaction().replace(R.id.playback_controls_fragment_container, fragment).commit();
    }

    private ServerShare getShare() {
        return getIntent().getParcelableExtra(Intents.Extras.SERVER_SHARE);
    }

    private ServerFile getFile() {
        return getIntent().getParcelableExtra(Intents.Extras.SERVER_FILE);
    }

    private ArrayList<ServerFile> getFiles() {
        return getIntent().getParcelableArrayListExtra(Intents.Extras.SERVER_FILES);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
                Object savedState = ((TvPlaybackVideoFragment) fragment).getmSavedState();
                if (savedState instanceof PlaybackControlsRow.RewindAction)
                    ((TvPlaybackVideoFragment) fragment).rewind();
                break;

            case KeyEvent.KEYCODE_DPAD_RIGHT:
                savedState = ((TvPlaybackVideoFragment) fragment).getmSavedState();
                if (savedState instanceof PlaybackControlsRow.FastForwardAction)
                    ((TvPlaybackVideoFragment) fragment).fastForward();
                break;

            case KeyEvent.KEYCODE_MEDIA_REWIND:
                ((TvPlaybackVideoFragment) fragment).rewind();
                break;

            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                playPause();
                break;

            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void playPause() {
        PlaybackControlsRow.PlayPauseAction pauseAction = ((TvPlaybackVideoFragment) fragment).getmPlayPauseAction();
        ((TvPlaybackVideoFragment) fragment).togglePlayPause(pauseAction.getIndex() == PlaybackControlsRow.PlayPauseAction.PAUSE);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);

        if (((TvPlaybackVideoFragment) fragment).getmPlayPauseAction().getIndex() == PlaybackControlsRow.PlayPauseAction.PAUSE)
            playPause();

        builder.setTitle(getString(R.string.exit_title))
            .setMessage(getString(R.string.exit_message))
            .setPositiveButton(getString(R.string.yes), (dialog, which) -> TvPlaybackVideoActivity.super.onBackPressed())
            .setNegativeButton(getString(R.string.no), (dialog, which) -> dialog.dismiss()).show();
    }
}
