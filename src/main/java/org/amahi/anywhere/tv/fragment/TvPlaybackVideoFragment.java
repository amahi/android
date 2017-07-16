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
import android.graphics.PixelFormat;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v17.leanback.app.PlaybackFragment;
import android.support.v17.leanback.widget.Action;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ClassPresenterSelector;
import android.support.v17.leanback.widget.ControlButtonPresenterSelector;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnActionClickedListener;
import android.support.v17.leanback.widget.PlaybackControlsRow;
import android.support.v17.leanback.widget.PlaybackControlsRowPresenter;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import org.amahi.anywhere.AmahiApplication;
import org.amahi.anywhere.R;
import org.amahi.anywhere.server.client.ServerClient;
import org.amahi.anywhere.server.model.ServerFile;
import org.amahi.anywhere.server.model.ServerShare;
import org.amahi.anywhere.tv.presenter.VideoDetailsDescriptionPresenter;
import org.amahi.anywhere.util.Intents;
import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.util.ArrayList;

import javax.inject.Inject;


public class TvPlaybackVideoFragment extends PlaybackFragment {

    private static final int DEFAULT_UPDATE_PERIOD = 1000;
    private static final int UPDATE_PERIOD = 16;
    @Inject
    ServerClient serverClient;
    private SurfaceHolder mSurfaceHolder;
    private MediaPlayer mediaPlayer;
    private LibVLC mLibVlc;
    private Handler mHandler;
    private Runnable mRunnable;
    private PlaybackControlsRow mPlaybackControlsRow;
    private int mCurrentPlaybackState;
    private int mDuration;
    private ArrayObjectAdapter mRowsAdapter;
    private ArrayObjectAdapter mPrimaryActionsAdapter;
    private PlaybackControlsRow.PlayPauseAction mPlayPauseAction;
    private PlaybackControlsRow.FastForwardAction mFastForwardAction;
    private PlaybackControlsRow.RewindAction mRewindAction;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpInjections();
        setDuration();
        playVideo();
        setBackgroundType(BG_DARK);
        setFadingEnabled(false);
        mHandler = new Handler(Looper.getMainLooper());
        setUpRows();
    }

    private void setDuration() {
        android.media.MediaPlayer mediaPlayer = android.media.MediaPlayer.create(getActivity(), getFileUri());
        if(mediaPlayer!=null) {
            mDuration = mediaPlayer.getDuration();
            mediaPlayer.release();
        }
    }

    private void playVideo() {
        SurfaceView mSurfaceView = (SurfaceView) getActivity().findViewById(R.id.surfaceView);
        mSurfaceHolder = mSurfaceView.getHolder();
        setVideoHolder();
        setLibVlc();

        mediaPlayer = new MediaPlayer(mLibVlc);
        Media media = new Media(mLibVlc, getFileUri());
        mediaPlayer.setMedia(media);
        media.release();

        final IVLCVout vlcVout = mediaPlayer.getVLCVout();
        vlcVout.setVideoView(mSurfaceView);

        manageLayout(vlcVout);

        vlcVout.attachViews();
        mediaPlayer.play();
    }

    private void setUpInjections() {
        AmahiApplication.from(getActivity()).inject(this);
    }

    private void setVideoHolder() {
        mSurfaceHolder.setFormat(PixelFormat.RGBX_8888);
        mSurfaceHolder.setKeepScreenOn(true);
    }

    private void setLibVlc() {
        final ArrayList<String> args = new ArrayList<>();
        args.add("-vvv");
        mLibVlc = new LibVLC(getActivity(), args);
    }

    private void manageLayout(IVLCVout vlcVout) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        vlcVout.setWindowSize(displayMetrics.widthPixels, displayMetrics.heightPixels);
    }

    private void setUpRows() {
        ClassPresenterSelector ps = new ClassPresenterSelector();
        PlaybackControlsRowPresenter playbackControlsRowPresenter = new PlaybackControlsRowPresenter(new VideoDetailsDescriptionPresenter(getActivity()));
        ps.addClassPresenter(PlaybackControlsRow.class, playbackControlsRowPresenter);
        ps.addClassPresenter(ListRow.class, new ListRowPresenter());
        mRowsAdapter = new ArrayObjectAdapter(ps);
        addPlaybackControlsRow();
        playbackControlsRowPresenter.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.primary));
        playbackControlsRowPresenter.setProgressColor(Color.WHITE);
        mPlaybackControlsRow.setTotalTime(mDuration);
        playbackControlsRowPresenter.setOnActionClickedListener(new OnActionClickedListener() {
            @Override
            public void onActionClicked(Action action) {
                if (action.getId() == mPlayPauseAction.getId()) {
                    togglePlayPause(mPlayPauseAction.getIndex() == PlaybackControlsRow.PlayPauseAction.PAUSE);
                } else if (action.getId() == mRewindAction.getId()) {
                    rewind();
                } else if (action.getId() == mFastForwardAction.getId()) {
                    fastForward();
                }
                if (action instanceof PlaybackControlsRow.MultiAction) {
                    notifyChanged(action);
                }
            }
        });
        setAdapter(mRowsAdapter);
    }

    private void togglePlayPause(boolean isPaused) {
        if (isPaused) {
            mediaPlayer.pause();
        } else {
            mediaPlayer.play();
        }
        playbackStateChanged();
    }

    private void rewind() {
        if (mediaPlayer.getTime() - (10 * 1000) > 0) {
            mediaPlayer.setTime(mediaPlayer.getTime() - (10 * 1000));
            mPlaybackControlsRow.setCurrentTime((int) mediaPlayer.getTime());
        }
    }

    private void fastForward() {
        if (mediaPlayer.getTime() + (10 * 1000) < mDuration) {
            mediaPlayer.setTime(mediaPlayer.getTime() + (10 * 1000));
            mPlaybackControlsRow.setCurrentTime((int) mediaPlayer.getTime());
        }
    }

    private void addPlaybackControlsRow() {
        mPlaybackControlsRow = new PlaybackControlsRow(getVideoFile());
        mRowsAdapter.add(mPlaybackControlsRow);

        ControlButtonPresenterSelector presenterSelector = new ControlButtonPresenterSelector();
        mPrimaryActionsAdapter = new ArrayObjectAdapter(presenterSelector);
        mPlaybackControlsRow.setPrimaryActionsAdapter(mPrimaryActionsAdapter);

        mRewindAction = new PlaybackControlsRow.RewindAction(getActivity());
        mPlayPauseAction = new PlaybackControlsRow.PlayPauseAction(getActivity());
        mFastForwardAction = new PlaybackControlsRow.FastForwardAction(getActivity());
        mPrimaryActionsAdapter.add(mRewindAction);
        mPrimaryActionsAdapter.add(mPlayPauseAction);
        mPrimaryActionsAdapter.add(mFastForwardAction);
        playbackStateChanged();
    }

    public void playbackStateChanged() {
        if (mCurrentPlaybackState != PlaybackState.STATE_PLAYING) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mCurrentPlaybackState = PlaybackState.STATE_PLAYING;
            }
            startProgressAutomation();
            setFadingEnabled(true);
            mPlayPauseAction.setIndex(PlaybackControlsRow.PlayPauseAction.PAUSE);
            mPlayPauseAction.setIcon(mPlayPauseAction.getDrawable(PlaybackControlsRow.PlayPauseAction.PAUSE));
            notifyChanged(mPlayPauseAction);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mCurrentPlaybackState = PlaybackState.STATE_PAUSED;
            }
            stopProgressAutomation();
            setFadingEnabled(false);
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

    private ServerShare getVideoShare() {
        return getArguments().getParcelable(Intents.Extras.SERVER_SHARE);
    }

    private Uri getFileUri() {
        return serverClient.getFileUri(getVideoShare(), getVideoFile());
    }

    private ServerFile getVideoFile() {
        return getArguments().getParcelable(Intents.Extras.SERVER_FILE);
    }

    @Override
    public void onPause() {
        super.onPause();
        mediaPlayer.pause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mediaPlayer.play();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaPlayer.release();
        mediaPlayer = null;
    }
}