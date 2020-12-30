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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.viewpager.widget.ViewPager;

import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaLoadOptions;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.squareup.otto.Subscribe;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.R;
import org.amahi.anywhere.adapter.ServerFilesAudioPageAdapter;
import org.amahi.anywhere.bus.AudioCompletedEvent;
import org.amahi.anywhere.bus.AudioControlChangeEvent;
import org.amahi.anywhere.bus.AudioControlNextEvent;
import org.amahi.anywhere.bus.AudioControlPreviousEvent;
import org.amahi.anywhere.bus.AudioMetadataRetrievedEvent;
import org.amahi.anywhere.bus.AudioPreparedEvent;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.db.entities.FileInfo;
import org.amahi.anywhere.db.entities.OfflineFile;
import org.amahi.anywhere.db.entities.RecentFile;
import org.amahi.anywhere.db.repositories.FileInfoRepository;
import org.amahi.anywhere.db.repositories.OfflineFileRepository;
import org.amahi.anywhere.db.repositories.RecentFileRepository;
import org.amahi.anywhere.fragment.AudioListFragment;
import org.amahi.anywhere.model.AudioMetadata;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerShare;
import org.amahi.anywhere.service.AudioService;
import org.amahi.anywhere.task.AudioMetadataRetrievingTask;
import org.amahi.anywhere.util.AudioMetadataFormatter;
import org.amahi.anywhere.util.DateTime;
import org.amahi.anywhere.util.FileManager;
import org.amahi.anywhere.util.Fragments;
import org.amahi.anywhere.util.Intents;
import org.amahi.anywhere.util.LocaleHelper;
import org.amahi.anywhere.util.Preferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

/**
 * Audio activity. Shows audio, supports basic operations such as pausing, resuming, audio changing.
 * The playback itself is done via {@link org.amahi.anywhere.service.AudioService}.
 * Backed up by {@link android.media.MediaPlayer}.
 */
public class ServerFileAudioActivity extends AppCompatActivity implements
    ViewPager.OnPageChangeListener,
    ServiceConnection,
    MediaController.MediaPlayerControl,
    View.OnClickListener,
    SessionManagerListener<CastSession> {
    private static final Set<String> SUPPORTED_FORMATS;
    private static final int REPEAT_ALL = 0;
    private static final int REPEAT_ONE = 1;
    private static final String AUDIO_LIST_VISIBLE = "audio_list_visible";

    static {
        SUPPORTED_FORMATS = new HashSet<>(Arrays.asList(
            "audio/flac",
            "audio/mp4",
            "audio/mpeg",
            "audio/ogg"
        ));
    }

    @Inject
    ServerClient serverClient;
    private int REPEAT_MODE = 0;
    private CastContext mCastContext;
    private CastSession mCastSession;
    private AudioMetadataFormatter metadataFormatter;
    private PlaybackLocation mLocation = PlaybackLocation.LOCAL;
    private AudioService audioService;
    private boolean changeAudio = true;
    private boolean audioControlsAvailable = false;
    private boolean audioListVisible = false;

    public static boolean supports(String mime_type) {
        return SUPPORTED_FORMATS.contains(mime_type);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_file_audio);

        setUpInjections();

        setUpHomeNavigation();

        setUpCast();

        prepareFiles();

        setUpAudio();
        setUpLastOpened();

        if (savedInstanceState != null) {
            audioListVisible = savedInstanceState.getBoolean(AUDIO_LIST_VISIBLE);
        }
    }

    private void setUpInjections() {
        AmahiApplication.from(this).inject(this);
    }

    private void setUpHomeNavigation() {
        Toolbar toolbar = findViewById(R.id.toolbar_audio);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setUpCast() {
        mCastContext = CastContext.getSharedInstance(this);
        mCastSession = mCastContext.getSessionManager().getCurrentCastSession();
        if (isCastConnected()) {
            mLocation = PlaybackLocation.REMOTE;
        }
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

    private void setUpAudio() {
        if (mCastSession != null && mCastSession.isConnected()) {
            loadRemoteMedia(0, true);
        } else {
            setUpAudioAdapter();
            setUpAudioPosition();
            setUpAudioTitle();
            setUpAudioListener();
        }
    }

    private void setUpAudioAdapter() {
        getAudioPager().setAdapter(new ServerFilesAudioPageAdapter(getSupportFragmentManager(), getShare(), getAudioFiles()));
    }

    private ViewPager getAudioPager() {
        return (ViewPager) findViewById(R.id.pager_audio);
    }

    private void setUpAudioPosition() {
        changeAudio = false;
        getAudioPager().setCurrentItem(getAudioFiles().indexOf(getFile()));
    }

    private void setUpAudioListener() {
        getAudioPager().addOnPageChangeListener(this);
    }

    private void setUpLastOpened() {
        FileInfoRepository fileInfoRepository = new FileInfoRepository(this);
        FileInfo fileInfo = new FileInfo(getFile().getUniqueKey(), DateTime.getCurrentTime());
        fileInfoRepository.insert(fileInfo);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    public void onPageSelected(int position) {
        setUpAudioTitle(getAudioFiles().get(position));
        if (changeAudio) {
            boolean isPlaying;
            if (audioService != null) {
                isPlaying = audioService.isAudioStarted();
                if (isPlaying) {
                    audioService.pauseAudio();
                }
            }
            BusProvider.getBus().post(new AudioControlChangeEvent(position));
        }
        changeAudio = true;
        saveAudioFileState(getFiles().get(position));
    }

    private boolean isCastConnected() {
        return mCastSession != null && mCastSession.isConnected();
    }

    private ServerFile getFile() {
        return getIntent().getParcelableExtra(Intents.Extras.SERVER_FILE);
    }

    private void setUpAudioTitle() {
        setUpAudioTitle(getFile());
    }

    private void setUpAudioTitle(ServerFile file) {
        getAudioTitleView().setText(file.getName());
    }

    private TextView getAudioTitleView() {
        return (TextView) findViewById(R.id.text_title);
    }

    private TextView getAudioSubtitleView() {
        return (TextView) findViewById(R.id.text_subtitle);
    }

    public ServerFilesAudioPageAdapter getAudioAdapter() {
        return (ServerFilesAudioPageAdapter) getAudioPager().getAdapter();
    }

    @Subscribe
    public void onAudioMetadataRetrieved(AudioMetadataRetrievedEvent event) {
        ServerFile audioFile = getFile();
        if (audioFile != null && event.getServerFile() != null &&
            audioFile.getUniqueKey().equals(event.getServerFile().getUniqueKey())) {
            final AudioMetadata metadata = event.getAudioMetadata();
            this.metadataFormatter = new AudioMetadataFormatter(
                metadata.getAudioTitle(), metadata.getAudioArtist(), metadata.getAudioAlbum());
            this.metadataFormatter.setDuration(metadata.getDuration());
            if (mLocation == PlaybackLocation.LOCAL) {
                setUpAudioMetadata(metadataFormatter);
            } else if (mLocation == PlaybackLocation.REMOTE) {
                loadRemoteMedia(0, true);
                finish();
            }
        }
    }

    private void setUpAudioMetadata(AudioMetadataFormatter audioMetadataFormatter) {
        getAudioTitleView().setText(audioMetadataFormatter.getAudioTitle(getFile()));
        getAudioSubtitleView().setText(audioMetadataFormatter.getAudioSubtitle(getShare()));
    }

    private ServerShare getShare() {
        return getIntent().getParcelableExtra(Intents.Extras.SERVER_SHARE);
    }

    private Uri getAudioUri() {
        if (getShare() != null) {
            return serverClient.getFileUri(getShare(), getFile());
        } else {
            return getRecentFileUri();
        }
    }

    private Uri getRecentFileUri() {
        RecentFileRepository repository = new RecentFileRepository(this);
        return Uri.parse(repository.getRecentFile(getFile().getUniqueKey()).getUri());
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mLocation == PlaybackLocation.LOCAL) {
            setUpAudioService();
            setUpAudioServiceBind();
        } else if (mLocation == PlaybackLocation.REMOTE) {
            AudioMetadataRetrievingTask.newInstance(this, getAudioUri(), getFile()).execute();
        }
    }

    private void setUpAudioService() {
        Intent intent = new Intent(this, AudioService.class);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    private void setUpAudioServiceBind() {
        Intent intent = new Intent(this, AudioService.class);
        bindService(intent, this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onServiceDisconnected(ComponentName serviceName) {
    }

    @Override
    public void onServiceConnected(ComponentName serviceName, IBinder serviceBinder) {
        setUpAudioServiceBind(serviceBinder);

        setUpAudioPlayback();

        changeAudio = false;
        getAudioPager().setCurrentItem(audioService.getAudioPosition(), false);
        changeAudio = true;
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        setUpAudioControls();
        if (isAudioServiceAvailable()) {
            getPlayerControlView().setPlayer(audioService.getAudioPlayer());
        }
    }

    private void setUpAudioServiceBind(IBinder serviceBinder) {
        AudioService.AudioServiceBinder audioServiceBinder = (AudioService.AudioServiceBinder) serviceBinder;
        audioService = audioServiceBinder.getAudioService();
    }

    private void setUpAudioControls() {
        if (isAudioServiceAvailable()) {
            getPlayerControlView().setPlayer(audioService.getAudioPlayer());
            getNextButton().setOnClickListener(this);
            getPreviousButton().setOnClickListener(this);
            getRepeatButton().setOnClickListener(this);
            getPlayerControlView().setShowShuffleButton(true);
            audioControlsAvailable = true;
        }
        setUpRepeatToggleIcon();
        getPlayerControlView().setShowTimeoutMs(0);
    }

    private PlayerControlView getPlayerControlView() {
        return findViewById(R.id.control_view);
    }

    private ImageButton getNextButton() {
        return getPlayerControlView().findViewById(R.id.m_exo_next);
    }

    private ImageButton getPreviousButton() {
        return getPlayerControlView().findViewById(R.id.exo_prev);
    }

    private ImageButton getRepeatButton() {
        return getPlayerControlView().findViewById(R.id.m_exo_repeat_toggle);
    }

    private void setUpRepeatToggleIcon() {
        if (Preferences.getAudioRepeatMode(this) == REPEAT_ONE) {
            REPEAT_MODE = REPEAT_ONE;
            getRepeatButton().setImageResource(R.drawable.exo_controls_repeat_one);
        } else {
            REPEAT_MODE = REPEAT_ALL;
            getRepeatButton().setImageResource(R.drawable.exo_controls_repeat_all);
        }

    }

    private boolean areAudioControlsAvailable() {
        return audioControlsAvailable;
    }

    private void setUpAudioPlayback() {
        if (!areAudioControlsAvailable()) {
            setUpAudioControls();
        }

        if (audioService.isAudioStarted()) {
            if (getFile().getUniqueKey().equals(audioService.getAudioFile().getUniqueKey())) {
                setUpAudioMetadata();
                return;
            }
        }

        audioService.startAudio(getShare(), getAudioFiles(), getFile());
        audioService.enableShuffle(Preferences.getAudioShuffleMode(this));
        audioService.setPlayPosition(0);
    }

    private List<ServerFile> getAudioFiles() {
        List<ServerFile> audioFiles = new ArrayList<ServerFile>();

        for (ServerFile file : getFiles()) {
            if (SUPPORTED_FORMATS.contains(file.getMime())) {
                audioFiles.add(file);
            }
        }

        return audioFiles;
    }

    private List<ServerFile> getFiles() {
        return getIntent().getParcelableArrayListExtra(Intents.Extras.SERVER_FILES);
    }

    @Subscribe
    public void onAudioPrepared(AudioPreparedEvent event) {

    }

    @Subscribe
    public void onNextAudio(AudioControlNextEvent event) {
        int audioPosition;
        if (REPEAT_MODE == REPEAT_ONE) {
            audioPosition = getAudioPager().getCurrentItem();
        } else if (audioService.isShuffleEnabled()) {
            List<ServerFile> shuffledAudioFiles = audioService.getShuffledAudioFiles();
            audioPosition = shuffledAudioFiles.indexOf(getFile()) + 1;
            if (audioPosition == shuffledAudioFiles.size()) {
                audioPosition = 0;
            }

            audioPosition = getAudioFiles().indexOf(shuffledAudioFiles.get(audioPosition));
        } else {
            audioPosition = getAudioPager().getCurrentItem();
            audioPosition += 1;
            if (audioPosition == getAudioFiles().size()) {
                audioPosition = 0;
            }
        }
        changeAudio = false;
        getAudioPager().setCurrentItem(audioPosition);
        saveAudioFileState(getAudioFiles().get(audioPosition));
    }

    private void saveAudioFileState(ServerFile file) {
        getIntent().putExtra(Intents.Extras.SERVER_FILE, file);
    }

    @Subscribe
    public void onPreviousAudio(AudioControlPreviousEvent event) {
        int audioPosition;
        if (REPEAT_MODE == REPEAT_ONE) {
            audioPosition = getAudioPager().getCurrentItem();
        } else if (audioService.isShuffleEnabled()) {
            List<ServerFile> shuffledAudioFiles = audioService.getShuffledAudioFiles();
            audioPosition = shuffledAudioFiles.indexOf(getFile()) - 1;
            if (audioPosition == -1) {
                audioPosition = shuffledAudioFiles.size() - 1;
            }

            audioPosition = getAudioFiles().indexOf(shuffledAudioFiles.get(audioPosition));
        } else {
            audioPosition = getAudioPager().getCurrentItem();
            audioPosition -= 1;
            if (audioPosition == -1) {
                audioPosition = getAudioFiles().size() - 1;
            }
        }
        changeAudio = false;
        getAudioPager().setCurrentItem(audioPosition);
        saveAudioFileState(getAudioFiles().get(audioPosition));
    }

    @Subscribe
    public void onChangeAudio(AudioControlChangeEvent event) {
        changeAudio = false;
        getAudioPager().setCurrentItem(event.getPosition());
        setUpAudioMetadata();
    }

    @Subscribe
    public void onAudioCompleted(AudioCompletedEvent event) {

        int audioPosition;
        if (REPEAT_MODE == REPEAT_ONE) {
            audioPosition = getAudioPager().getCurrentItem();
        } else if (audioService.isShuffleEnabled()) {
            List<ServerFile> shuffledAudioFiles = audioService.getShuffledAudioFiles();
            audioPosition = shuffledAudioFiles.indexOf(getFile()) + 1;
            if (audioPosition == shuffledAudioFiles.size()) {
                audioPosition = 0;
            }

            audioPosition = getAudioFiles().indexOf(shuffledAudioFiles.get(audioPosition));
        } else {
            audioPosition = getAudioPager().getCurrentItem();
            audioPosition += 1;
            if (audioPosition == getAudioFiles().size()) {
                audioPosition = 0;
            }

        }
        changeAudio = false;
        getAudioPager().setCurrentItem(audioPosition);
        saveAudioFileState(getAudioFiles().get(audioPosition));
    }

    @Override
    public void start() {
        audioService.playAudio();
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public void pause() {
        audioService.pauseAudio();
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
        audioService.getAudioPlayer().seekTo(time);
    }

    @Override
    public int getDuration() {
        return (int) audioService.getAudioPlayer().getDuration();
    }

    @Override
    public int getCurrentPosition() {
        return (int) audioService.getAudioPlayer().getCurrentPosition();
    }

    @Override
    public boolean isPlaying() {
        return audioService.getAudioPlayer().getPlayWhenReady();
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.action_bar_audio_files, menu);
        setUpAudioListIcon(menu);
        CastButtonFactory.setUpMediaRouteButton(getApplicationContext(), menu,
            R.id.media_route_menu_item);
        return true;
    }

    public void setUpAudioListIcon(Menu menu) {
        MenuItem audioListItem = menu.findItem(R.id.menu_audio_list);
        if (audioListVisible) {
            audioListItem.setIcon(setDrawableTintActivated());
        } else {
            audioListItem.setIcon(setDrawableTintNormal());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int itemId = menuItem.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        } else if (itemId == R.id.menu_audio_list) {
            toggleAudioList();
            toggleAudioListIcon(menuItem);
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    private void toggleAudioList() {
        if (isAudioListVisible()) {
            hideAudioList();
        } else {
            showAudioList();
        }

    }

    private boolean isAudioListVisible() {
        AudioListFragment fragment = (AudioListFragment) getSupportFragmentManager().findFragmentByTag(AudioListFragment.TAG);
        return fragment != null;
    }

    private void hideAudioList() {
        AudioListFragment fragment = (AudioListFragment) getSupportFragmentManager().findFragmentByTag(AudioListFragment.TAG);

        getSupportFragmentManager().beginTransaction()
            .remove(fragment)
            .commit();
    }

    private void showAudioList() {
        AudioListFragment fragment = (AudioListFragment) getSupportFragmentManager().findFragmentByTag(AudioListFragment.TAG);
        if (fragment == null) {
            addAudioList();
        }
    }

    private void addAudioList() {
        AudioListFragment fragment = Fragments.Builder.buildAudioListFragment(getFile(),
            getShare(),
            (ArrayList<ServerFile>) getFiles());

        getSupportFragmentManager().beginTransaction()
            .replace(R.id.audio_list_container, fragment, AudioListFragment.TAG)
            .commit();
    }

    private void toggleAudioListIcon(MenuItem menuItem) {
        Drawable drawable = setUpIconDrawable();
        menuItem.setIcon(drawable);

    }

    private Drawable setUpIconDrawable() {
        if (isAudioListVisible()) {
            return setDrawableTintNormal();
        } else {
            return setDrawableTintActivated();
        }
    }

    private Drawable setDrawableTintActivated() {
        Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_queue_music_24dp, null);
        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(drawable, getResources().getColor(R.color.accent));
        return drawable;
    }

    private Drawable setDrawableTintNormal() {
        Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_queue_music_24dp, null);
        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(drawable, getResources().getColor(R.color.primary_text));
        return drawable;

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(AUDIO_LIST_VISIBLE, isAudioListVisible());
    }

    @Override
    protected void onResume() {
        super.onResume();

        mCastContext.getSessionManager().addSessionManagerListener(this, CastSession.class);

        BusProvider.getBus().register(this);

        setUpAudioMetadata();
        changeAudio = true;
    }

    private void setUpAudioMetadata() {
        if (!isAudioServiceAvailable()) {
            return;
        }

        if (audioService.getAudioMetadataFormatter() != null) {
            setUpAudioMetadata(audioService.getAudioMetadataFormatter());
        }
    }

    private boolean isAudioServiceAvailable() {
        return audioService != null;
    }

    @Override
    protected void onPause() {
        super.onPause();

        mCastContext.getSessionManager().removeSessionManagerListener(this, CastSession.class);

        BusProvider.getBus().unregister(this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (isAudioServiceAvailable()) {
            tearDownAudioServiceBind();
        }
    }

    private void tearDownAudioServiceBind() {
        unbindService(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (isFinishing()) {
            if (audioService != null &&
                (!audioService.getAudioPlayer().getPlayWhenReady() || getShare() == null)) {
                tearDownAudioService();
            }
        }
    }

    private void tearDownAudioService() {
        Intent intent = new Intent(this, AudioService.class);
        stopService(intent);
    }

    @Override
    public void onSessionEnded(CastSession session, int error) {
        onApplicationDisconnected();
    }

    @Override
    public void onSessionResumed(CastSession session, boolean wasSuspended) {
        onApplicationConnected(session);
    }

    @Override
    public void onSessionResumeFailed(CastSession session, int error) {
        onApplicationDisconnected();
    }

    @Override
    public void onSessionStarted(CastSession session, String sessionId) {
        onApplicationConnected(session);
    }

    @Override
    public void onSessionStartFailed(CastSession session, int error) {
        onApplicationDisconnected();
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
        boolean isPlaying = false;
        int position = 0;
        if (audioService != null) {
            isPlaying = audioService.isAudioStarted();
            if (isPlaying) {
                audioService.pauseAudio();
                position = (int) audioService.getAudioPlayer().getCurrentPosition();
            }
        }
        loadRemoteMedia(position, isPlaying);
        finish();
    }

    private void onApplicationDisconnected() {
        mCastSession = null;
        mLocation = PlaybackLocation.LOCAL;
        invalidateOptionsMenu();
        if (!isAudioServiceAvailable()) {
            setUpAudioService();
            setUpAudioServiceBind();
        }
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
                Intent intent = new Intent(ServerFileAudioActivity.this, ExpandedControlsActivity.class);
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
        MediaLoadOptions mediaLoadOptions = new MediaLoadOptions.Builder()
            .setAutoplay(autoPlay)
            .setPlayPosition(position)
            .build();
        remoteMediaClient.load(buildMediaInfo(), mediaLoadOptions);
    }

    private MediaInfo buildMediaInfo() {
        MediaMetadata audioMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MUSIC_TRACK);

        if (metadataFormatter != null) {
            audioMetadata.putString(MediaMetadata.KEY_TITLE, metadataFormatter.getAudioTitle(getFile()));
            audioMetadata.putString(MediaMetadata.KEY_ARTIST, metadataFormatter.getAudioArtist());
            audioMetadata.putString(MediaMetadata.KEY_ALBUM_TITLE, metadataFormatter.getAudioAlbum());
        } else {
            audioMetadata.putString(MediaMetadata.KEY_TITLE, getFile().getNameOnly());
            audioMetadata.putString(MediaMetadata.KEY_ARTIST, "");
            audioMetadata.putString(MediaMetadata.KEY_ALBUM_TITLE, "");
        }

        String audioSource = getRemoteUri(getFile());
        MediaInfo.Builder builder = new MediaInfo.Builder(audioSource)
            .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
            .setContentType(getFile().getMime())
            .setMetadata(audioMetadata);
        if (metadataFormatter != null) {
            builder.setStreamDuration(metadataFormatter.getDuration());
        }
        return builder.build();
    }

    private String getRemoteUri(ServerFile serverFile) {
        OfflineFileRepository repository = new OfflineFileRepository(this);
        OfflineFile offlineFile = repository.getOfflineFile(serverFile.getName(), serverFile.getModificationTime().getTime());
        if (offlineFile != null) {
            return offlineFile.getFileUri();
        } else {
            return getAudioUri().toString();
        }
    }

    @Override
    public void onClick(View v) {
        if (v == getNextButton()) {
            BusProvider.getBus().post(new AudioControlNextEvent());
        }
        if (v == getPreviousButton()) {
            BusProvider.getBus().post(new AudioControlPreviousEvent());
        }
        if (v == getRepeatButton()) {
            if (isAudioServiceAvailable()) {
                if (REPEAT_MODE == REPEAT_ALL) {
                    REPEAT_MODE = REPEAT_ONE;
                    getRepeatButton().setImageResource(R.drawable.exo_controls_repeat_one);
                } else if (REPEAT_MODE == REPEAT_ONE) {
                    REPEAT_MODE = REPEAT_ALL;
                    getRepeatButton().setImageResource(R.drawable.exo_controls_repeat_all);
                }
                Preferences.setAudioRepeatMode(this, REPEAT_MODE);
            }
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    private enum PlaybackLocation {
        LOCAL,
        REMOTE
    }
}
