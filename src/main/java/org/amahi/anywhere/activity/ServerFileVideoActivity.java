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

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.squareup.otto.Subscribe;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.R;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.bus.DialogButtonClickedEvent;
import org.amahi.anywhere.db.entities.PlayedFile;
import org.amahi.anywhere.db.entities.RecentFile;
import org.amahi.anywhere.db.repositories.PlayedFileRepository;
import org.amahi.anywhere.db.repositories.RecentFileRepository;
import org.amahi.anywhere.fragment.ResumeDialogFragment;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerShare;
import org.amahi.anywhere.service.VideoService;
import org.amahi.anywhere.util.FileManager;
import org.amahi.anywhere.util.FullScreenHelper;
import org.amahi.anywhere.util.Intents;
import org.amahi.anywhere.util.VideoSwipeGestures;
import org.amahi.anywhere.view.MediaControls;
import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Video activity. Shows videos, supports basic operations such as pausing, resuming, scrolling.
 * The playback itself is done via {@link org.amahi.anywhere.service.VideoService}.
 * Backed up by {@link android.view.SurfaceView} and {@link org.videolan.libvlc.LibVLC}.
 */
public class ServerFileVideoActivity extends AppCompatActivity implements
    ServiceConnection,
    MediaController.MediaPlayerControl,
    IVLCVout.OnNewVideoLayoutListener,
    View.OnLayoutChangeListener,
    VideoSwipeGestures.SeekControl,
    SessionManagerListener<CastSession> {

    private boolean isSubtitlesEnable = false;
    private static SurfaceSizes CURRENT_SIZE = SurfaceSizes.SURFACE_BEST_FIT;
    @Inject
    ServerClient serverClient;
    private VideoService videoService;
    private MediaControls videoControls;
    private FullScreenHelper fullScreen;
    private Handler layoutChangeHandler;
    private CastContext mCastContext;
    private CastSession mCastSession;
    private int mVideoHeight = 0;
    private int mVideoWidth = 0;
    private int mVideoVisibleHeight = 0;
    private int mVideoVisibleWidth = 0;
    private int mVideoSarNum = 0;
    private int mVideoSarDen = 0;
    private SurfaceView mSubtitlesSurface = null;
    private final Runnable mRunnable = this::updateVideoSurfaces;
    private float bufferPercent = 0.0f;

    //TODO Add feature for changing the screen size

    public static boolean supports(String mime_type) {
        String type = mime_type.split("/")[0];

        return "video".equals(type);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_file_video);

        setUpInjections();

        setUpHomeNavigation();

        setUpCast();

        prepareFiles();

        setUpVideoTitle();

        setUpVideo();

        loadState(savedInstanceState);
    }

    private void loadState(Bundle state) {
        if (state != null)
            isSubtitlesEnable = state.getBoolean(State.SUBTITLES_ENABLED, false);
    }

    private void setUpInjections() {
        AmahiApplication.from(this).inject(this);
    }

    private void setUpHomeNavigation() {
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_launcher);
    }

    private void setUpCast() {
        mCastContext = CastContext.getSharedInstance(this);
        mCastSession = mCastContext.getSessionManager().getCurrentCastSession();
    }

    private void prepareFiles() {
        int fileType = getIntent().getIntExtra(Intents.Extras.FILE_TYPE, FileManager.SERVER_FILE);
        if (fileType == FileManager.RECENT_FILE) {
            RecentFileRepository repository = new RecentFileRepository(this);
            RecentFile recentFile = repository.getRecentFile(getIntent().getStringExtra(Intents.Extras.UNIQUE_KEY));
            ServerFile serverFile = new ServerFile(recentFile.getName(), recentFile.getModificationTime(), recentFile.getMime());
            serverFile.setSize(recentFile.getSize());
            serverFile.setMime(recentFile.getMime());
            getIntent().putExtra(Intents.Extras.SERVER_FILE, serverFile);
            List<ServerFile> serverFiles = new ArrayList<>();
            serverFiles.add(serverFile);
            getIntent().putExtra(Intents.Extras.SERVER_FILES, new ArrayList<Parcelable>(serverFiles));
        }
    }

    private void setUpVideo() {
        if (mCastSession != null && mCastSession.isConnected()) {
            loadRemoteMedia(0, true);
        } else {
            setUpViews();

            setUpFullScreen();

            setUpGestureListener();

            setUpVideoService();
        }
    }

    private void setUpViews() {
        final ViewStub stub = findViewById(R.id.subtitles_stub);
        mSubtitlesSurface = (SurfaceView) stub.inflate();
        mSubtitlesSurface.setZOrderMediaOverlay(true);
        mSubtitlesSurface.getHolder().setFormat(PixelFormat.TRANSLUCENT);
    }

    private void setUpVideoTitle() {
        getSupportActionBar().setTitle(getVideoFile().getName());
    }

    private ServerFile getVideoFile() {
        return getIntent().getParcelableExtra(Intents.Extras.SERVER_FILE);
    }

    private void setUpFullScreen() {
        fullScreen = new FullScreenHelper(getSupportActionBar(), getVideoMainFrame());
        fullScreen.enableOnClickToggle(false);
        getVideoMainFrame().setOnClickListener(view -> {
            fullScreen.toggle();
            videoControls.toggle();
        });
        fullScreen.init();
    }

    private FrameLayout getSwipeContainer() {
        return findViewById(R.id.swipe_controls_frame);
    }

    private void setUpGestureListener() {
        getVideoMainFrame().setOnTouchListener(new VideoSwipeGestures(this, this, getSwipeContainer()));
    }

    private MediaPlayer getMediaPlayer() {
        assert videoService != null;
        return videoService.getMediaPlayer();
    }

    @Override
    protected void onStart() {
        super.onStart();
        setUpVideoServiceBind();
    }

    private void setUpVideoService() {
        Intent intent = new Intent(this, VideoService.class);
        startService(intent);
    }

    private void setUpVideoServiceBind() {
        Intent intent = new Intent(this, VideoService.class);
        bindService(intent, this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onServiceDisconnected(ComponentName serviceName) {
    }

    @Override
    public void onServiceConnected(ComponentName serviceName, IBinder serviceBinder) {
        setUpVideoServiceBind(serviceBinder);

        setUpVideoView();
        setUpVideoControls();
        setUpHandlers();
        setUpVideoPlayback();
    }

    private void setUpVideoServiceBind(IBinder serviceBinder) {
        VideoService.VideoServiceBinder videoServiceBinder = (VideoService.VideoServiceBinder) serviceBinder;
        videoService = videoServiceBinder.getVideoService();
    }

    private void setUpVideoView() {
        SurfaceHolder surfaceHolder = getSurface().getHolder();

        surfaceHolder.setFormat(PixelFormat.RGBX_8888);
        surfaceHolder.setKeepScreenOn(true);
        final IVLCVout vlcVout = getMediaPlayer().getVLCVout();
        vlcVout.setVideoView(getSurface());
        if (mSubtitlesSurface != null)
            vlcVout.setSubtitlesView(mSubtitlesSurface);
        vlcVout.attachViews(this);
        getMediaPlayer().setEventListener(this::onEvent);
    }

    private SurfaceView getSurface() {
        return findViewById(R.id.surface);
    }

    private FrameLayout getSurfaceFrame() {
        return findViewById(R.id.video_surface_frame);
    }

    private FrameLayout getVideoMainFrame() {
        return findViewById(R.id.video_main_frame);
    }

    private void setUpVideoControls() {
        if (!areVideoControlsAvailable()) {
            videoControls = new MediaControls(this);
            videoControls.setMediaPlayer(this);
            videoControls.setAnchorView(getControlsContainer());
        }

    }

    private boolean areVideoControlsAvailable() {
        return videoControls != null;
    }

    private void setUpHandlers() {
        if (!layoutChangeHandlerAvailable()) {
            layoutChangeHandler = new Handler();
        }
    }

    private boolean layoutChangeHandlerAvailable() {
        return layoutChangeHandler != null;
    }

    private View getControlsContainer() {
        return findViewById(R.id.container_controls);
    }

    private void setUpVideoPlayback() {
        if (videoService.isVideoStarted()) {
            showThenAutoHideControls();
            getVideoMainFrame().setVisibility(View.VISIBLE);
            getProgressBar().setVisibility(View.INVISIBLE);
        } else {
            videoService.startVideo(getVideoShare(), getVideoFile(), false);
            addLayoutChangeListener();
            setUpPlayPosition();
        }
    }

    private void showThenAutoHideControls() {
        if (!isFinishing()) {
            fullScreen.show();
            fullScreen.delayedHide();
            videoControls.show();
        }
    }

    private void setUpPlayPosition() {
        long lastPlayedPosition = getLastPlayedPosition(getVideoFile());

        if (lastPlayedPosition != 0) {
            new ResumeDialogFragment().show(getSupportFragmentManager(), "resume_dialog");
        } else {
            getMediaPlayer().setTime(0);
            getMediaPlayer().play();
        }
    }

    private long getLastPlayedPosition(ServerFile serverFile) {
        PlayedFileRepository repository = new PlayedFileRepository(this);
        PlayedFile playedFile = repository.getPlayedFile(serverFile.getUniqueKey());
        if (playedFile != null) {
            return playedFile.getPosition();
        }
        return 0;
    }

    @Subscribe
    public void onDialogButtonClicked(DialogButtonClickedEvent event) {
        getMediaPlayer().play();
        if (event.getButtonId() == DialogButtonClickedEvent.YES) {
            getMediaPlayer().setTime(getLastPlayedPosition(getVideoFile()));
        } else {
            deletePlayedFileFromDatabase(getVideoFile());
            getMediaPlayer().setTime(0);
        }
    }

    private void deletePlayedFileFromDatabase(ServerFile serverFile) {
        PlayedFileRepository repository = new PlayedFileRepository(this);
        repository.delete(serverFile.getUniqueKey());
    }

    private ProgressBar getProgressBar() {
        return findViewById(android.R.id.progress);
    }

    private ServerShare getVideoShare() {
        return getIntent().getParcelableExtra(Intents.Extras.SERVER_SHARE);
    }

    private void addLayoutChangeListener() {
        getSurfaceFrame().addOnLayoutChangeListener(this);
    }

    @Override
    public void onLayoutChange(View v, int left, int top, int right,
                               int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        if (left != oldLeft || top != oldTop || right != oldRight || bottom != oldBottom) {
            layoutChangeHandler.removeCallbacks(mRunnable);
            layoutChangeHandler.post(mRunnable);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onNewVideoLayout(IVLCVout vout, int width, int height, int visibleWidth, int visibleHeight, int sarNum, int sarDen) {
        mVideoWidth = width;
        mVideoHeight = height;
        mVideoVisibleWidth = visibleWidth;
        mVideoVisibleHeight = visibleHeight;
        mVideoSarNum = sarNum;
        mVideoSarDen = sarDen;
        updateVideoSurfaces();
    }

    private void updateVideoSurfaces() {
        int screenWidth = getWindow().getDecorView().getWidth();
        int screenHeight = getWindow().getDecorView().getHeight();

        // sanity check
        if (screenWidth * screenHeight == 0) {
            Log.e("Error", "Invalid surface size");
            return;
        }

        getMediaPlayer().getVLCVout().setWindowSize(screenWidth, screenHeight);
        ViewGroup.LayoutParams lp = getSurface().getLayoutParams();
        if (mVideoWidth * mVideoHeight == 0) {
            /* Case of OpenGL vouts: handles the placement of the video using MediaPlayer API */
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
            getSurface().setLayoutParams(lp);
            lp = getSurfaceFrame().getLayoutParams();
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
            getSurfaceFrame().setLayoutParams(lp);
            changeMediaPlayerLayout(screenWidth, screenHeight);
            return;
        }

        if (lp.width == lp.height && lp.width == ViewGroup.LayoutParams.MATCH_PARENT) {
            /* We handle the placement of the video using Android View LayoutParams */
            getMediaPlayer().setAspectRatio(null);
            getMediaPlayer().setScale(0);
        }

        double dw = screenWidth, dh = screenHeight;
        final boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        if (screenWidth > screenHeight && isPortrait || screenWidth < screenHeight && !isPortrait) {
            dw = screenHeight;
            dh = screenWidth;
        }

        // compute the aspect ratio
        double ar, vw;
        if (mVideoSarDen == mVideoSarNum) {
            /* No indication about the density, assuming 1:1 */
            vw = mVideoVisibleWidth;
            ar = (double) mVideoVisibleWidth / (double) mVideoVisibleHeight;
        } else {
            /* Use the specified aspect ratio */
            vw = mVideoVisibleWidth * (double) mVideoSarNum / mVideoSarDen;
            ar = vw / mVideoVisibleHeight;
        }

        // compute the display aspect ratio
        double dar = dw / dh;
        switch (CURRENT_SIZE) {
            case SURFACE_BEST_FIT:
                if (dar < ar)
                    dh = dw / ar;
                else
                    dw = dh * ar;
                break;
            case SURFACE_FIT_SCREEN:
                if (dar >= ar)
                    dh = dw / ar; /* horizontal */
                else
                    dw = dh * ar; /* vertical */
                break;
            case SURFACE_FILL:
                break;
            case SURFACE_16_9:
                ar = 16.0 / 9.0;
                if (dar < ar)
                    dh = dw / ar;
                else
                    dw = dh * ar;
                break;
            case SURFACE_4_3:
                ar = 4.0 / 3.0;
                if (dar < ar)
                    dh = dw / ar;
                else
                    dw = dh * ar;
                break;
            case SURFACE_ORIGINAL:
                dh = mVideoVisibleHeight;
                dw = vw;
                break;
        }
        // set display size
        lp.width = (int) Math.ceil(dw * mVideoWidth / mVideoVisibleWidth);
        lp.height = (int) Math.ceil(dh * mVideoHeight / mVideoVisibleHeight);
        getSurface().setLayoutParams(lp);
        if (mSubtitlesSurface != null)
            mSubtitlesSurface.setLayoutParams(lp);

        // set frame size (crop if necessary)
        lp = getSurfaceFrame().getLayoutParams();
        lp.width = (int) Math.floor(dw);
        lp.height = (int) Math.floor(dh);
        getSurfaceFrame().setLayoutParams(lp);
        lp = getSwipeContainer().getLayoutParams();
        lp.width = (int) Math.floor(dw);
        lp.height = (int) Math.floor(dh);
        getSwipeContainer().setLayoutParams(lp);
        getSurface().invalidate();
        if (mSubtitlesSurface != null)
            mSubtitlesSurface.invalidate();
    }

    private void changeMediaPlayerLayout(int displayW, int displayH) {
        /* Change the video placement using the MediaPlayer API */
        switch (CURRENT_SIZE) {
            case SURFACE_BEST_FIT:
                getMediaPlayer().setAspectRatio(null);
                getMediaPlayer().setScale(0);
                break;
            case SURFACE_FIT_SCREEN:
            case SURFACE_FILL: {
                Media.VideoTrack vtrack = getMediaPlayer().getCurrentVideoTrack();
                if (vtrack == null)
                    return;
                final boolean videoSwapped = vtrack.orientation == Media.VideoTrack.Orientation.LeftBottom
                    || vtrack.orientation == Media.VideoTrack.Orientation.RightTop;
                if (CURRENT_SIZE == SurfaceSizes.SURFACE_FIT_SCREEN) {
                    int videoW = vtrack.width;
                    int videoH = vtrack.height;
                    if (videoSwapped) {
                        int swap = videoW;
                        videoW = videoH;
                        videoH = swap;
                    }
                    if (vtrack.sarNum != vtrack.sarDen)
                        videoW = videoW * vtrack.sarNum / vtrack.sarDen;
                    float videoAspectRatio = videoW / (float) videoH;
                    float displayAspectRatio = displayW / (float) displayH;
                    float scale;
                    if (displayAspectRatio >= videoAspectRatio)
                        scale = displayW / (float) videoW; /* horizontal */
                    else
                        scale = displayH / (float) videoH; /* vertical */
                    getMediaPlayer().setScale(scale);
                    getMediaPlayer().setAspectRatio(null);
                } else {
                    getMediaPlayer().setScale(0);
                    getMediaPlayer().setAspectRatio(!videoSwapped ? "" + displayW + ":" + displayH
                        : "" + displayH + ":" + displayW);
                }
                break;
            }
            case SURFACE_16_9:
                getMediaPlayer().setAspectRatio("16:9");
                getMediaPlayer().setScale(0);
                break;
            case SURFACE_4_3:
                getMediaPlayer().setAspectRatio("4:3");
                getMediaPlayer().setScale(0);
                break;
            case SURFACE_ORIGINAL:
                getMediaPlayer().setAspectRatio(null);
                getMediaPlayer().setScale(1);
                break;
        }
    }

    @Override
    public void start() {
        videoService.playVideo();
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public void pause() {
        videoService.pauseVideo();
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public void seekTo(int time) {
        getMediaPlayer().setTime(time);
    }

    @Override
    public int getDuration() {
        return (int) getMediaPlayer().getLength();
    }

    @Override
    public int getCurrentPosition() {
        return (int) getMediaPlayer().getTime();
    }

    @Override
    public boolean isPlaying() {
        return getMediaPlayer().isPlaying();
    }

    @Override
    public int getBufferPercentage() {
        return (int) bufferPercent;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    public void onEvent(MediaPlayer.Event event) {

        switch (event.type) {
            case MediaPlayer.Event.MediaChanged:
                getVideoMainFrame().setVisibility(View.VISIBLE);
                break;
            case MediaPlayer.Event.Playing:
                getProgressBar().setVisibility(View.INVISIBLE);
                showThenAutoHideControls();
                break;
            case MediaPlayer.Event.Paused:
                showThenAutoHideControls();
                break;
            case MediaPlayer.Event.EndReached:
                deletePlayedFileFromDatabase(getVideoFile());
                finish();
                break;
            case MediaPlayer.Event.Buffering:
                bufferPercent = event.getBuffering();
                break;
            case MediaPlayer.Event.EncounteredError:
                Toast.makeText(this, R.string.message_error_video, Toast.LENGTH_SHORT).show();
                break;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar_video_files, menu);
        CastButtonFactory.setUpMediaRouteButton(getApplicationContext(), menu,
            R.id.media_route_menu_item);
        menu.findItem(R.id.menu_subtitle).setChecked(isSubtitlesEnable);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.menu_subtitle:
                menuItem.setChecked(!menuItem.isChecked());
                enableSubtitles(menuItem.isChecked());
                return true;

            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    private void enableSubtitles(boolean enable) {
        videoService.enableSubtitles(enable);
        isSubtitlesEnable = enable;
    }

    @Override
    protected void onResume() {
        mCastContext.getSessionManager().addSessionManagerListener(this, CastSession.class);
        super.onResume();

        BusProvider.getBus().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        mCastContext.getSessionManager().removeSessionManagerListener(this, CastSession.class);

        if (videoControls != null && videoControls.isShowing()) {
            videoControls.hide();
        }

        if (!isChangingConfigurations()) {
            pause();
        }

        if (isFinishing()) {
            tearDownVideoPlayback();
        }

        BusProvider.getBus().unregister(this);
    }

    private void tearDownVideoPlayback() {
        getMediaPlayer().stop();
    }

    @Override
    protected void onStop() {
        super.onStop();
        getSurfaceFrame().removeOnLayoutChangeListener(this);
        getMediaPlayer().getVLCVout().detachViews();
        tearDownVideoServiceBind();
    }

    private void tearDownVideoServiceBind() {
        unbindService(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing()) {
            tearDownVideoService();
        }
    }

    private void tearDownVideoService() {
        Intent intent = new Intent(this, VideoService.class);
        stopService(intent);
    }

    @Override
    public void onSessionEnded(CastSession session, int error) {
    }

    @Override
    public void onSessionResumed(CastSession session, boolean wasSuspended) {
        onApplicationConnected(session);
    }

    @Override
    public void onSessionResumeFailed(CastSession session, int error) {
    }

    @Override
    public void onSessionStarted(CastSession session, String sessionId) {
        onApplicationConnected(session);
    }

    @Override
    public void onSessionStartFailed(CastSession session, int error) {
    }

    @Override
    public void onSessionStarting(CastSession session) {
    }

    @Override
    public void onSessionEnding(CastSession session) {
    }

    @Override
    public void onSessionResuming(CastSession session, String sessionId) {
    }

    @Override
    public void onSessionSuspended(CastSession session, int reason) {
    }

    private void onApplicationConnected(CastSession castSession) {
        mCastSession = castSession;
        boolean isVideoPlaying = videoService.getMediaPlayer().isPlaying();
        if (isVideoPlaying)
            pause();
        loadRemoteMedia(getCurrentPosition(), isVideoPlaying);
    }

    private void loadRemoteMedia(int position, boolean autoPlay) {
        if (mCastSession == null) {
            return;
        }
        final RemoteMediaClient remoteMediaClient = mCastSession.getRemoteMediaClient();
        if (remoteMediaClient == null) {
            return;
        }
        remoteMediaClient.addListener(new RemoteMediaClient.Listener() {
            @Override
            public void onStatusUpdated() {
                Intent intent = new Intent(ServerFileVideoActivity.this, ExpandedControlsActivity.class);
                startActivity(intent);
                remoteMediaClient.removeListener(this);
                finish();
            }

            @Override
            public void onMetadataUpdated() {
            }

            @Override
            public void onQueueStatusUpdated() {
            }

            @Override
            public void onPreloadStatusUpdated() {
            }

            @Override
            public void onSendingRemoteMediaRequest() {
            }

            @Override
            public void onAdBreakStatusUpdated() {
            }
        });
        remoteMediaClient.load(buildMediaInfo(), autoPlay, position);
    }

    private Uri getVideoUri() {
        int fileType = getIntent().getIntExtra(Intents.Extras.FILE_TYPE, FileManager.SERVER_FILE);

        if (fileType == FileManager.RECENT_FILE) {
            return getRecentFileUri();
        }

        return serverClient.getFileUri(getVideoShare(), getVideoFile());
    }

    private Uri getRecentFileUri() {
        RecentFileRepository repository = new RecentFileRepository(this);
        return Uri.parse(repository.getRecentFile(getVideoFile().getUniqueKey()).getUri());
    }

    private MediaInfo buildMediaInfo() {
        MediaMetadata movieMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
        movieMetadata.putString(MediaMetadata.KEY_TITLE, getVideoFile().getNameOnly());
        MediaInfo.Builder builder = new MediaInfo.Builder(getVideoUri().toString())
            .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
            .setContentType(getVideoFile().getMime())
            .setMetadata(movieMetadata);
        if (videoService != null && videoService.getMediaPlayer() != null) {
            builder.setStreamDuration(videoService.getMediaPlayer().getLength());
        }
        return builder.build();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(State.SUBTITLES_ENABLED, isSubtitlesEnable);
    }

    private enum SurfaceSizes {
        SURFACE_BEST_FIT,
        SURFACE_FIT_SCREEN,
        SURFACE_FILL,
        SURFACE_16_9,
        SURFACE_4_3,
        SURFACE_ORIGINAL;
    }

    private static final class State {
        public static final String SUBTITLES_ENABLED = "subtitles_enabled";
    }
}
