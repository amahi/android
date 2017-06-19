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

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.VideoView;
import android.widget.Toast;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.R;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerShare;
import org.amahi.anywhere.util.FullScreenHelper;
import org.amahi.anywhere.util.Intents;
import org.amahi.anywhere.view.MediaControls;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

/**
 * Native Video Player activity. Shows video, supports basic operations such as pausing, resuming.
 * The playback itself is done via {@link org.amahi.anywhere.service}.
 * Backed up by {@link android.media.MediaPlayer}.
 */
public class NativeVideoActivity extends AppCompatActivity implements
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener {

    private static final Set<String> SUPPORTED_FORMATS;
    private static final String VIDEO_POSITION = "video_position";

    static {
        SUPPORTED_FORMATS = new HashSet<>(Arrays.asList(
                "video/3gp",
                "video/mp4",
                "video/ts",
                "video/webm",
                "video/x-matroska"
        ));
    }

    public static boolean supports(String mime_type) {
        return SUPPORTED_FORMATS.contains(mime_type);
    }

    @Inject
    ServerClient serverClient;

    private MediaControls videoControls;
    private VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_native_video);

        // NOTE-cpg: used for debugging - to visually display when the native player is being used
        // Toast.makeText(this, "NATIVE PLAYER", Toast.LENGTH_SHORT).show();

        setUpInjections();

        setUpHomeNavigation();

        setUpVideo();

        setUpFullScreen();
    }

    private void setUpInjections() {
        AmahiApplication.from(this).inject(this);
    }

    private void setUpHomeNavigation() {
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_launcher);
    }

    private void setUpVideo() {
        setUpVideoTitle();
        setUpVideoView();
    }

    private ServerShare getVideoShare() {
        return getIntent().getParcelableExtra(Intents.Extras.SERVER_SHARE);
    }

    private ServerFile getVideoFile() {
        return getIntent().getParcelableExtra(Intents.Extras.SERVER_FILE);
    }

    private Uri getVideoUri() {
        return serverClient.getFileUri(getVideoShare(), getVideoFile());
    }

    private FrameLayout getVideoMainFrame() {
        return (FrameLayout) findViewById(R.id.video_main_frame);
    }

    private View getControlsContainer() {
        return findViewById(R.id.container_controls);
    }

    private FrameLayout getVideoSurfaceFrame() {
        return (FrameLayout) findViewById(R.id.video_surface_frame);
    }

    private ProgressBar getProgressBar() {
        return (ProgressBar) findViewById(android.R.id.progress);
    }

    private void setUpVideoView() {
        setUpVideoControls();
        videoView = (VideoView) findViewById(R.id.video_view);
        videoView.setOnPreparedListener(this);
        videoView.setVideoURI(getVideoUri());
        videoView.setMediaController(videoControls);
        videoView.start();
    }

    private boolean areVideoControlsAvailable() {
        return videoControls != null;
    }

    private void setUpVideoControls() {
        if (!areVideoControlsAvailable()) {
            videoControls = new MediaControls(this);
            videoControls.setMediaPlayer(videoView);
            videoControls.setAnchorView(getControlsContainer());
        }
    }

    private void setUpVideoTitle() {
        getSupportActionBar().setTitle(getVideoFile().getName());
    }

    private void setUpFullScreen() {
        final FullScreenHelper fullScreen = new FullScreenHelper(getSupportActionBar(), getVideoMainFrame());
        fullScreen.init();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    public void onPause() {
        super.onPause();

        videoControls.hide();

        if (!isChangingConfigurations()) {
            videoView.pause();
        }

        if (isFinishing()) {
            videoView.stopPlayback();
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        getProgressBar().setVisibility(View.GONE);
        getVideoMainFrame().setVisibility(View.VISIBLE);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(VIDEO_POSITION, videoView.getCurrentPosition());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        int videoPosition = savedInstanceState.getInt(VIDEO_POSITION, 0);
        if (videoPosition > 0) {
            videoView.seekTo(videoPosition);
        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        finish();
    }
}
