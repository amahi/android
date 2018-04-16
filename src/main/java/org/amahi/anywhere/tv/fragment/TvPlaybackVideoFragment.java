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
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
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
import org.amahi.anywhere.tv.presenter.MainTVPresenter;
import org.amahi.anywhere.tv.presenter.VideoDetailsDescriptionPresenter;
import org.amahi.anywhere.util.Fragments;
import org.amahi.anywhere.util.Intents;
import org.amahi.anywhere.util.Mimes;
import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.util.ArrayList;

import javax.inject.Inject;

import wseemann.media.FFmpegMediaMetadataRetriever;


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
    private Object mSavedState;
    private PlaybackControlsRow mPlaybackControlsRow;
    private int mCurrentPlaybackState;
    private int mDuration;
    private ArrayList<ServerFile> mVideoList;
    private ArrayObjectAdapter mRowsAdapter;
    private ArrayObjectAdapter mPrimaryActionsAdapter;
    private PlaybackControlsRow.SkipPreviousAction mSkipPreviousAction;
    private PlaybackControlsRow.PlayPauseAction mPlayPauseAction;
    private PlaybackControlsRow.FastForwardAction mFastForwardAction;
    private PlaybackControlsRow.RewindAction mRewindAction;
    private PlaybackControlsRow.SkipNextAction mSkipNextAction;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpInjections();

        setAllVideoFiles();

        setDuration();

        playVideo();

        setBackgroundType(BG_NONE);

        setFadingEnabled(false);

        mHandler = new Handler(Looper.getMainLooper());

        setUpRows();

        addOtherRows();

        mediaPlayer.setEventListener(event -> {
            if (event.type == MediaPlayer.Event.EndReached) {
                skipNext();
            }
        });

        setOnItemViewClickedListener((OnItemViewClickedListener) (itemViewHolder, item, rowViewHolder, row) -> {
            if (item instanceof ServerFile) {
                ServerFile serverFile = (ServerFile) item;
                replaceFragment(serverFile);
            }
        });

        setOnItemViewSelectedListener((OnItemViewSelectedListener) (itemViewHolder, item, rowViewHolder, row) -> mSavedState = item);
    }

    private ArrayList<ServerFile> setAllVideoFiles() {
        mVideoList = new ArrayList<>();
        ArrayList<ServerFile> allFiles = getArguments().getParcelableArrayList(Intents.Extras.SERVER_FILES);
        if (allFiles != null) {
            for (ServerFile serverFile : allFiles) {
                if (isVideo(serverFile)) {
                    mVideoList.add(serverFile);
                }
            }
        }
        return mVideoList;
    }

    private void replaceFragment(ServerFile serverFile) {
        getFragmentManager().beginTransaction().replace(R.id.playback_controls_fragment_container, Fragments.Builder.buildVideoFragment(serverFile, getVideoShare(), getVideoFiles())).commit();
    }

    private boolean isVideo(ServerFile serverFile) {
        return Mimes.match(serverFile.getMime()) == Mimes.Type.VIDEO;
    }

    private boolean isMetadataAvailable() {
        return ServerShare.Tag.MOVIES.equals(getVideoShare().getTag());
    }

    private void setDuration() {
        FFmpegMediaMetadataRetriever mFFmpegMediaMetadataRetriever = new FFmpegMediaMetadataRetriever();
        mFFmpegMediaMetadataRetriever.setDataSource(getFileUri().toString());
        String mVideoDuration = mFFmpegMediaMetadataRetriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_DURATION);
        mDuration = Integer.parseInt(mVideoDuration);
    }

    private void playVideo() {
        SurfaceView mSurfaceView = getActivity().findViewById(R.id.surfaceView);
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
        playbackControlsRowPresenter.setOnActionClickedListener(action -> {
            if (action.getId() == mPlayPauseAction.getId()) {
                togglePlayPause(mPlayPauseAction.getIndex() == PlaybackControlsRow.PlayPauseAction.PAUSE);
            } else if (action.getId() == mRewindAction.getId()) {
                setFadingEnabled(false);
                rewind();
            } else if (action.getId() == mFastForwardAction.getId()) {
                setFadingEnabled(false);
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

    private void addOtherRows() {
        ArrayObjectAdapter adapter = new ArrayObjectAdapter(new MainTVPresenter(getActivity(), serverClient, getVideoShare()));

        for (ServerFile serverFile : mVideoList) adapter.add(serverFile);

        mRowsAdapter.add(new ListRow(getHeader(), adapter));
    }

    private HeaderItem getHeader() {
        HeaderItem headerItem;

        if (getVideoFile().getParentFile() == null)
            headerItem = new HeaderItem("Video(s) in " + getVideoShare().getName());
        else
            headerItem = new HeaderItem("Video(s) in " + getVideoFile().getParentFile().getName());

        return headerItem;
    }

    public void togglePlayPause(boolean isPaused) {
        if (isPaused) {
            mediaPlayer.pause();
        } else {
            mediaPlayer.play();
        }
        playbackStateChanged();
    }

    public void rewind() {
        if (mPlaybackControlsRow.getCurrentTime() - (10 * 1000) > 0) {
            mediaPlayer.setTime(mPlaybackControlsRow.getCurrentTime() - (10 * 1000));
            mPlaybackControlsRow.setCurrentTime((int) mediaPlayer.getTime());
        }
        setFadingEnabled(true);
    }

    public void fastForward() {
        if (mPlaybackControlsRow.getCurrentTime() + (10 * 1000) < mDuration) {
            mediaPlayer.setTime(mPlaybackControlsRow.getCurrentTime() + (10 * 1000));
            mPlaybackControlsRow.setCurrentTime((int) mediaPlayer.getTime());
        }
        setFadingEnabled(true);
    }

    public void skipPrevious() {
        int presentIndex = mVideoList.indexOf(getVideoFile());
        if (presentIndex < mVideoList.size() - 1)
            replaceFragment(mVideoList.get(presentIndex + 1));
        else
            replaceFragment(mVideoList.get(0));
    }

    public void skipNext() {
        int presentIndex = mVideoList.indexOf(getVideoFile());
        if (presentIndex > 0)
            replaceFragment(mVideoList.get(presentIndex - 1));
        else
            replaceFragment(mVideoList.get(mVideoList.size() - 1));
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
        mSkipNextAction = new PlaybackControlsRow.SkipNextAction(getActivity());
        mSkipPreviousAction = new PlaybackControlsRow.SkipPreviousAction(getActivity());
        if (!isMetadataAvailable())
            mPrimaryActionsAdapter.add(mSkipPreviousAction);
        mPrimaryActionsAdapter.add(mRewindAction);
        mPrimaryActionsAdapter.add(mPlayPauseAction);
        mPrimaryActionsAdapter.add(mFastForwardAction);
        if (!isMetadataAvailable())
            mPrimaryActionsAdapter.add(mSkipNextAction);
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

    private ArrayList<ServerFile> getVideoFiles() {
        return getArguments().getParcelableArrayList(Intents.Extras.SERVER_FILES);
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
        mLibVlc.release();
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
    }

    public PlaybackControlsRow.PlayPauseAction getmPlayPauseAction() {
        return mPlayPauseAction;
    }

    public Object getmSavedState() {
        return mSavedState;
    }
}
