package com.haokan.mytestimageview.custom.preview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.widget.Scroller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.haokan.mytestimageview.custom.PhotoBaseView;
import com.haokan.mytestimageview.custom.PhotoViewAttacher;
import com.haokan.mytestimageview.custom.listener.OnScaleChangedListener;
import com.haokan.mytestimageview.custom.listener.OnViewDragListener;

import java.lang.annotation.ElementType;

public class PhotoView extends PhotoBaseView implements OnScaleChangedListener, OnViewDragListener {
    private static final int RESET_ANIM_TIME = 100;
    private String TAG = "PhotoView ";

    private final Scroller mScroller;

    // 是否是预览的第一个View
    private boolean mStartView = false;
    // 是否是预览的最后一个View
    private boolean mEndView = false;
    private PhotoPreviewHelper mHelper;
    private ImageChangeListener mImageChangeListener;
    private final ViewConfiguration mViewConfiguration;

    // 当前是否正在拖拽
    private boolean mDragging;
    private boolean mBgAnimStart;

    // 透明度
    private int mIntAlpha = 255;
    // 记录缩放后垂直方向边界判定值
    private int mScaleVerticalScrollEdge = PhotoViewAttacher.VERTICAL_EDGE_INSIDE;
    // 记录缩放后水平方向边界判定值
    private int mScaleHorizontalScrollEdge = PhotoViewAttacher.HORIZONTAL_EDGE_INSIDE;
    private OnScaleChangedListener mOnScaleChangedListener;
    //拖拽最后的dx和dy 记录， 用于scale>1 拖拽抬起手指时  判断是否是下滑的边界
    private float mDragLastDx;
    private float mDragLastDy;

    public PhotoView(@NonNull Context context) {
        this(context, null);
    }

    public PhotoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PhotoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        super.setOnScaleChangeListener(this);
        setOnViewDragListener(this);
        mScroller = new Scroller(context);
        mViewConfiguration = ViewConfiguration.get(context);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                Log.d("onDrag", "dispatchTouchEvent: onFingerUp event.getX()：" + event.getX() + ",event.getX():" + event.getY());
                onFingerUp();
                break;
        }

        return super.dispatchTouchEvent(event);
    }

    /**
     * 手指抬起的操作
     */
    private void onFingerUp() {
        //scale 大于1 且 isVerticalScrollOutTop 为true 先执行
        if (getScale() > 1 && mDragging && isVerticalScrollOutTop(mDragLastDx, mDragLastDy)) {
            mDragging = false;
            if (mIntAlpha < 240) {
                Log.d("onDrag", "dispatchTouchEvent: onFingerUp mHelper.exit()111111;");
                mDragLastDx = 0;
                mDragLastDy = 0;
                // 执行 exit之前 先translation 偏移到 原位置的左下方 再执行退出动画
                mHelper.exit();
            } else {
                if (Math.abs(getScrollX()) > 0 || Math.abs(getScrollY()) > 0) {
                    Log.d("onDrag", "dispatchTouchEvent: onFingerUp Math.abs(getScrollX()：" + Math.abs(getScrollX()) + ",Math.abs(getScrollY():" + Math.abs(getScrollY()));
                    reset();
                }
            }
        } else if (getScale() > 1 && isVerticalScrollOutBottom(mDragLastDx, mDragLastDy)) {
            mDragging = false;
            mDragLastDx = 0;
            mDragLastDy = 0;
            if (Math.abs(getScrollX()) > 0 || Math.abs(getScrollY()) > 0) {
                Log.d("onDrag", "dispatchTouchEvent: onFingerUp Math.abs(getScrollX()：" + Math.abs(getScrollX()) + ",Math.abs(getScrollY():" + Math.abs(getScrollY()));
                reset();
            }
            return;
        } else {
            mDragging = false;
            if (getScale() > 1) {
                if (Math.abs(getScrollX()) > 0 || Math.abs(getScrollY()) > 0) {
                    Log.d("onDrag", "dispatchTouchEvent: onFingerUp Math.abs(getScrollX()：" + Math.abs(getScrollX()) + ",Math.abs(getScrollY():" + Math.abs(getScrollY()));
                    reset();
                }
                return;
            }
            // 这里恢复位置和透明度
            if (mIntAlpha != 255 && getScale() < 0.8) {
                // 执行退出动画
                Log.d("onDrag", "dispatchTouchEvent: onFingerUp mHelper.exit() 22222222;");
                mHelper.exit();
            } else {
                reset();
            }
        }

    }

    /**
     * 预览图重置
     */
    private void reset() {
        mIntAlpha = 255;
        mBgAnimStart = true;
        Log.d("onDrag", " onFingerUp reset：");
        mHelper.doViewBgAnim(Color.BLACK, RESET_ANIM_TIME, new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mBgAnimStart = false;
            }
        });

        mScroller.startScroll(
                getScrollX(),
                getScrollY(),
                -getScrollX(),
                -getScrollY(), RESET_ANIM_TIME
        );
        invalidate();
    }

    @Override
    public void setOnScaleChangeListener(OnScaleChangedListener onScaleChangedListener) {
        mOnScaleChangedListener = onScaleChangedListener;
    }

    @Override
    public void onScaleChange(float scaleFactor, float focusX, float focusY) {
        mScaleVerticalScrollEdge = attacher.getVerticalScrollEdge();
        mScaleHorizontalScrollEdge = attacher.getHorizontalScrollEdge();
        Log.d("onDrag", TAG + "ScaleGestureDetector onScaleChange 444444444444:");
        if (mOnScaleChangedListener != null) {
            mOnScaleChangedListener.onScaleChange(scaleFactor, focusX, focusY);
        }
    }

    //
    @Override
    public boolean onDrag(float dx, float dy) {
        boolean intercept = mBgAnimStart
                || Math.sqrt((dx * dx) + (dy * dy)) < mViewConfiguration.getScaledTouchSlop()
                || !hasVisibleDrawable();

        Log.d("onDrag", TAG + "onDrag 4444444444444 intercept:" + intercept + ",dx:" + dx + ",dy:" + dy);
        if (!mDragging && intercept) {
            return false;
        }


        //scale >1;
        if (getScale() > 1) {// 如果图片的scale 大于1，图片处于放大状态，可以拖拽
            // 需要处理 图片的scale 大于1，垂直方向的滑动 图片高度未超过 screenView 的高度，可以下拉消失
            if (isVerticalScrollOutTop(dx, dy) || isVerticalScrollOutBottom(dx, dy)) {
                Log.d("onDrag", TAG + "onDrag 4444444444444  isVerticalScrollOutTop:");
            } else {
                return dragWhenScaleThanOne(dx, dy);
            }
        }

        if (!mDragging && Math.abs(dx) > Math.abs(dy)) {//横向的大于 竖向的拖拽，不消费此次
            return false;
        }

        if (!mDragging) {
            // 执行拖拽操作，请求父类不要拦截请求
            ViewParent parent = getParent();
            if (parent != null) {
                parent.requestDisallowInterceptTouchEvent(true);
            }
        }

        mDragging = true;
        mDragLastDx = dx;
        mDragLastDy = dy;
        float scale = getScale();
        // 移动图像
        if (scale > 1 && isVerticalScrollOutTop(dx, dy)) {
            scrollBy(0, ((int) -dy));
            // 新增的逻辑，scale 大于1，且是下拉的操作
            float scrollY = getScrollY();
            if (scrollY >= 0) {
                mIntAlpha = 255;
            } else {
                //scale -= dy * 0.003f;
                mIntAlpha -= dy * 0.03;
            }
            if (scale < 0) {
                scale = 0f;
            }
            if (mIntAlpha < 200) {
                mIntAlpha = 200;
            } else if (mIntAlpha > 255) {
                mIntAlpha = 255;
            }

            Log.d("onDrag", TAG + "onDrag  4444444444444-5555555555555555 getScrollY:" + scrollY);
            mHelper.mRootViewBgMask.getBackground().setAlpha(mIntAlpha);
            mHelper.showThumbnailViewMask(mIntAlpha >= 255);
            if (scrollY < 0 && scale > 1) {
                // 更改大小
                setScale(scale);
                Log.d("onDrag", TAG + "onDrag  4444444444444-66666666666666 scale:" + scale);
            }
        } else if (scale > 1 && isVerticalScrollOutBottom(dx, dy)) {
            scrollBy(0, ((int) -dy));

        } else {
            //原来的逻辑
            scrollBy(((int) -dx), ((int) -dy));
            float scrollY = getScrollY();
            if (scrollY >= 0) {
                scale = 1f;
                mIntAlpha = 255;
            } else {
                scale -= dy * 0.001f;
                mIntAlpha -= dy * 0.03;
            }

            if (scale > 1) {
                scale = 1f;
            } else if (scale < 0) {
                scale = 0f;
            }

            if (mIntAlpha < 200) {
                mIntAlpha = 200;
            } else if (mIntAlpha > 255) {
                mIntAlpha = 255;
            }
            Log.d("onDrag", TAG + "onDrag  4444444444444-3333333333333 getScrollY:" + scrollY);
            mHelper.mRootViewBgMask.getBackground().setAlpha(mIntAlpha);
            mHelper.showThumbnailViewMask(mIntAlpha >= 255);

            if (scrollY < 0 && scale >= 0.6) {
                // 更改大小
                setScale(scale);
                Log.d("onDrag", TAG + "onDrag  4444444444444-4444444444 scale:" + scale);
            }
        }

        return true;
    }

    /**
     * 垂直方向  滑动打的判断
     *
     * @param dx
     * @param dy
     * @return true 表示触发下拉 消失的逻辑
     */
    private boolean isVerticalScrollOutTop(float dx, float dy) {
        //判断一下 下拉的距离大于上边界
        boolean dyBigDx = Math.abs(dy) > Math.abs(dx);
        int verticalScrollEdge = attacher.getVerticalScrollEdge();//0 靠近边缘，-1 图片边缘超出image 高度，-2，图片边缘在image高度内
        int horizontalScrollEdge = attacher.getHorizontalScrollEdge();
        boolean isTop = (verticalScrollEdge != PhotoViewAttacher.VERTICAL_EDGE_OUTSIDE) || (getScale() > 1 && verticalScrollEdge == PhotoViewAttacher.VERTICAL_EDGE_OUTSIDE);//除 图片边缘超出image 高度 以外的情况，
        boolean isBottom = verticalScrollEdge == PhotoViewAttacher.VERTICAL_EDGE_BOTTOM
                || verticalScrollEdge == PhotoViewAttacher.VERTICAL_EDGE_BOTH;

        boolean isVerticalScroll = dyBigDx && ((isTop && dy > 0) || (isBottom && dy < 0));
        boolean isVerticalScrollTop = dyBigDx && (isTop && dy > 0);
        boolean isVerticalScrollBottom = dyBigDx && (isBottom && dy < 0);

        Log.d("onDrag", TAG + "onDrag isVerticalScrollOutTop 4444444444444-222222222222 isVerticalScroll:" + isVerticalScroll
                + ",isVerticalScrollTop:" + isVerticalScrollTop + ",isVerticalScrollBottom:" + isVerticalScrollBottom);
        Log.d("onDrag", TAG + "onDrag isVerticalScrollOutTop 4444444444444-222222222222 verticalScrollEdge:" + verticalScrollEdge + ",horizontalScrollEdge:" + horizontalScrollEdge);

        return dyBigDx && dy > 0 && isVerticalScrollTop;
    }

    /**
     * @param dx
     * @param dy
     * @return true 表示触发 拉 底部的边界
     */
    private boolean isVerticalScrollOutBottom(float dx, float dy) {

        //判断一下 下拉的距离大于上边界
        boolean dyBigDx = Math.abs(dy) > Math.abs(dx);
        int verticalScrollEdge = attacher.getVerticalScrollEdge();//0 靠近边缘，-1 图片边缘超出image 高度，-2，图片边缘在image高度内
        int horizontalScrollEdge = attacher.getHorizontalScrollEdge();
        boolean isTop = verticalScrollEdge != PhotoViewAttacher.VERTICAL_EDGE_OUTSIDE;//除 图片边缘超出image 高度 以外的情况，
        boolean isBottom = verticalScrollEdge == PhotoViewAttacher.VERTICAL_EDGE_BOTTOM
                || verticalScrollEdge == PhotoViewAttacher.VERTICAL_EDGE_BOTH;

        boolean isVerticalScroll = dyBigDx && ((isTop && dy > 0) || (isBottom && dy < 0));
        boolean isVerticalScrollTop = dyBigDx && (isTop && dy > 0);
        boolean isVerticalScrollBottom = dyBigDx && (isBottom && dy < 0);

        Log.d("onDrag", TAG + "onDrag 4444444444444-3333333333 isVerticalScroll:" + isVerticalScroll
                + ",isVerticalScrollTop:" + isVerticalScrollTop + ",isVerticalScrollBottom:" + isVerticalScrollBottom);
        Log.d("onDrag", TAG + "onDrag 4444444444444-33333333333 verticalScrollEdge:" + verticalScrollEdge + ",horizontalScrollEdge:" + horizontalScrollEdge);

        return dyBigDx && dy < 0 && verticalScrollEdge != PhotoViewAttacher.VERTICAL_EDGE_OUTSIDE;
    }

    /**
     * 处理图片如果超出控件大小时的滑动
     * 左右滑动 的边界
     * 下滑的边界
     */
    private boolean dragWhenScaleThanOne(float dx, float dy) {
        boolean dxBigDy = Math.abs(dx) > Math.abs(dy);
        if (mDragging) {
            dx *= 0.2f;
            dy *= 0.2f;
            int scrollX = (int) (getScrollX() - dx);
            int scrollY = (int) (getScrollY() - dy);
            int width = (int) (getWidth() * 0.2);
            int height = (int) (getHeight() * 0.2);
            if (Math.abs(scrollX) > width) {
                dx = 0;
            }

            if (Math.abs(scrollY) > height) {
                dy = 0;
            }

            if (dxBigDy) {
                dy = 0;
            } else {
                dx = 0;
            }
            Log.d("onDrag", TAG + "dragWhenScaleThanOne 55555555555 mDragging:" + mDragging);
            // 移动图像
            scrollBy(((int) -dx), ((int) -dy));
            return true;
        } else {
            int verticalScrollEdge = attacher.getVerticalScrollEdge();
            int horizontalScrollEdge = attacher.getHorizontalScrollEdge();
            boolean isTop = verticalScrollEdge == PhotoViewAttacher.VERTICAL_EDGE_TOP
                    || verticalScrollEdge == PhotoViewAttacher.VERTICAL_EDGE_BOTH;
            boolean isBottom = verticalScrollEdge == PhotoViewAttacher.VERTICAL_EDGE_BOTTOM
                    || verticalScrollEdge == PhotoViewAttacher.VERTICAL_EDGE_BOTH;
            boolean isStart = horizontalScrollEdge == PhotoViewAttacher.HORIZONTAL_EDGE_LEFT
                    || horizontalScrollEdge == PhotoViewAttacher.HORIZONTAL_EDGE_BOTH;
            boolean isEnd = horizontalScrollEdge == PhotoViewAttacher.HORIZONTAL_EDGE_RIGHT
                    || horizontalScrollEdge == PhotoViewAttacher.HORIZONTAL_EDGE_BOTH;
            boolean isVerticalScroll = !dxBigDy && ((isTop && dy > 0) || (isBottom && dy < 0));
            boolean isHorizontalScroll = dxBigDy && ((mStartView && isStart && dx > 0) || (mEndView && isEnd && dx < 0));
            if ((isVerticalScroll && mScaleVerticalScrollEdge == PhotoViewAttacher.VERTICAL_EDGE_OUTSIDE)
                    || (isHorizontalScroll && mScaleHorizontalScrollEdge == PhotoViewAttacher.VERTICAL_EDGE_OUTSIDE)) {
                // 执行拖拽操作，请求父类不要拦截请求
                ViewParent parent = getParent();
                if (parent != null) {
                    parent.requestDisallowInterceptTouchEvent(true);
                }
                Log.d("onDrag", TAG + "dragWhenScaleThanOne 6666666666 mDragging:" + mDragging);

                mDragging = true;
                // 移动图像
                scrollBy(((int) -dx), ((int) -dy));
                return true;
            }
        }
        return false;
    }

    /**
     * 是否存在可观察的图像
     */
    private boolean hasVisibleDrawable() {
        if (getDrawable() == null) {
            return false;
        }

        Drawable drawable = getDrawable();
        // 获得ImageView中Image的真实宽高，
        int dw = drawable.getBounds().width();
        int dh = drawable.getBounds().height();
        return dw > 0 && dh > 0;
    }

    @Override
    public float getAlpha() {
        return mIntAlpha;
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        if (mImageChangeListener != null) {
            mImageChangeListener.onChange(getDrawable());
        }
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        if (mImageChangeListener != null) {
            mImageChangeListener.onChange(getDrawable());
        }
    }

    @Override
    public void setImageURI(Uri uri) {
        super.setImageURI(uri);
        if (mImageChangeListener != null) {
            mImageChangeListener.onChange(getDrawable());
        }
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        if (mImageChangeListener != null) {
            mImageChangeListener.onChange(getDrawable());
        }
    }

    public void setPhotoPreviewHelper(PhotoPreviewHelper helper) {
        mHelper = helper;
    }

    public void setImageChangeListener(ImageChangeListener listener) {
        mImageChangeListener = listener;
    }

    public void setStartView(boolean isStartView) {
        mStartView = isStartView;
    }

    public void setEndView(boolean isEndView) {
        mEndView = isEndView;
    }


}
