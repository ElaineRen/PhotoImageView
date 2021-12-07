/*
 Copyright 2011, 2012 Chris Banes.
 <p/>
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 <p/>
 http://www.apache.org/licenses/LICENSE-2.0
 <p/>
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package com.haokan.mytestimageview.custom;

import android.content.Context;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;

import com.haokan.mytestimageview.custom.listener.OnGestureListener;
import com.haokan.mytestimageview.utils.Util;

/**
 * Does a whole lot of gesture detecting.
 */
class CustomGestureDetector {

    private static final int INVALID_POINTER_ID = -1;
    private String TAG = "CustomGestureDetector ";

    private int mActivePointerId = INVALID_POINTER_ID;
    private int mActivePointerIndex = 0;
    private final ScaleGestureDetector mDetector;

    private VelocityTracker mVelocityTracker;//速度跟踪，getXVelocity()和getYVelocity() 获取横向和竖向的速率
    private boolean mIsDragging;
    private float mLastTouchX;
    private float mLastTouchY;
    private final float mTouchSlop;
    private final float mMinimumVelocity;
    private final OnGestureListener mListener;

    // new add
    private boolean mZooming;

    CustomGestureDetector(Context context, OnGestureListener listener) {
        final ViewConfiguration configuration = ViewConfiguration
                .get(context);
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mTouchSlop = configuration.getScaledTouchSlop();

        mListener = listener;
        ScaleGestureDetector.OnScaleGestureListener mScaleListener = new ScaleGestureDetector.OnScaleGestureListener() {

            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                mZooming = true;
                float scaleFactor = detector.getScaleFactor();
                Log.d("onDrag", "ScaleGestureDetector onScale  1111111111 detector.getFocusX(): " +
                        "" + detector.getFocusX() + ",detector.getFocusY():" + detector.getFocusY());

                if (Float.isNaN(scaleFactor) || Float.isInfinite(scaleFactor))
                    return false;

                if (scaleFactor >= 0) {

                    Log.d("onDrag", "ScaleGestureDetector onScale  1111111111-2222222222 detector.getFocusX(): " +
                            "" + detector.getFocusX() + ",detector.getFocusY():" + detector.getFocusY());
                    mListener.onScale(scaleFactor,
                            detector.getFocusX(), detector.getFocusY());
                }
                return true;
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                // TODO: 11/23/20 wanggaowan 如果当前正在拖拽则不允许缩放
                return !mIsDragging;
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {
                // NO-OP
                //+rmr
                //mZooming = false;
                Log.d("onDrag", "ScaleGestureDetector onScaleEnd  1111111111 detector.getFocusX(): " +
                        "" + detector.getFocusX() + ",detector.getFocusY():" + detector.getFocusY());

            }
        };
        mDetector = new ScaleGestureDetector(context, mScaleListener);

    }

    private float getActiveX(MotionEvent ev) {
        try {
            return ev.getX(mActivePointerIndex);
        } catch (Exception e) {
            return ev.getX();
        }
    }

    private float getActiveY(MotionEvent ev) {
        try {
            return ev.getY(mActivePointerIndex);
        } catch (Exception e) {
            return ev.getY();
        }
    }

    public boolean isScaling() {
        return mDetector.isInProgress() || mZooming;
    }

    public boolean isDragging() {
        return mIsDragging;
    }

    /**
     * 传递 motionEvent 事件
     *
     * @param ev
     * @return
     */
    public boolean onTouchEvent(MotionEvent ev) {
        try {
            mDetector.onTouchEvent(ev);
            return processTouchEvent(ev);
        } catch (IllegalArgumentException e) {
            // Fix for support lib bug, happening when onDestroy is called
            return true;
        }
    }

    /**
     * 处理各种手指触摸事件
     *
     * @param ev
     * @return
     */
    //是否是 双指放大后的状态 ，
    //因为mActivePointerId 切换getPointerId以后， mLastTouchX，mLastTouchY的值获取会有失真;
    //放大后的
    private boolean isAfterZooming;
    private float mLastZoomingTouchX;
    private float mLastZoomingTouchY;

    private boolean processTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = ev.getPointerId(0);

                mVelocityTracker = VelocityTracker.obtain();
                if (null != mVelocityTracker) {
                    mVelocityTracker.addMovement(ev);
                }
                Log.d("onDrag", TAG + "ACTION_DOWN 111111111111  mLastTouchX :" + mLastTouchX
                        + ",mLastTouchY：" + mLastTouchY + "，mActivePointerId：" + mActivePointerId);
                mLastTouchX = getActiveX(ev);
                mLastTouchY = getActiveY(ev);
                Log.d("onDrag", TAG + "ACTION_DOWN 111111111111——2222222222222  mLastTouchX :" + mLastTouchX
                        + ",mLastTouchY：" + mLastTouchY + "，mActivePointerId：" + mActivePointerId);

                mIsDragging = false;
                mZooming = false;
                break;
            case MotionEvent.ACTION_MOVE:
                float x = getActiveX(ev);
                float y = getActiveY(ev);
                float dx = x - mLastTouchX, dy = y - mLastTouchY;

                // 只有一个手指在屏幕上，可以拖拽
                if (!mZooming && !mIsDragging && ev.getPointerCount() == 1) {
                    // 如果已经开始缩放则不允许拖拽
                    // Use Pythagoras to see if drag length is larger than
                    // touch slop
                    // 判断满足拖拽的条件
                    mIsDragging = Math.sqrt((dx * dx) + (dy * dy)) >= mTouchSlop;
                }

                if (mIsDragging) {
                    Log.d("onDrag", TAG + "onDrag 111111111111------- x:" + x + ",y:" + y + " "
                            + ",dx:" + dx + ",dy:" + dy
                            + "，mLastTouchX：" + mLastTouchX + "，mLastTouchY：" + mLastTouchY);
                    if (isAfterZooming && mLastTouchX == 0 && mLastTouchY == 0) {
                        dx = x - mLastZoomingTouchX;
                        dy = y - mLastZoomingTouchY;
                        isAfterZooming = false;
                        mLastZoomingTouchX = 0;
                        mLastZoomingTouchY = 0;
                        if (!(Math.abs(dx) > 100 || Math.abs(dy) > 100)) {
                            mListener.onDrag(dx, dy);
                        }
                    } else {
                        mListener.onDrag(dx, dy);
                    }
                    mLastTouchX = x;
                    mLastTouchY = y;
                    if (null != mVelocityTracker) {
                        mVelocityTracker.addMovement(ev);
                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                mActivePointerId = INVALID_POINTER_ID;
                // Recycle Velocity Tracker
                if (null != mVelocityTracker) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                break;
            case MotionEvent.ACTION_UP:
                mActivePointerId = INVALID_POINTER_ID;
                if (mIsDragging) {
                    if (null != mVelocityTracker) {
                        mLastTouchX = getActiveX(ev);
                        mLastTouchY = getActiveY(ev);
                        Log.d("onDrag", TAG + "ACTION_UP 111111111111  mLastTouchX :" + mLastTouchX + ",mLastTouchY：" + mLastTouchY);

                        // Compute velocity within the last 1000ms
                        mVelocityTracker.addMovement(ev);
                        mVelocityTracker.computeCurrentVelocity(1000);

                        final float vX = mVelocityTracker.getXVelocity(),
                                vY = mVelocityTracker.getYVelocity();

                        // If the velocity is greater than minVelocity, call
                        // listener
                        if (Math.max(Math.abs(vX), Math.abs(vY)) >= mMinimumVelocity) {
                            mListener.onFling(mLastTouchX, mLastTouchY, -vX, -vY);
                        }
                    }
                }

                // Recycle Velocity Tracker
                if (null != mVelocityTracker) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                final int pointerIndex = Util.getPointerIndex(ev.getAction());
                final int pointerId = ev.getPointerId(pointerIndex);
                //mLastTouchX = getActiveX(ev);
                //mLastTouchY = getActiveY(ev);
                isAfterZooming = true;
                Log.d("onDrag", TAG + "ACTION_POINTER_UP 0000000000  mLastTouchX :" + mLastTouchX + ",mLastTouchY：" + mLastTouchY);
                if (pointerId == mActivePointerId) {
                    // This was our active pointer going up. Choose a new
                    // active pointer and adjust accordingly.
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    mActivePointerId = ev.getPointerId(newPointerIndex);
                    mLastZoomingTouchX = mLastTouchX;
                    mLastZoomingTouchY = mLastTouchY;
                    mLastTouchX = ev.getX(newPointerIndex);
                    mLastTouchY = ev.getY(newPointerIndex);
                    Log.d("onDrag", TAG + "ACTION_POINTER_UP 111111111111  mLastTouchX :" + mLastTouchX
                            + ",mLastTouchY：" + mLastTouchY + ",mLastZoomingTouchX:" + mLastZoomingTouchX + ",mLastZoomingTouchY:" + mLastZoomingTouchY + "，mActivePointerId：" + mActivePointerId);
                }
                break;
        }

        mActivePointerIndex = ev
                .findPointerIndex(mActivePointerId != INVALID_POINTER_ID ? mActivePointerId
                        : 0);
        return true;
    }
}
