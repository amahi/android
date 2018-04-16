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

package org.amahi.anywhere.util;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.view.View;

/**
 * FullScreenHelper Helper class.
 * Shows content as full screen and implements touch listener
 * to control the show/hide of controls and ActionBar.
 */

public class FullScreenHelper {

    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private boolean autoHide = true;
    private long autoHideDelayMillis = 3000;
    private boolean onClickToggleEnabled = true;
    private boolean mVisible;
    private View mControlsView;
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private ActionBar actionBar;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            if (actionBar != null)
                actionBar.show();
            if (mControlsView != null)
                mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private final Runnable mHideRunnable = this::hide;

    public FullScreenHelper(ActionBar actionBar, @NonNull View contentView) {
        setActionBar(actionBar);
        setmContentView(contentView);
    }

    public FullScreenHelper(ActionBar actionBar, @NonNull View contentView, View controlsView) {
        setActionBar(actionBar);
        setmContentView(contentView);
        setmControlsView(controlsView);
    }

    public void setmControlsView(View mControlsView) {
        this.mControlsView = mControlsView;
    }

    public void setmContentView(View mContentView) {
        this.mContentView = mContentView;
    }

    public void setActionBar(ActionBar actionBar) {
        this.actionBar = actionBar;
    }

    public void setAutoHide(Boolean autoHide) {
        this.autoHide = autoHide;
    }

    public void enableOnClickToggle(Boolean enable) {
        this.onClickToggleEnabled = enable;
    }

    public void init() {
        mVisible = true;

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(autoHideDelayMillis);

        if (onClickToggleEnabled) {
            // Toggle hide/show on click
            mContentView.setOnClickListener(view -> toggle());
        }

        if (mControlsView != null) {
            // Upon interacting with UI controls, delay any scheduled hide()
            // operations to prevent the jarring behavior of controls going away
            // while interacting with the UI.
            mControlsView.setOnTouchListener((view, motionEvent) -> {
                if (autoHide) {
                    delayedHide();
                }
                return false;
            });
        }
    }

    public void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
            delayedHide();
        }
    }

    public void hide() {
        if (actionBar != null)
            actionBar.hide();
        if (mControlsView != null)
            mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    public void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    public void delayedHide(long delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    public void delayedHide() {
        delayedHide(autoHideDelayMillis);
    }

    public long getAutoHideDelayMillis() {
        return autoHideDelayMillis;
    }

    public void setAutoHideDelayMillis(int millis) {
        this.autoHideDelayMillis = millis;
    }
}
