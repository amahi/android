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

package org.amahi.anywhere.tv.fragment;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.RequiresApi;
import android.support.v17.leanback.app.PlaybackFragment;
import android.support.v17.leanback.widget.Action;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ClassPresenterSelector;
import android.support.v17.leanback.widget.ControlButtonPresenterSelector;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.PlaybackControlsRow;
import android.support.v17.leanback.widget.PlaybackControlsRowPresenter;
import android.support.v4.content.ContextCompat;
import android.widget.ImageView;

import com.squareup.otto.Subscribe;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.R;
import org.amahi.anywhere.bus.AudioMetadataRetrievedEvent;
import org.amahi.anywhere.bus.BusProvider;
import org.amahi.anywhere.model.AudioMetadata;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerShare;
import org.amahi.anywhere.task.AudioMetadataRetrievingTask;
import org.amahi.anywhere.tv.presenter.AudioDetailsDescriptionPresenter;
import org.amahi.anywhere.tv.presenter.MainTVPresenter;
import org.amahi.anywhere.util.Fragments;
import org.amahi.anywhere.util.Intents;
import org.amahi.anywhere.util.Mimes;

import java.io.IOException;
import java.util.ArrayList;

import javax.inject.Inject;

public class TvPlaybackAudioFragment extends PlaybackFragment {

    private static final int DEFAULT_UPDATE_PERIOD = 1000;
    private static final int UPDATE_PERIOD = 16;
    @Inject
    ServerClient serverClient;
    private ArrayObjectAdapter mRowsAdapter;
    private ArrayObjectAdapter mPrimaryActionsAdapter;
    private PlaybackControlsRow mPlaybackControlsRow;
    private PlaybackControlsRow.PlayPauseAction mPlayPauseAction;
    private PlaybackControlsRow.SkipNextAction mSkipNextAction;
    private PlaybackControlsRow.SkipPreviousAction mSkipPreviousAction;
    private PlaybackControlsRow.FastForwardAction mFastForwardAction;
    private PlaybackControlsRow.RewindAction mRewindAction;
    private int mCurrentPlaybackState;
    private Handler mHandler;
    private Runnable mRunnable;
    private MediaPlayer mediaPlayer;
    private ArrayList<ServerFile> mAudioList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setFadingEnabled(false);

        setBackgroundType(BG_DARK);

        setUpInjections();

        mHandler = new Handler(Looper.getMainLooper());

        setUpRows();

        getAllAudioFiles();

        AudioMetadataRetrievingTask.newInstance(getActivity(), getFileUri(), getAudioFile()).execute();

        mediaPlayer = new MediaPlayer();

        mediaPlayer.setOnCompletionListener(mp -> skipNext());

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        setDataSource();

        prepareAudio();

        mediaPlayer.start();

        setOnItemViewClickedListener((OnItemViewClickedListener) (itemViewHolder, item, rowViewHolder, row) -> {
            if (item instanceof ServerFile) {
                ServerFile serverFile = (ServerFile) item;
                replaceFragment(serverFile);
            }
        });
    }

    private void setUpInjections() {
        AmahiApplication.from(getActivity()).inject(this);
    }

    private void replaceFragment(ServerFile serverFile) {
        getFragmentManager().beginTransaction().replace(R.id.playback_controls_fragment_container, Fragments.Builder.buildAudioFragment(serverFile, getAudioShare(), getAudioFiles())).commit();
    }

    private void setUpRows() {
        ClassPresenterSelector ps = new ClassPresenterSelector();
        PlaybackControlsRowPresenter playbackControlsRowPresenter = new PlaybackControlsRowPresenter(new AudioDetailsDescriptionPresenter(getActivity()));

        ps.addClassPresenter(PlaybackControlsRow.class, playbackControlsRowPresenter);
        ps.addClassPresenter(ListRow.class, new ListRowPresenter());
        mRowsAdapter = new ArrayObjectAdapter(ps);
        playbackControlsRowPresenter.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.primary));
        playbackControlsRowPresenter.setProgressColor(Color.WHITE);
        playbackControlsRowPresenter.setOnActionClickedListener(action -> {
            if (action.getId() == mPlayPauseAction.getId()) {
                togglePlayPause(mPlayPauseAction.getIndex() == PlaybackControlsRow.PlayPauseAction.PAUSE);
            } else if (action.getId() == mRewindAction.getId()) {
                rewind();
            } else if (action.getId() == mFastForwardAction.getId()) {
                fastForward();
            } else if (action.getId() == mSkipNextAction.getId()) {
                skipNext();
            } else if (action.getId() == mSkipPreviousAction.getId()) {
                skipPrevious();
            }
            if (action instanceof PlaybackControlsRow.MultiAction) {
                notifyChanged(action);
            }
        });
        setAdapter(mRowsAdapter);
    }

    private void setDataSource() {
        try {
            mediaPlayer.setDataSource(getActivity(), getFileUri());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void prepareAudio() {
        try {
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void togglePlayPause(boolean isPaused) {
        if (isPaused) {
            mediaPlayer.pause();
        } else {
            mediaPlayer.start();
        }
        playbackStateChanged();
    }

    public void rewind() {
        if (mediaPlayer.getCurrentPosition() - (10 * 1000) > 0) {
            mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() - (10 * 1000));
            mPlaybackControlsRow.setCurrentTime(mediaPlayer.getCurrentPosition());
        }
    }

    public void fastForward() {
        if (mediaPlayer.getCurrentPosition() + (10 * 1000) <= mediaPlayer.getDuration()) {
            mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() + (10 * 1000));
            mPlaybackControlsRow.setCurrentTime(mediaPlayer.getCurrentPosition());
        }
    }

    private void skipNext() {
        int presentIndex = getAudioFiles().indexOf(getAudioFile());
        if (presentIndex < (getAudioFiles().size() - 1))
            replaceFragment(getAudioFiles().get(presentIndex + 1));
        else
            replaceFragment(getAudioFiles().get(0));
    }

    private void skipPrevious() {
        int presentIndex = getAudioFiles().indexOf(getAudioFile());
        if (presentIndex > 0)
            replaceFragment(getAudioFiles().get(presentIndex - 1));
        else
            replaceFragment(getAudioFiles().get(getAudioFiles().size() - 1));
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void playbackStateChanged() {

        if (mCurrentPlaybackState != PlaybackState.STATE_PLAYING) {
            mCurrentPlaybackState = PlaybackState.STATE_PLAYING;
            startProgressAutomation();
            setFadingEnabled(false);
            mPlayPauseAction.setIndex(PlaybackControlsRow.PlayPauseAction.PAUSE);
            mPlayPauseAction.setIcon(mPlayPauseAction.getDrawable(PlaybackControlsRow.PlayPauseAction.PAUSE));
            notifyChanged(mPlayPauseAction);
        } else {
            mCurrentPlaybackState = PlaybackState.STATE_PAUSED;
            stopProgressAutomation();
            mPlayPauseAction.setIndex(PlaybackControlsRow.PlayPauseAction.PLAY);
            mPlayPauseAction.setIcon(mPlayPauseAction.getDrawable(PlaybackControlsRow.PlayPauseAction.PLAY));
            notifyChanged(mPlayPauseAction);
        }
    }

    private void startProgressAutomation() {
        if (mRunnable == null) {
            mRunnable = new Runnable() {
                @Override
                public void run() {
                    int updatePeriod = getUpdatePeriod();
                    int currentTime = mPlaybackControlsRow.getCurrentTime() + updatePeriod;
                    int totalTime = mPlaybackControlsRow.getTotalTime();
                    mPlaybackControlsRow.setCurrentTime(currentTime);

                    if (totalTime > 0 && totalTime <= currentTime) {
                        stopProgressAutomation();
                    } else {
                        mHandler.postDelayed(this, updatePeriod);
                    }
                }
            };
            mHandler.postDelayed(mRunnable, getUpdatePeriod());
        }
    }

    private void stopProgressAutomation() {
        if (mHandler != null && mRunnable != null) {
            mHandler.removeCallbacks(mRunnable);
            mRunnable = null;
        }
    }

    private int getUpdatePeriod() {
        if (getView() == null || mPlaybackControlsRow.getTotalTime() <= 0) {
            return DEFAULT_UPDATE_PERIOD;
        }
        return Math.max(UPDATE_PERIOD, mPlaybackControlsRow.getTotalTime() / getView().getWidth());
    }

    private void notifyChanged(Action action) {
        ArrayObjectAdapter adapter = mPrimaryActionsAdapter;
        if (adapter.indexOf(action) >= 0) {
            adapter.notifyArrayItemRangeChanged(adapter.indexOf(action), 1);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void addPlaybackControlsRow(AudioMetadataRetrievedEvent event) {
        mPlaybackControlsRow = new PlaybackControlsRow(event);
        mRowsAdapter.add(mPlaybackControlsRow);

        ControlButtonPresenterSelector presenterSelector = new ControlButtonPresenterSelector();
        mPrimaryActionsAdapter = new ArrayObjectAdapter(presenterSelector);
        mPlaybackControlsRow.setPrimaryActionsAdapter(mPrimaryActionsAdapter);

        mSkipPreviousAction = new PlaybackControlsRow.SkipPreviousAction(getActivity());
        mRewindAction = new PlaybackControlsRow.RewindAction(getActivity());
        mPlayPauseAction = new PlaybackControlsRow.PlayPauseAction(getActivity());
        mFastForwardAction = new PlaybackControlsRow.FastForwardAction(getActivity());
        mSkipNextAction = new PlaybackControlsRow.SkipNextAction(getActivity());
        mPrimaryActionsAdapter.add(mSkipPreviousAction);
        mPrimaryActionsAdapter.add(mRewindAction);
        mPrimaryActionsAdapter.add(mPlayPauseAction);
        mPrimaryActionsAdapter.add(mFastForwardAction);
        mPrimaryActionsAdapter.add(mSkipNextAction);
        playbackStateChanged();
    }

    private void addOtherRows() {
        ArrayObjectAdapter adapter = new ArrayObjectAdapter(new MainTVPresenter(getActivity(), serverClient, getAudioShare()));
        for (ServerFile serverFile : getAudioFiles()) {
            adapter.add(serverFile);
        }

        HeaderItem header;

        if (getAudioFile().getParentFile() == null)
            header = new HeaderItem(0, "Song(s) in " + getAudioShare().getName());
        else
            header = new HeaderItem(0, "Song(s) in " + getAudioFile().getParentFile().getName());

        mRowsAdapter.add(new ListRow(header, adapter));
    }

    private boolean isAudio(ServerFile serverFile) {
        return Mimes.match(serverFile.getMime()) == Mimes.Type.AUDIO;
    }

    private ServerFile getAudioFile() {
        return getArguments().getParcelable(Intents.Extras.SERVER_FILE);
    }

    private ArrayList<ServerFile> getAudioFiles() {
        return mAudioList;
    }

    private ArrayList<ServerFile> getAllAudioFiles() {
        mAudioList = new ArrayList<>();
        ArrayList<ServerFile> allFiles = getArguments().getParcelableArrayList(Intents.Extras.SERVER_FILES);
        if (allFiles != null) {
            for (ServerFile serverFile : allFiles) {
                if (isAudio(serverFile)) {
                    mAudioList.add(serverFile);
                }
            }
        }
        return mAudioList;
    }

    private ServerShare getAudioShare() {
        return getArguments().getParcelable(Intents.Extras.SERVER_SHARE);
    }

    private Uri getFileUri() {
        return serverClient.getFileUri(getAudioShare(), getAudioFile());
    }

    private ImageView getBackground() {
        return getActivity().findViewById(R.id.imageViewBackground);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Subscribe
    public void onAudioMetadataRetrieved(AudioMetadataRetrievedEvent event) {
        addPlaybackControlsRow(event);
        addOtherRows();
        mediaPlayer.setOnBufferingUpdateListener((mp, percent) -> {
            int time = percent * mediaPlayer.getDuration();
            mPlaybackControlsRow.setBufferedProgress(time);
        });
        mPlaybackControlsRow.setTotalTime(mediaPlayer.getDuration());
        AudioMetadata metadata = event.getAudioMetadata();
        if (metadata.getAudioAlbumArt() != null) {
            getBackground().setPadding(0, 0, 0, 0);
            getBackground().setImageBitmap(metadata.getAudioAlbumArt());
            mPlaybackControlsRow.setImageBitmap(getActivity(), metadata.getAudioAlbumArt());
        } else {
            Drawable audioDrawable = ContextCompat.getDrawable(getActivity(), R.drawable.tv_ic_audio);
            getBackground().setPadding(100, 100, 100, 100);
            getBackground().setImageDrawable(audioDrawable);
        }
        BusProvider.getBus().unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        BusProvider.getBus().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        mediaPlayer.pause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaPlayer.release();
        mediaPlayer = null;
    }

    public PlaybackControlsRow.PlayPauseAction getmPlayPauseAction() {
        return mPlayPauseAction;
    }
}
