package org.amahi.anywhere.util;

/*
 * Copyright 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Context;

import androidx.core.view.ViewCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.View;

/**
 * A descendant of {@link androidx.swiperefreshlayout.widget.SwipeRefreshLayout} which supports multiple
 * child views triggering a refresh gesture. You set the views which can trigger the gesture via
 * {@link #setSwipeableChildren(int...)}, providing it the child ids.
 */
public class MultiSwipeRefreshLayout extends SwipeRefreshLayout {

    private View[] mSwipeableChildren;

    public MultiSwipeRefreshLayout(Context context) {
        super(context);
    }

    public MultiSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Set the children which can trigger a refresh by swiping down when they are visible. These
     * views need to be a descendant of this view.
     */
    public void setSwipeableChildren(final int... ids) {
        assert ids != null;

        // Iterate through the ids and find the Views
        mSwipeableChildren = new View[ids.length];
        for (int i = 0; i < ids.length; i++) {
            mSwipeableChildren[i] = findViewById(ids[i]);
        }
    }

    /**
     * This method controls when the swipe-to-refresh gesture is triggered. By returning false here
     * we are signifying that the view is in a state where a refresh gesture can start.
     *
     * <p>As {@link androidx.swiperefreshlayout.widget.SwipeRefreshLayout} only supports one direct child by
     * default, we need to manually iterate through our swipeable children to see if any are in a
     * state to trigger the gesture. If so we return false to start the gesture.
     */
    @Override
    public boolean canChildScrollUp() {
        if (mSwipeableChildren != null && mSwipeableChildren.length > 0) {
            // Iterate through the scrollable children and check if any of them can not scroll up
            for (View view : mSwipeableChildren) {
                if (view != null && view.isShown() && !canViewScrollUp(view)) {
                    // If the view is shown, and can not scroll upwards, return false and start the
                    // gesture.
                    return false;
                }
            }
        }
        return true;
    }
    /**
     * Utility method to check whether a {@link View} can scroll up from it's current position.
     * Handles platform version differences, providing backwards compatible functionality where
     * needed.
     */
    private static boolean canViewScrollUp(View view) {
        return ViewCompat.canScrollVertically(view, -1);
    }
}
