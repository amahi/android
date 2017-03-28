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

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.ViewAnimator;

/**
 * {@link android.widget.ViewAnimator} high-level operator.
 */
public final class ViewDirector {
    private final Activity activity;
    private final Fragment fragment;

    private final int animatorId;

    private ViewDirector(Activity activity, int animatorId) {
        this.activity = activity;
        this.fragment = null;

        this.animatorId = animatorId;
    }

    private ViewDirector(Fragment fragment, int animatorId) {
        this.activity = null;
        this.fragment = fragment;

        this.animatorId = animatorId;
    }

    public static ViewDirector of(Activity activity, int animatorId) {
        return new ViewDirector(activity, animatorId);
    }

    public static ViewDirector of(Fragment fragment, int animatorId) {
        return new ViewDirector(fragment, animatorId);
    }

    public void show(int viewId) {
        ViewAnimator animator = (ViewAnimator) findView(animatorId);
        View view = findView(viewId);

        if (animator.getDisplayedChild() != animator.indexOfChild(view)) {
            animator.setDisplayedChild(animator.indexOfChild(view));
        }
    }

    private View findView(int viewId) {
        if (activity != null) {
            return activity.findViewById(viewId);
        } else {
            return fragment.getView().findViewById(viewId);
        }
    }
}
