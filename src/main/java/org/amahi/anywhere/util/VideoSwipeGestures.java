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
import android.media.AudioManager;
import android.os.Handler;
import android.support.v4.view.MotionEventCompat;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;

import org.amahi.anywhere.view.PercentageView;
import org.amahi.anywhere.view.SeekView;

import static android.view.MotionEvent.INVALID_POINTER_ID;

/**
 * SwipeGestures Helper class.
 * Implements touch listener to control the actions of Swipe Gestures.
 */

public class VideoSwipeGestures implements View.OnTouchListener {

    private int mActivePointerId = INVALID_POINTER_ID;
    private AudioManager audio;
    private PercentageView percentageView;
    private SeekView seekView;
    private int currentVolume;
    private int numberOfTaps = 0;
    private long lastTapTimeMs = 0;
    private long touchDownMs = 0;
    private double volper;
    private double per;
    private float brightness;
    private float seekDistance = 0;
    private float distanceCovered = 0;
    private boolean checkBrightness = true;
    private boolean checkVolume = true;
    private boolean checkSeek = true;
    private String onHorizontal;
    private String onVertical;
    private String onCircular;
    private Activity activity;
    public static String color = "#FB5B0A";

    public enum Orientation {
        HORIZONTAL, VERTICAL, CIRCULAR
    }

    @Override
    public boolean onTouch(View v, MotionEvent ev) {
        final int action = MotionEventCompat.getActionMasked(ev);

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                seekDistance = 0;
                distanceCovered = 0;
                touchDownMs = System.currentTimeMillis();
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                final float x = ev.getX();
                final float y = ev.getY();
                distanceCovered = getDistance(x, y, ev);
                try {
                    changeBrightness(ev.getHistoricalX(0, 0), ev.getHistoricalY(0, 0), x, y, distanceCovered, "Y");
                    changeSeek(ev.getHistoricalX(0, 0), ev.getHistoricalY(0, 0), x, y, distanceCovered, "X");
                } catch (IllegalArgumentException | IndexOutOfBoundsException ignored) {

                }

                break;
            }

            case MotionEvent.ACTION_UP: {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (percentageView.isVisible())
                            percentageView.hide();
                        if (seekView.isVisible())
                            seekView.hide();
                    }
                }, 2000);

                if ((System.currentTimeMillis() - touchDownMs) > ViewConfiguration.getTapTimeout()) {
                    numberOfTaps = 0;
                    lastTapTimeMs = 0;
                    break;
                }

                if (numberOfTaps > 0 && (System.currentTimeMillis() - lastTapTimeMs) < ViewConfiguration.getDoubleTapTimeout()) {
                    numberOfTaps += 1;
                } else {
                    numberOfTaps = 1;
                }

                lastTapTimeMs = System.currentTimeMillis();
                mActivePointerId = INVALID_POINTER_ID;
                break;
            }
            case MotionEvent.ACTION_CANCEL: {
                mActivePointerId = INVALID_POINTER_ID;
                break;
            }
            case MotionEvent.ACTION_POINTER_UP: {

                final int pointerIndex = MotionEventCompat.getActionIndex(ev);
                final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);

                if (pointerId == mActivePointerId) {
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    mActivePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex);
                }
                break;
            }
        }
        return true;
    }

    private float getDistance(float startX, float startY, MotionEvent ev) {
        float distanceSum = 0;
        final int historySize = ev.getHistorySize();
        for (int h = 0; h < historySize; h++) {
            float hx = ev.getHistoricalX(0, h);
            float hy = ev.getHistoricalY(0, h);
            float dx = (hx - startX);
            float dy = (hy - startY);
            distanceSum += Math.sqrt(dx * dx + dy * dy);
            startX = hx;
            startY = hy;
        }
        float dx = (ev.getX(0) - startX);
        float dy = (ev.getY(0) - startY);
        distanceSum += Math.sqrt(dx * dx + dy * dy);
        return distanceSum;
    }

    private void changeVolume(float X, float Y, float x, float y, float distance, String type) {
        percentageView.setTitle("Volume");
        seekView.hide();
        if (type.equals("Y") && x == X) {
            if (y < Y) {
                distance = distance / 100;
                commonVolume(distance);
            } else {
                distance = distance / 150;
                commonVolume(-distance);
            }
        } else if (type.equals("X") && y == Y) {
            if (x > X) {
                distance = distance / 200;
                commonVolume(distance);
            } else {
                distance = distance / 250;
                commonVolume(-distance);
            }
        }
    }

    private void commonVolume(float distance) {
        currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
        per = (double) currentVolume / (double) maxVolume;
        if (per + distance <= 1 && per + distance >= 0) {
            percentageView.show();
            if (Math.abs(distance) > 0.05) {
                percentageView.setProgress((int) ((per + distance) * 100));
                percentageView.setProgressText((int) ((per + distance) * 100) + "%");
                volper = (per + (double) distance);
                audio.setStreamVolume(AudioManager.STREAM_MUSIC, (int) (volper * 15), 0);
            }
        }
    }

    private void changeBrightness(float X, float Y, float x, float y, float distance, String type) {
        percentageView.setTitle("Brightness");
        seekView.hide();
        if (type.equals("Y") && x == X) {
            distance = distance / 270;
            if (y < Y) {
                commonBrightness(distance);
            } else {
                commonBrightness(-distance);
            }
        } else if (type.equals("X") && y == Y) {
            distance = distance / 160;
            if (x > X) {
                commonBrightness(distance);
            } else {
                commonBrightness(-distance);
            }
        }
    }

    private void commonBrightness(float distance) {
        WindowManager.LayoutParams layout = activity.getWindow().getAttributes();
        if (+distance <= 1
                && activity.getWindow().getAttributes().screenBrightness + distance >= 0) {
            percentageView.show();
            if ((int) ((activity.getWindow().getAttributes().screenBrightness + distance) * 100) > 100) {
                percentageView.setProgress(100);
                percentageView.setProgressText("100");
            } else if ((int) ((activity.getWindow().getAttributes().screenBrightness + distance) * 100) < 0) {
                percentageView.setProgress(0);
                percentageView.setProgressText("0");
            } else {
                percentageView.setProgress((int) ((activity.getWindow().getAttributes().screenBrightness + distance) * 100));
                percentageView.setProgressText(Integer.valueOf((int) ((activity.getWindow().getAttributes().screenBrightness + distance) * 100)).toString() + "%");
            }
            layout.screenBrightness = activity.getWindow().getAttributes().screenBrightness + distance;
            activity.getWindow().setAttributes(layout);
        }
    }

    private void changeSeek(float X, float Y, float x, float y, float distance, String type) {
        if (type.equals("X") && y == Y) {
            distance = distance / 200;
            if (x > X) {
                seekCommon(distance);
            } else {
                seekCommon(-distance);
            }
        }
    }

    private void seekCommon(float distance) {
        seekDistance += distance * 60000;
        seekView.show();
//        if (mediaPlayer != null) {
////            Log.e("after", mediaPlayer.getCurrentPosition() + (int) (distance * 60000) + "");
////            Log.e("seek distance", (int) (seekDistance) + "");
//            if (mediaPlayer.getCurrentPosition() + (int) (distance * 60000) > 0 && mediaPlayer.getCurrentPosition() + (int) (distance * 60000) < mediaPlayer.getDuration() + 10) {
//                mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() + (int) (distance * 60000));
//                if (seekDistance > 0)
//                    seekView.setText("+" + Math.abs((int) (seekDistance / 60000)) + ":" + String.valueOf(Math.abs((int) ((seekDistance) % 60000))).substring(0, 2) + "(" + (int) ((mediaPlayer.getCurrentPosition() + (int) (distance * 60000)) / 60000) + ":" + String.valueOf((int) ((mediaPlayer.getCurrentPosition() + (int) (distance * 60000)) % 60000)).substring(0, 2) + ")");
//                else
//                    seekView.setText("-" + Math.abs((int) (seekDistance / 60000)) + ":" + String.valueOf(Math.abs((int) ((seekDistance) % 60000))).substring(0, 2) + "(" + (int) ((mediaPlayer.getCurrentPosition() + (int) (distance * 60000)) / 60000) + ":" + String.valueOf((int) ((mediaPlayer.getCurrentPosition() + (int) (distance * 60000)) % 60000)).substring(0, 2) + ")");
//            }
//        } else if (videoView != null) {
////            Log.e("after", videoView.getCurrentPosition() + (int) (distance * 60000) + "");
////            Log.e("seek distance", (int) (seekDistance) + "");
//            if (videoView.getCurrentPosition() + (int) (distance * 60000) > 0 && videoView.getCurrentPosition() + (int) (distance * 60000) < videoView.getDuration() + 10) {
//                videoView.seekTo(videoView.getCurrentPosition() + (int) (distance * 60000));
//                if (seekDistance > 0)
//                    seekView.setText("+" + Math.abs((int) (seekDistance / 60000)) + ":" + String.valueOf(Math.abs((int) ((seekDistance) % 60000))).substring(0, 2) + "(" + (int) ((videoView.getCurrentPosition() + (int) (distance * 60000)) / 60000) + ":" + String.valueOf((int) ((videoView.getCurrentPosition() + (int) (distance * 60000)) % 60000)).substring(0, 2) + ")");
//                else
//                    seekView.setText("-" + Math.abs((int) (seekDistance / 60000)) + ":" + String.valueOf(Math.abs((int) ((seekDistance) % 60000))).substring(0, 2) + "(" + (int) ((videoView.getCurrentPosition() + (int) (distance * 60000)) / 60000) + ":" + String.valueOf((int) ((videoView.getCurrentPosition() + (int) (distance * 60000)) % 60000)).substring(0, 2) + ")");
//
//            }
    }
}
