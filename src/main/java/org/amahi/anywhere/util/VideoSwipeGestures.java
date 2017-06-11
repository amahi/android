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
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import org.amahi.anywhere.view.PercentageView;
import org.amahi.anywhere.view.SeekView;

/**
 * SwipeGestures Helper class.
 * Implements Gesture Detector to control the actions of Swipe Gestures.
 */

public class VideoSwipeGestures implements View.OnTouchListener {

    private Activity activity;
    private PercentageView percentageView;
    private SeekView seekView;


    private enum Direction {
        LEFT, RIGHT, UP, DOWN, NONE
    }

    private GestureDetector gestureDetector;

    public VideoSwipeGestures(Activity activity) {
        this.activity = activity;
        this.gestureDetector = new GestureDetector(activity, new CustomGestureDetect());
        this.gestureDetector.setIsLongpressEnabled(false);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (gestureDetector != null) {
            gestureDetector.onTouchEvent(event);
        }
        return false;
    }

    private class CustomGestureDetect implements GestureDetector.OnGestureListener {

        private Direction direction = Direction.NONE;
        private Direction xPosition;

        @Override
        public boolean onDown(MotionEvent e) {
            direction = Direction.NONE;
            DisplayMetrics displayMetrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int width = displayMetrics.widthPixels;
            if (e.getX() >= width / 2)
                xPosition = Direction.RIGHT;
            else
                xPosition = Direction.LEFT;
            return true;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        /* Check if this is the first ACTION_MOVE event in the current touch event
        * If true then set the initial direction of the movement
        * */
            try {
                if (direction == Direction.NONE) {
                    float diffY = e2.getY() - e1.getY();
                    float diffX = e2.getX() - e1.getX();
                    if (Math.abs(diffX) > Math.abs(diffY)) {
                        if (diffX > 0) {
                            direction = Direction.RIGHT;
                        } else {
                            direction = Direction.LEFT;
                        }

                    } else {
                        if (diffY > 0) {
                            direction = Direction.DOWN;
                        } else {
                            direction = Direction.UP;
                        }
                    }
                }
            } catch (NullPointerException ignored) {
                ignored.printStackTrace();
            }
            Log.e("ACTION_MOVE", String.format("(%f,%f) %s", distanceX, distanceY, direction.name()));

            switch (direction) {
                case UP:
                case DOWN:
                    switch (xPosition) {
                        case LEFT:
                            Log.d("TOUCH", "UP_DOWN_LEFT");
                            changeBrightness(distanceY);
                            break;
                        case RIGHT:
                            Log.d("TOUCH", "UP_DOWN_RIGHT");
                            changeBrightness(distanceY);
                            break;
                    }
                    break;
                case LEFT:
                case RIGHT:


                    break;
                case NONE:
                    break;
            }
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {

        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }

    }


    public void changeBrightness(float distance) {
        WindowManager.LayoutParams layout = activity.getWindow().getAttributes();
        layout.screenBrightness += distance;
        Log.d("Brightness", String.valueOf(layout.screenBrightness));
        if (distance <= 1 && layout.screenBrightness >= 0) {
            if ((int) (layout.screenBrightness * 100) > 100) {
//                customView.setProgress(100);
//                customView.setProgressText("100");
            } else if ((int) (layout.screenBrightness * 100) < 0) {
//                customView.setProgress(0);
//                customView.setProgressText("0");
            } else {
//                customView.setProgress((int) ((activity.getWindow().getAttributes().screenBrightness + distance) * 100));
//                customView.setProgressText(Integer.valueOf((int) ((activity.getWindow().getAttributes().screenBrightness + distance) * 100)).toString() + "%");
            }
            activity.getWindow().setAttributes(layout);
        }
    }

}
