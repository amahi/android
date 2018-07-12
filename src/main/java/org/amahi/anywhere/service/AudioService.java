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

package org.amahi.anywhere.service;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.squareup.otto.Subscribe;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.R;
import org.amahi.anywhere.bus.AudioCompletedEvent;
import org.amahi.anywhere.bus.AudioControlChangeEvent;
import org.amahi.anywhere.bus.AudioControlNextEvent;
import org.amahi.anywhere.bus.AudioControlPauseEvent;
import org.amahi.anywhere.bus.AudioControlPlayEvent;
import org.amahi.anywhere.bus.AudioControlPlayPauseEvent;
import org.amahi.anywhere.bus.AudioControlPreviousEvent;
import org.amahi.anywhere.bus.AudioMetadataRetrievedEvent;
import org.amahi.anywhere.bus.AudioPreparedEvent;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.db.entities.OfflineFile;
import org.amahi.anywhere.db.entities.RecentFile;
import org.amahi.anywhere.db.repositories.OfflineFileRepository;
import org.amahi.anywhere.db.repositories.RecentFileRepository;
import org.amahi.anywhere.model.AudioMetadata;
import org.amahi.anywhere.receiver.AudioReceiver;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerShare;
import org.amahi.anywhere.task.AudioMetadataRetrievingTask;
import org.amahi.anywhere.util.AudioMetadataFormatter;
import org.amahi.anywhere.util.Downloader;
import org.amahi.anywhere.util.Identifier;
import org.amahi.anywhere.util.Intents;
import org.amahi.anywhere.util.MediaNotificationManager;
import org.amahi.anywhere.util.Preferences;

import java.io.File;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

/**
 * Audio server. Does all the work related to the audio playback.
 * Places information at {@link android.app.Notification} and {@link MediaSessionCompat},
 * handles audio focus changes as well.
 */
public class AudioService extends MediaBrowserServiceCompat implements
    AudioManager.OnAudioFocusChangeListener,
    Player.EventListener {
    @Inject
    ServerClient serverClient;
    private MediaNotificationManager mMediaNotificationManager;
    private SimpleExoPlayer audioPlayer;
    private MediaSessionCompat mediaSession;
    private AudioFocus audioFocus;

    private ServerShare audioShare;
    private List<ServerFile> audioFiles;
    private ServerFile audioFile;
    private boolean isPreparing = false;

    private AudioMetadataFormatter audioMetadataFormatter;
    private Bitmap audioAlbumArt;

    @Override
    public IBinder onBind(Intent intent) {
        return new AudioServiceBinder(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        setUpInjections();

        setUpBus();

        setUpAudioPlayer();
        setUpAudioPlayerRemote();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MediaButtonReceiver.handleIntent(mediaSession, intent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        if (TextUtils.equals(clientPackageName, getPackageName())) {
            return new BrowserRoot(getString(R.string.application_name), null);
        }

        return null;
    }

    //Not important for general audio service, required for class
    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        result.sendResult(null);
    }

    private void setUpInjections() {
        AmahiApplication.from(this).inject(this);
    }

    private void setUpBus() {
        BusProvider.getBus().register(this);
    }

    private void setUpAudioPlayer() {
        audioPlayer =
            ExoPlayerFactory.newSimpleInstance(new DefaultRenderersFactory(this), new DefaultTrackSelector(), new DefaultLoadControl());
        audioPlayer.addListener(this);
        audioPlayer.setPlayWhenReady(true);
        audioPlayer.setVolume(1.0f);
    }

    private MediaSource buildMediaSource(Uri uri) {
        return new ExtractorMediaSource.Factory(
            new DefaultHttpDataSourceFactory(Identifier.getUserAgent(this)))
            .createMediaSource(uri);
    }

    private void setUpAudioPlayerRemote() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        ComponentName audioReceiver = new ComponentName(getPackageName(), AudioReceiver.class.getName());

        Intent audioIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        audioIntent.setComponent(audioReceiver);
        PendingIntent audioPendingIntent = PendingIntent.getBroadcast(this, 0, audioIntent, 0);

        mediaSession = new MediaSessionCompat(this, "PlayerService", audioReceiver, audioPendingIntent);
        mediaSession.setCallback(new MediaSessionCallback());
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
            MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setMediaButtonReceiver(audioPendingIntent);
        setSessionToken(mediaSession.getSessionToken());

        try {
            mMediaNotificationManager = new MediaNotificationManager(this);
        } catch (RemoteException e) {
            throw new IllegalStateException("Could not create a MediaNotificationManager", e);
        }

        mediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
            .setState(PlaybackStateCompat.STATE_NONE, 0, 0)
            .setActions(getAvailableActions())
            .build());

        if (audioManager != null) {
            audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        }
    }

    public boolean isAudioStarted() {
        return (audioFiles != null) && (audioFile != null);
    }

    public void startAudio(ServerShare audioShare, List<ServerFile> audioFiles, ServerFile audioFile) {
        this.audioShare = audioShare;
        this.audioFiles = audioFiles;
        this.audioFile = audioFile;

        pauseAudio();
        setUpAudioPlayback();
        setUpAudioMetadata();
    }

    private void setUpAudioPlayback() {
        setUpRecentFiles();

        MediaSource mediaSource;
        if (isFileAvailableOffline(getAudioFile())) {
            mediaSource = new ExtractorMediaSource.Factory(
                new DefaultDataSourceFactory(this, Identifier.getUserAgent(this)))
                .createMediaSource(getOfflineFileUri(getAudioFile().getName()));
        } else {
            mediaSource = buildMediaSource(getAudioUri());
        }
        audioPlayer.prepare(mediaSource, true, true);
        isPreparing = true;
    }

    private void setUpRecentFiles() {
        String uri;
        long size;
        if (isFileAvailableOffline(getAudioFile())) {
            uri = getUriFrom(getAudioFile().getName(), getAudioFile().getModificationTime());
            size = new File(getOfflineFileUri(getAudioFile().getName()).toString()).length();
        } else {
            uri = getAudioUri().toString();
            size = getAudioFile().getSize();
        }

        String serverName = Preferences.getServerName(this);

        RecentFile recentFile = new RecentFile(audioFile.getUniqueKey(),
            uri,
            serverName,
            System.currentTimeMillis(),
            size);
        RecentFileRepository recentFileRepository = new RecentFileRepository(this);
        recentFileRepository.insert(recentFile);
    }

    private String getUriFrom(String name, Date modificationTime) {
        OfflineFileRepository repository = new OfflineFileRepository(this);
        OfflineFile offlineFile = repository.getOfflineFile(name, modificationTime.getTime());
        return offlineFile.getFileUri();
    }

    private boolean isFileAvailableOffline(ServerFile serverFile) {
        OfflineFileRepository repository = new OfflineFileRepository(this);
        OfflineFile file = repository.getOfflineFile(serverFile.getName(), serverFile.getModificationTime().getTime());
        return file != null && file.getState() == OfflineFile.DOWNLOADED;
    }

    private Uri getOfflineFileUri(String name) {
        return Uri.parse(getFilesDir() + "/" + Downloader.OFFLINE_PATH + "/" + name);
    }

    private Uri getAudioUri() {
        if (audioShare != null) {
            return serverClient.getFileUri(audioShare, getAudioFile());
        } else {
            return getRecentFileUri();
        }
    }

    private Uri getRecentFileUri() {
        RecentFileRepository repository = new RecentFileRepository(this);
        return Uri.parse(repository.getRecentFile(getAudioFile().getUniqueKey()).getUri());
    }

    private void setUpAudioMetadata() {
        if (audioShare != null) {
            // Clear any previous metadata
            tearDownAudioMetadataFormatter();
            // Start fetching new metadata in the background
            AudioMetadataRetrievingTask
                .newInstance(this, getAudioUri(), audioFile)
                .execute();
        }
    }

    @Subscribe
    public void onAudioMetadataRetrieved(AudioMetadataRetrievedEvent event) {
        if (audioFile != null && event.getServerFile() != null && audioFile.getUniqueKey().equals(event.getServerFile().getUniqueKey())) {
            final AudioMetadata metadata = event.getAudioMetadata();
            this.audioMetadataFormatter = new AudioMetadataFormatter(
                metadata.getAudioTitle(), metadata.getAudioArtist(), metadata.getAudioAlbum());
            this.audioMetadataFormatter.setDuration(metadata.getDuration());
            this.audioAlbumArt = metadata.getAudioAlbumArt();
            setUpAudioPlayerRemote(audioMetadataFormatter, audioAlbumArt);

            mMediaNotificationManager.startNotification();
        }
    }

    private void setUpAudioPlayerRemote(AudioMetadataFormatter audioMetadataFormatter, Bitmap audioAlbumArt) {

        mediaSession.setMetadata(new MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, audioMetadataFormatter.getAudioTitle(audioFile))
            .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, audioMetadataFormatter.getAudioSubtitle(audioShare))
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, audioMetadataFormatter.getAudioSubtitle(audioShare))
            .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, getAudioPlayerRemoteArtwork(audioAlbumArt))
            .build());
    }

    private Bitmap getAudioPlayerRemoteArtwork(Bitmap audioAlbumArt) {
        if (audioAlbumArt == null) {
            return null;
        }

        Bitmap.Config artworkConfig = audioAlbumArt.getConfig();

        if (artworkConfig == null) {
            artworkConfig = Bitmap.Config.ARGB_8888;
        }

        return audioAlbumArt.copy(artworkConfig, false);
    }

    public void setPlayPosition(long playPosition) {
        getAudioPlayer().seekTo(playPosition);
        playAudio();
        BusProvider.getBus().post(new AudioPreparedEvent());
    }

    public PendingIntent createContentIntent() {
        Intent audioIntent = Intents.Builder.with(this).buildServerFileIntent(audioShare, audioFiles, audioFile);
        return PendingIntent.getActivity(this, 0, audioIntent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    public ServerFile getAudioFile() {
        return audioFile;
    }

    public ServerShare getAudioShare() {
        return audioShare;
    }

    public List<ServerFile> getAudioFiles() {
        return audioFiles;
    }

    public AudioMetadataFormatter getAudioMetadataFormatter() {
        return audioMetadataFormatter;
    }

    public Bitmap getAudioAlbumArt() {
        return audioAlbumArt;
    }

    public ExoPlayer getAudioPlayer() {
        return audioPlayer;
    }

    @Subscribe
    public void onAudioControlPlayPause(AudioControlPlayPauseEvent event) {
        if (audioPlayer.getPlayWhenReady()) {
            pauseAudio();
        } else {
            playAudio();
        }
    }

    @Subscribe
    public void onAudioControlPlay(AudioControlPlayEvent event) {
        playAudio();
    }

    @Subscribe
    public void onAudioControlPause(AudioControlPauseEvent event) {
        pauseAudio();
    }

    @Subscribe
    public void onAudioControlChange(AudioControlChangeEvent event) {
        startChangedAudio(event.getPosition());
    }

    @Subscribe
    public void onAudioControlNext(AudioControlNextEvent event) {
        tearDownAudioPlayback();
        startNextAudio();
    }

    @Subscribe
    public void onAudioControlPrevious(AudioControlPreviousEvent event) {
        tearDownAudioPlayback();
        startPreviousAudio();
    }

    private void startNextAudio() {
        this.audioFile = getNextAudioFile();
        setUpAudioPlayback();
        playAudio();
        setUpAudioMetadata();
    }

    private ServerFile getNextAudioFile() {
        int audioPosition = audioFiles.indexOf(audioFile);

        if (audioPosition == audioFiles.size() - 1) {
            return audioFiles.get(0);
        }
        return audioFiles.get(audioPosition + 1);
    }

    private void startPreviousAudio() {
        this.audioFile = getPreviousAudioFile();

        setUpAudioPlayback();
        playAudio();
        setUpAudioMetadata();
    }

    private ServerFile getPreviousAudioFile() {
        int audioPosition = audioFiles.indexOf(audioFile);

        if (audioPosition == 0) {
            audioPosition = audioFiles.size();
        }
        return audioFiles.get(audioPosition - 1);
    }

    @Subscribe
    public void onAudioCompleted(AudioCompletedEvent event) {
        startNextAudio();
    }

    public void playAudio() {
        mediaSession.setActive(true);
        audioPlayer.setPlayWhenReady(true);
        setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING);
    }

    public void pauseAudio() {
        mediaSession.setActive(false);
        audioPlayer.setPlayWhenReady(false);
        setMediaPlaybackState(PlaybackStateCompat.STATE_PAUSED);
    }

    private void setMediaPlaybackState(int state) {
        PlaybackStateCompat.Builder playbackStateBuilder = new PlaybackStateCompat.Builder();
        playbackStateBuilder.setActions(getAvailableActions());
        playbackStateBuilder.setState(state, audioPlayer.getCurrentPosition(), 1.0f, SystemClock.elapsedRealtime());
        mediaSession.setPlaybackState(playbackStateBuilder.build());
    }

    private long getAvailableActions() {
        long actions = PlaybackStateCompat.ACTION_PLAY_PAUSE |
            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
            PlaybackStateCompat.ACTION_SKIP_TO_NEXT;
        if (audioPlayer.getPlayWhenReady()) {
            actions |= PlaybackStateCompat.ACTION_PLAY;
        } else {
            actions |= PlaybackStateCompat.ACTION_PAUSE;
        }
        return actions;
    }

    private void tearDownAudioPlayback() {
        if (isAudioPlaying())
            pauseAudio();
    }

    private void startChangedAudio(int position) {
        this.audioFile = getChangedAudioFile(position);

        tearDownAudioPlayback();

        setUpAudioPlayback();
        playAudio();
        setUpAudioMetadata();
    }

    private ServerFile getChangedAudioFile(int position) {
        return audioFiles.get(position);
    }

    @Override
    public void onAudioFocusChange(int audioFocus) {
        switch (audioFocus) {
            case AudioManager.AUDIOFOCUS_GAIN:
                handleAudioFocusGain();
                break;

            case AudioManager.AUDIOFOCUS_LOSS:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                handleAudioFocusLoss();
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                handleAudioFocusDuck();
                break;

            default:
                break;
        }
    }

    private void handleAudioFocusGain() {
        if (isAudioPlaying()) {
            setUpAudioVolume();
        } else {
            if (audioFocus == AudioFocus.LOSS) {
                playAudio();
            }
        }

        this.audioFocus = AudioFocus.GAIN;
    }

    private boolean isAudioPlaying() {
        try {
            return isAudioStarted() && audioPlayer.getPlayWhenReady();
        } catch (IllegalStateException e) {
            return false;
        }
    }

    private void setUpAudioVolume() {
        audioPlayer.setVolume(1.0f);
    }

    private void handleAudioFocusLoss() {
        if (isAudioPlaying()) {
            pauseAudio();
            this.audioFocus = AudioFocus.LOSS;
        }
    }

    private void handleAudioFocusDuck() {
        if (isAudioPlaying()) {
            tearDownAudioVolume();
        }
    }

    private void tearDownAudioVolume() {
        audioPlayer.setVolume(0.3f);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        tearDownBus();

        tearDownAudioPlayer();
        tearDownAudioPlayerRemote();
        tearDownAudioPlayerNotification();
    }

    private void tearDownAudioMetadataFormatter() {
        audioMetadataFormatter = null;
    }

    private void tearDownBus() {
        BusProvider.getBus().unregister(this);
    }

    private void tearDownAudioPlayer() {
        audioPlayer.release();
    }

    private void tearDownAudioPlayerRemote() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        if (audioManager != null) {
            audioManager.abandonAudioFocus(this);
        }
        mediaSession.release();
    }

    private void tearDownAudioPlayerNotification() {
        mMediaNotificationManager.stopNotification();
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if (playbackState == Player.STATE_ENDED) {
            BusProvider.getBus().post(new AudioCompletedEvent());
        } else if (playbackState == Player.STATE_READY && isPreparing) {
            isPreparing = false;
            BusProvider.getBus().post(new AudioPreparedEvent());
        }
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        getAudioPlayer().stop();
        tearDownAudioMetadataFormatter();
    }

    @Override
    public void onPositionDiscontinuity(int reason) {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }

    @Override
    public void onSeekProcessed() {

    }

    public int getAudioPosition() {
        return audioFiles.indexOf(audioFile);
    }

    private enum AudioFocus {
        GAIN, LOSS
    }

    public static final class AudioServiceBinder extends Binder {
        private final AudioService audioService;

        public AudioServiceBinder(AudioService audioService) {
            this.audioService = audioService;
        }

        public AudioService getAudioService() {
            return audioService;
        }
    }

    private class MediaSessionCallback extends MediaSessionCompat.Callback {
        @Override
        public void onPlay() {
            playAudio();
        }

        @Override
        public void onPause() {
            pauseAudio();
        }

        @Override
        public void onSkipToNext() {
            BusProvider.getBus().post(new AudioControlNextEvent());
        }

        @Override
        public void onSkipToPrevious() {
            BusProvider.getBus().post(new AudioControlPreviousEvent());
        }
    }
}
