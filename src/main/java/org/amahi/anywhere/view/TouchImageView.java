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
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;

/**
 * Custom ImageView supporting touch gestures. Extends default ImageView class.
 * Enables double click to zoom in / zoom out image.
 * Enables dragging a zoomed in image to view it completely.
 */
public class TouchImageView extends AppCompatImageView {

    static final int CLICK = 3;
    protected float origWidth, origHeight;
    Matrix matrix;
    State mode = State.NONE;
    PointF last = new PointF();
    PointF start = new PointF();
    float minScale = 1f;
    float doubleClickScale = 2f;
    float maxScale = 3f;
    float[] m;
    int viewWidth, viewHeight;
    float saveScale = 1f;
    int oldMeasuredWidth, oldMeasuredHeight;
    ScaleGestureDetector mScaleDetector;
    Context context;
    private long DOUBLE_CLICK_INTERVAL = ViewConfiguration.getDoubleTapTimeout();
    private long thisTouchTime;
    private long previousTouchTime = 0;

    public TouchImageView(Context context) {
        super(context);
        sharedConstructing(context);
    }

    public TouchImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        sharedConstructing(context);
    }

    private void sharedConstructing(Context context) {
        super.setClickable(true);
        this.context = context;
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        matrix = new Matrix();
        m = new float[9];
        setImageMatrix(matrix);
        setScaleType(AppCompatImageView.ScaleType.MATRIX);

        setOnTouchListener((v, event) -> {
            mScaleDetector.onTouchEvent(event);
            PointF curr = new PointF(event.getX(), event.getY());

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    onSingleClick(curr);
                    thisTouchTime = System.currentTimeMillis();
                    if (thisTouchTime - previousTouchTime <= DOUBLE_CLICK_INTERVAL) {
                        onDoubleClick(curr);
                    }
                    previousTouchTime = thisTouchTime;
                    break;

                case MotionEvent.ACTION_MOVE:
                    if (mode == State.DRAG) {
                        float deltaX = curr.x - last.x;
                        float deltaY = curr.y - last.y;
                        if (deltaX != 0f || deltaY != 0f) {
                            float fixTransX = getFixDragTrans(deltaX, viewWidth,
                                origWidth * saveScale);
                            float fixTransY = getFixDragTrans(deltaY, viewHeight,
                                origHeight * saveScale);
                            if (saveScale > 1f) {
                                matrix.getValues(m);
                                float absTransX = Math.abs(m[Matrix.MTRANS_X]);
                                float transXMax = (origWidth * (saveScale - 1f));
                                if ((transXMax - absTransX < 0.5f && fixTransX < 0f)
                                    || (absTransX < 0.5f && fixTransX > 0f))
                                    getParent().requestDisallowInterceptTouchEvent(false);
                                else
                                    getParent().requestDisallowInterceptTouchEvent(true);
                            }
                            matrix.postTranslate(fixTransX, fixTransY);
                            fixTrans();
                            last.set(curr.x, curr.y);
                        }
                    }
                    break;

                case MotionEvent.ACTION_UP:
                    mode = State.NONE;
                    int xDiff = (int) Math.abs(curr.x - start.x);
                    int yDiff = (int) Math.abs(curr.y - start.y);
                    if (xDiff < CLICK && yDiff < CLICK)
                        performClick();
                    break;

                case MotionEvent.ACTION_POINTER_UP:
                    mode = State.NONE;
                    break;
            }

            setImageMatrix(matrix);
            invalidate();
            return true; // indicate event was handled
        });

    }

    private void onSingleClick(PointF curr) {
        last.set(curr);
        start.set(last);
        mode = State.DRAG;
    }

    private void onDoubleClick(PointF curr) {
        float mScaleFactor;
        if (saveScale < doubleClickScale) {
            mScaleFactor = doubleClickScale / saveScale;
            saveScale = doubleClickScale;
            matrix.postScale(mScaleFactor, mScaleFactor, curr.x, viewHeight / 2);
        } else {
            mScaleFactor = minScale / saveScale;
            saveScale = minScale;
            matrix.postScale(mScaleFactor, mScaleFactor, viewWidth / 2, viewHeight / 2);
        }
        fixTrans();
    }

    public void setMaxZoom(float x) {
        maxScale = x;
    }

    public void setDoubleClickZoom(float x) {
        doubleClickScale = x;
    }

    void fixTrans() {
        matrix.getValues(m);
        float transX = m[Matrix.MTRANS_X];
        float transY = m[Matrix.MTRANS_Y];

        float fixTransX = getFixTrans(transX, viewWidth, origWidth * saveScale);
        float fixTransY = getFixTrans(transY, viewHeight, origHeight * saveScale);

        if (fixTransX != 0 || fixTransY != 0)
            matrix.postTranslate(fixTransX, fixTransY);
    }

    float getFixTrans(float trans, float viewSize, float contentSize) {
        float minTrans, maxTrans;

        if (contentSize <= viewSize) {
            minTrans = 0;
            maxTrans = viewSize - contentSize;
        } else {
            minTrans = viewSize - contentSize;
            maxTrans = 0;
        }

        if (trans < minTrans)
            return -trans + minTrans;
        if (trans > maxTrans)
            return -trans + maxTrans;
        return 0;
    }

    float getFixDragTrans(float delta, float viewSize, float contentSize) {
        if (contentSize <= viewSize) {
            return 0f;
        }
        return delta;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        viewWidth = View.MeasureSpec.getSize(widthMeasureSpec);
        viewHeight = View.MeasureSpec.getSize(heightMeasureSpec);

        //
        // Rescales image on rotation
        //
        if (oldMeasuredHeight == viewWidth && oldMeasuredHeight == viewHeight
            || viewWidth == 0 || viewHeight == 0)
            return;
        oldMeasuredHeight = viewHeight;
        oldMeasuredWidth = viewWidth;

        if (saveScale == 1) {
            // Fit to screen.
            float scale;

            Drawable drawable = getDrawable();
            if (drawable == null || drawable.getIntrinsicWidth() == 0
                || drawable.getIntrinsicHeight() == 0)
                return;
            int bmWidth = drawable.getIntrinsicWidth();
            int bmHeight = drawable.getIntrinsicHeight();

            Log.d("bmSize", "bmWidth: " + bmWidth + " bmHeight : " + bmHeight);

            float scaleX = (float) viewWidth / (float) bmWidth;
            float scaleY = (float) viewHeight / (float) bmHeight;
            scale = Math.min(scaleX, scaleY);
            matrix.setScale(scale, scale);

            // Center the image
            float redundantYSpace = (float) viewHeight
                - (scale * (float) bmHeight);
            float redundantXSpace = (float) viewWidth
                - (scale * (float) bmWidth);
            redundantYSpace /= (float) 2;
            redundantXSpace /= (float) 2;

            matrix.postTranslate(redundantXSpace, redundantYSpace);

            origWidth = viewWidth - 2 * redundantXSpace;
            origHeight = viewHeight - 2 * redundantYSpace;
            setImageMatrix(matrix);
        }
        fixTrans();
    }

    private enum State {
        NONE,
        DRAG,
        ZOOM
    }

    private class ScaleListener extends
        ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            mode = State.ZOOM;
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float mScaleFactor = detector.getScaleFactor();
            float origScale = saveScale;
            saveScale *= mScaleFactor;
            if (saveScale > maxScale) {
                saveScale = maxScale;
                mScaleFactor = maxScale / origScale;
            } else if (saveScale < minScale) {
                saveScale = minScale;
                mScaleFactor = minScale / origScale;
            }

            if (origWidth * saveScale <= viewWidth
                || origHeight * saveScale <= viewHeight)
                matrix.postScale(mScaleFactor, mScaleFactor, viewWidth / 2,
                    viewHeight / 2);
            else
                matrix.postScale(mScaleFactor, mScaleFactor,
                    detector.getFocusX(), detector.getFocusY());

            fixTrans();
            return true;
        }
    }
}
