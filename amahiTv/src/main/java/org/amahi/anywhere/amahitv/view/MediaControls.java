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

package org.amahi.anywhere.amahitv.view;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.MediaController;

import org.amahi.anywhere.amahitv.R;

import java.util.concurrent.TimeUnit;

/**
 * Media controls view. Does the same as {@link MediaController}
 * with a couple of modifications. Media controls do not auto-hide, back button do not hide
 * controls but finishes parent {@link Activity}, there are methods to show and hide
 * controls animated.
 */
public class MediaControls extends MediaController implements Animation.AnimationListener {
    private Handler videoControlsHandler;
    private boolean isVisible = false;
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hideAnimated();
        }
    };
    private long autoHideDelayMillis = TimeUnit.SECONDS.toMillis(3);

    public MediaControls(Context context) {
        super(context);
        init();
    }

    public MediaControls(Context context, long autoHideDelayMillis) {
        super(context);
        this.autoHideDelayMillis = autoHideDelayMillis;
        init();
    }

    private void init() {
        videoControlsHandler = new Handler();
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                hideControlsDelayed();
            }
        });
    }

    @Override
    public void show(int timeout) {
        super.show(0);
    }

    public void showAnimated() {
        if (!isVisible) {
            videoControlsHandler.removeCallbacks(mHideRunnable);
            Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.slide_up_view);
            startAnimation(animation);
            show();
            isVisible = true;
        }
    }

    public void hideAnimated() {
        if (isVisible) {
            Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.slide_down_view);
            animation.setAnimationListener(this);
            startAnimation(animation);
        }
    }

    public void hideControlsDelayed() {
        videoControlsHandler.removeCallbacks(mHideRunnable);
        videoControlsHandler.postDelayed(mHideRunnable, autoHideDelayMillis);
    }

    public void toggle() {
        if (isVisible) {
            hideAnimated();
        } else {
            showAnimated();
            hideControlsDelayed();
        }
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        hide();
        isVisible = false;
    }

    @Override
    public void onAnimationStart(Animation animation) {
    }

    @Override
    public void onAnimationRepeat(Animation animation) {
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if ((event.getKeyCode() == KeyEvent.KEYCODE_BACK) && (event.getAction() == KeyEvent.ACTION_DOWN)) {
            Activity activity = (Activity) getContext();
            activity.finish();

            return true;
        }

        return super.dispatchKeyEvent(event);
    }
}
