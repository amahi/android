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
import android.content.Context;
import android.media.AudioManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import org.amahi.anywhere.view.PercentageView;
import org.amahi.anywhere.view.SeekView;

/**
 * SwipeGestures Helper class.
 * Implements Gesture Detector to control the actions of Swipe Gestures.
 */

public class VideoSwipeGestures implements View.OnTouchListener {

    private final AudioManager audio;
    private int currentVolume;
    private final int maxVolume;
    private Activity activity;
    private PercentageView percentageView;
    private SeekView seekView;
    private float seekDistance = 0;
    private SeekControl seekControl;


    public interface SeekControl {
        int getCurrentPosition();

        void seekTo(int time);

        int getDuration();
    }

    private enum Direction {
        LEFT, RIGHT, UP, DOWN, NONE
    }

    private GestureDetector gestureDetector;

    public VideoSwipeGestures(Activity activity, SeekControl seekControl, FrameLayout container) {
        this.activity = activity;
        this.seekControl = seekControl;
        this.gestureDetector = new GestureDetector(activity, new CustomGestureDetect());
        this.gestureDetector.setIsLongpressEnabled(false);
        percentageView = new PercentageView(container);
        container.addView(percentageView.getView());
        seekView = new SeekView(container);
        container.addView(seekView.getView());
        audio = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
        currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
        maxVolume = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (gestureDetector != null) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                percentageView.hide();
                seekView.hide();
            }
            return gestureDetector.onTouchEvent(event);
        }
        return false;
    }

    private class CustomGestureDetect implements GestureDetector.OnGestureListener {

        private Direction direction = Direction.NONE;
        private Direction xPosition;

        @Override
        public boolean onDown(MotionEvent e) {
            seekDistance = 0;
            direction = Direction.NONE;
            DisplayMetrics displayMetrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int width = displayMetrics.widthPixels;
            if (e.getX() >= width / 2) {
                xPosition = Direction.RIGHT;
            } else {
                xPosition = Direction.LEFT;
            }
            return false;
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
                        seekView.show();
                    } else {
                        if (xPosition == Direction.LEFT) {
                            percentageView.setType(PercentageView.BRIGHTNESS);
                        } else if (xPosition == Direction.RIGHT) {
                            percentageView.setType(PercentageView.VOLUME);
                        }
                        if (diffY > 0) {
                            direction = Direction.DOWN;
                        } else {
                            direction = Direction.UP;
                        }
                        percentageView.show();
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
                            changeBrightness(distanceY / 270);
                            break;
                        case RIGHT:
                            Log.d("TOUCH", "UP_DOWN_RIGHT");
                            changeVolume(distanceY / 250);
                            break;
                    }
                    break;
                case LEFT:
                case RIGHT:
                    seek(-distanceX * 200);
                    break;
                case NONE:
                    break;
            }
            if (e2.getAction() == MotionEvent.ACTION_UP) {
                percentageView.hide();
                seekView.hide();
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


    private void changeBrightness(float distance) {
        WindowManager.LayoutParams layout = activity.getWindow().getAttributes();
        layout.screenBrightness += distance;
        Log.d("Brightness", String.valueOf(layout.screenBrightness));
        if (layout.screenBrightness <= 1 && layout.screenBrightness >= 0) {
            percentageView.setProgress((int) (layout.screenBrightness * 100));
            activity.getWindow().setAttributes(layout);
        }
    }

    private void changeVolume(float distance) {
        currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
        double per = (double) currentVolume / (double) maxVolume;
        Log.e("audio per", String.valueOf(per));
        double val = per + distance;
        if (val <= 1d && val >= 0d) {
            percentageView.setProgress((int) (val * 100));
            audio.setStreamVolume(AudioManager.STREAM_MUSIC, (int) (val * 15), 0);

        }
    }

    private void seek(float distance) {
        seekDistance += distance;
        if (seekControl != null && seekView != null) {
            Log.e("after", String.valueOf(seekControl.getCurrentPosition() + (int) (distance)));
            Log.e("seek distance", String.valueOf(seekDistance));
            float seekValue = seekControl.getCurrentPosition() + distance;
            if (seekValue > 0 && seekValue < seekControl.getDuration()) {
                seekControl.seekTo((int) seekValue);
                String displayText = Math.abs((int) (seekDistance)) + ":" +
                        String.valueOf(Math.abs((int) (seekDistance % 1))) +
                        "(" + (int) seekValue +
                        ":" + String.valueOf((int) (seekValue % 1)) + ")";
                if (seekDistance > 0)
                    seekView.setText("+" + displayText);
                else
                    seekView.setText("-" + displayText);
            }
        }
    }

}
