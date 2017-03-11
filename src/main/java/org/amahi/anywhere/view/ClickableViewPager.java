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


package org.amahi.anywhere.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;

/**
 * Custom ViewPager for implementing click events.
 */
public class ClickableViewPager extends ViewPager {

    private OnClickListener mOnClickListener;
    private GestureDetector tapGestureDetector;

    public ClickableViewPager(Context context) {
        super(context);

        setup();
    }

    public ClickableViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);

        setup();
    }

    private void setup() {
        tapGestureDetector = new GestureDetector(getContext(), new TapGestureListener());
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        tapGestureDetector.onTouchEvent(ev);
        return super.onInterceptTouchEvent(ev);
    }

    public void setOnViewPagerClickListener(OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
    }

    public interface OnClickListener {
        void onViewPagerClick(ViewPager viewPager);
    }

    private class TapGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (mOnClickListener != null) {
                mOnClickListener.onViewPagerClick(ClickableViewPager.this);
            }
            return true;
        }
    }
}
