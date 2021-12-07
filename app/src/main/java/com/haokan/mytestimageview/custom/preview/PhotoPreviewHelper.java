package com.haokan.mytestimageview.custom.preview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewParent;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.lifecycle.Lifecycle.State;
import androidx.transition.ChangeBounds;
import androidx.transition.ChangeImageTransform;
import androidx.transition.ChangeTransform;
import androidx.transition.Transition;
import androidx.transition.TransitionListenerAdapter;
import androidx.transition.TransitionManager;
import androidx.transition.TransitionSet;

import com.haokan.mytestimageview.R;
import com.haokan.mytestimageview.utils.MatrixUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 图片预览辅助类，主要处理预览打开和关闭时过渡动画
 *
 * @author Created by wanggaowan on 2019/2/27 0027 11:24
 */
@RestrictTo(Scope.LIBRARY)
public class PhotoPreviewHelper {
    
    /*
     此预览库动画采用androidx.transition.Transition库实现，使用此库有以下几个点需要注意
        说明：适配不同缩放类型的逻辑都是基于Glide图片加载库
             srcView 指定缩略图，且是ImageView类型
             helperView 指定实现从缩略图到预览图过度动画辅助类
             photoView 指定预览大图
        
        1. 如果srcView 的缩放类型为ScaleType.CENTER_CROP，那么helperView 设置的drawable 必须为photoView drawable，
           否则过度动画不能无缝衔接。比如以下情况：
           
           // srcView并非加载的原图，缩放类型为ScaleType.CENTER_CROP
           Glide.with(mContext)
            .load(item)
            // .override(Target.SIZE_ORIGINAL)
            .into(srcView);
            
           // 此时不管photoView是否加载原图，如果helperView设置为srcView的drawable，那么过度动画不能无缝衔接
           Glide.with(mContext)
            .load(item)
            // .override(Target.SIZE_ORIGINAL)
            .into(photoView);
            
        2. 如果srcView 的缩放类型为非ScaleType.CENTER_CROP，那么helperView 设置的drawable 必须为srcView drawable，
           否则过度动画不能无缝衔接。比如以下情况：
           
           // srcView缩放类型非ScaleType.CENTER_CROP
           Glide.with(mContext)
            .load(item)
            // 无论是否加载原图
            // .override(Target.SIZE_ORIGINAL)
            .into(srcView);
            
           // 此时不管photoView是否加载原图，如果helperView设置为photoView的drawable，那么过度动画不能无缝衔接
           Glide.with(mContext)
            .load(item)
            // .override(Target.SIZE_ORIGINAL)
            .into(photoView);
           
        3. 由于存在srcView实际显示大小并非布局或代码指定的固定大小，因此在helperView外部包裹一层父布局，用于裁剪
     */

    private static final long OPEN_AND_EXIT_ANIM_DURATION = 200;
    private static final long OPEN_AND_EXIT_ANIM_DURATION_FOR_IMAGE = 350;

    private static final ArgbEvaluator ARGB_EVALUATOR = new ArgbEvaluator();
    private static final Interpolator INTERPOLATOR = new FastOutSlowInInterpolator();

    private static final int ANIM_START_PRE = 0;
    private static final int ANIM_START = 1;
    private static final int ANIM_END = 2;

    final PreviewDialogFragment mFragment;
    // 占位图，辅助执行过度动画
    // 为什么不直接使用mPhotoView进行过度动画，主要有一下几个问题
    // 1. 使用mPhotoView，某些情况下预览打开后图片某一部分自动被放大。导致这个的原因可能是使用Glide加载库，设置了placeholder导致
    // 2. 预览动画开始时，需要对图片进行位移，裁减等操作，而预览大图加载时，不能调整其大小和缩放类型，否则预览大图显示将出现问题
    private final ImageView mHelperView;
    private final FrameLayout mHelperViewParent;
    final FrameLayout mRootViewBgMask;
    private final ShareData mShareData;

    // 当前界面显示预览图位置
    private int mPosition;
    // 上一次统计预览图对应缩列图数据时预览图所处位置
    private int mOldPosition = -1;

    private View mThumbnailView;
    private int mThumbnailViewVisibility = View.VISIBLE;
    private ScaleType mThumbnailViewScaleType;
    // 当前界面是否需要执行动画
    private boolean mNeedInAnim;
    // 根据动画时间可决定整个预览是否需要执行动画
    private long mAnimDuration;

    private final int[] mIntTemp = new int[2];
    // 缩略图设定大小
    private final int[] mSrcViewSize = new int[2];
    // 缩略图实际可显示大小
    private final int[] mSrcViewParentSize = new int[2];
    private final int[] mSrcImageLocation = new int[2];
    private final float[] mFloatTemp = new float[2];
    // 辅助图是否可接收新图片
    private boolean mHelpViewCanSetImage = true;

    // 预览打开动画执行结束
    private boolean mOpenAnimEnd = false;
    private List<OnOpenListener> mOpenListenerList;
    private List<OnExitListener> mExitListenerList;

    private String TAG = "PhotoPreviewHelper";

    public PhotoPreviewHelper(PreviewDialogFragment fragment, int position) {
        mFragment = fragment;
        mShareData = mFragment.mShareData;
        mPosition = position;

        mFragment.mRootView.setFocusableInTouchMode(true);
        mFragment.mRootView.requestFocus();
        mRootViewBgMask = mFragment.mRootView;
        mHelperView = mFragment.mRootView.findViewById(R.id.iv_anim);
        mHelperViewParent = mFragment.mRootView.findViewById(R.id.fl_parent);

        mRootViewBgMask.setBackgroundColor(Color.TRANSPARENT);

        mHelperViewParent.setVisibility(View.INVISIBLE);
        mHelperViewParent.setTranslationX(0);
        mHelperViewParent.setTranslationY(0);
        mHelperView.setScaleX(1f);
        mHelperView.setScaleY(1f);
        mHelperView.setImageDrawable(null);
        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            mHelperView.setOutlineProvider(null);
        }
        setViewSize(mHelperViewParent, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        setViewSize(mHelperView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

        initEvent();
        initData();
    }

    private void initEvent() {
        mFragment.mRootView.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK
                    && (event == null || event.getAction() == KeyEvent.ACTION_UP)
                    && mOpenAnimEnd) {
                exit();
                return true;
            }

            return false;
        });

        mFragment.mRootView.setOnClickListener(v -> {
            if (!mOpenAnimEnd) {
                return;
            }

            exit();
        });
    }

    private void initData() {
        if (mPosition != mOldPosition) {
            mThumbnailView = getThumbnailView(mShareData);
            if (mThumbnailView != null) {
                mThumbnailViewVisibility = mThumbnailView.getVisibility();
            }
            mAnimDuration = getOpenAndExitAnimDuration(mThumbnailView, mShareData);
            initThumbnailViewScaleType();
            mOldPosition = mPosition;
        }

        mNeedInAnim = mAnimDuration > 0 && mShareData.showNeedAnim;
        doPreviewAnim();
    }

    /**
     * 初始化缩略图的缩放类型
     */
    private void initThumbnailViewScaleType() {
        if (mThumbnailView instanceof ImageView) {
            mThumbnailViewScaleType = ((ImageView) mThumbnailView).getScaleType();
            if (mThumbnailViewScaleType == ScaleType.CENTER || mThumbnailViewScaleType == ScaleType.CENTER_INSIDE) {
                Drawable drawable = ((ImageView) mThumbnailView).getDrawable();
                if (drawable != null && drawable.getIntrinsicWidth() >= mThumbnailView.getWidth()
                        && drawable.getIntrinsicHeight() >= mThumbnailView.getHeight()) {
                    // ScaleType.CENTER：不缩放图片，如果图片大于图片控件，效果与ScaleType.CENTER_CROP一致
                    if (mThumbnailViewScaleType == ScaleType.CENTER) {
                        mThumbnailViewScaleType = ScaleType.CENTER_CROP;
                    }
                } else if (mAnimDuration > 0) {
                    // 当图片小于控件大小时，对于缩放类型ScaleType.CENTER 和 ScaleType.CENTER_INSIDE，需要以动画的方式打开
                    // 除非整个预览不需要动画，否则再推出时，可能不会无缝衔接
                    mNeedInAnim = true;
                }
            }
        } else {
            mThumbnailViewScaleType = null;
        }
    }

    /**
     * 执行预览动画
     */
    private void doPreviewAnim() {
        if (!mNeedInAnim) {
            initNoAnim();
            // 整个预览无需执行动画，因此第一个执行的预览界面执行一次预览打开回调
            callOnOpen(ANIM_START_PRE, ANIM_START, ANIM_END);
            mShareData.showNeedAnim = false;
            return;
        }

        // 处理进入时的动画
        mNeedInAnim = false;
        mShareData.showNeedAnim = false;
        mHelpViewCanSetImage = true;
        mShareData.preDrawableLoadListener = drawable -> {
            if (mHelpViewCanSetImage) {
                mHelperView.setImageDrawable(drawable);
            }
        };
        mHelperView.setImageDrawable(mShareData.preLoadDrawable);
        mShareData.preLoadDrawable = null;

        if (mThumbnailView == null) {
            enterAnimByScale();
            return;
        }

        enterAnimByTransition(mThumbnailView);
    }

    /**
     * 无动画进入
     */
    private void initNoAnim() {
        mRootViewBgMask.setBackgroundColor(Color.BLACK);
        mHelperViewParent.setVisibility(View.INVISIBLE);
    }

    /**
     * 仅缩放动画
     */
    private void enterAnimByScale() {
        ObjectAnimator scaleOx = ObjectAnimator.ofFloat(mHelperView, "scaleX", 0, 1f);
        ObjectAnimator scaleOy = ObjectAnimator.ofFloat(mHelperView, "scaleY", 0, 1f);

        callOnOpen(ANIM_START_PRE);
        AnimatorSet set = new AnimatorSet();
        set.setDuration(mAnimDuration);
        set.setInterpolator(INTERPOLATOR);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                callOnOpen(ANIM_START);
                mHelperViewParent.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mHelperViewParent.setVisibility(View.INVISIBLE);
                callOnOpen(ANIM_END);
            }
        });

        set.playTogether(scaleOx, scaleOy, getViewBgAnim(Color.BLACK, mAnimDuration, null));
        set.start();
    }

    /**
     * 使用Transition库实现过度动画
     */
    private void enterAnimByTransition(final View thumbnailView) {
        long delay = mShareData.openAnimDelayTime;
        callOnOpen(ANIM_START_PRE);
        if (delay > 0) {
            mHelperView.postDelayed(this::doEnterAnimByTransition, delay);
            initEnterAnimByTransition(thumbnailView);
        } else {
            initEnterAnimByTransition(thumbnailView);
            mHelperView.post(this::doEnterAnimByTransition);
        }
    }

    /**
     * 初始化Transition所需内容
     */
    private void initEnterAnimByTransition(final View thumbnailView) {
        getSrcViewSize(thumbnailView);
        getSrcViewLocation(thumbnailView);

        mHelperViewParent.setTranslationX(mSrcImageLocation[0]);
        mHelperViewParent.setTranslationY(mSrcImageLocation[1]);
        setViewSize(mHelperViewParent, mSrcViewParentSize[0], mSrcViewParentSize[1]);

        setHelperViewDataByThumbnail();
    }

    /**
     * 展示/隐藏缩略图蒙层数据
     */
    void showThumbnailViewMask(boolean show) {
        if (!mShareData.config.showThumbnailViewMask) {
            return;
        }

        mThumbnailView.setVisibility(show ? View.INVISIBLE : mThumbnailViewVisibility);
    }

    /**
     * 根据缩列图数据设置预览占位View大小、位置和缩放模式
     */
    private void setHelperViewDataByThumbnail() {
        if (mThumbnailViewScaleType != null) {
            mHelperView.setScaleType(mThumbnailViewScaleType);
        } else {
            mHelperView.setScaleType(ScaleType.FIT_CENTER);
        }

        mHelperView.setTranslationX(0);
        mHelperView.setTranslationY(0);
        if (mShareData == null || mShareData.config.shapeTransformType == null || mThumbnailViewScaleType == null) {
            setViewSize(mHelperView, mSrcViewSize[0], mSrcViewSize[1]);
            return;
        }

        // 需要进行图片裁剪，部分缩放类型不能使图片充满控件，而裁剪基于控件，因此设置控件大小与图片大小一致
        Drawable drawable = ((ImageView) mThumbnailView).getDrawable();
        if (drawable == null) {
            setViewSize(mHelperView, mSrcViewSize[0], mSrcViewSize[1]);
            return;
        }

        int intrinsicWidth = drawable.getIntrinsicWidth();
        int intrinsicHeight = drawable.getIntrinsicHeight();
        if (intrinsicWidth <= 0 || intrinsicHeight <= 0) {
            setViewSize(mHelperView, mSrcViewSize[0], mSrcViewSize[1]);
            return;
        }

        int width = mSrcViewSize[0];
        int height = mSrcViewSize[1];
        if (mShareData.config.shapeTransformType == ShapeTransformType.CIRCLE) {
            int minSize = Math.min(width, height);
            switch (mThumbnailViewScaleType) {
                case FIT_START:
                case MATRIX:
                    width = minSize;
                    height = minSize;
                    break;

                case FIT_END:
                    mHelperView.setTranslationX(width - minSize);
                    mHelperView.setTranslationY(height - minSize);
                    width = minSize;
                    height = minSize;
                    break;

                case FIT_CENTER:
                    mHelperView.setTranslationX((width - minSize) / 2f);
                    mHelperView.setTranslationY((height - minSize) / 2f);
                    width = minSize;
                    height = minSize;
                    break;

                case CENTER_INSIDE:
                    if (intrinsicWidth < width && intrinsicHeight < height) {
                        minSize = Math.min(intrinsicWidth, intrinsicHeight);
                    }

                    mHelperView.setTranslationX((width - minSize) / 2f);
                    mHelperView.setTranslationY((height - minSize) / 2f);
                    width = minSize;
                    height = minSize;
                    break;

                case CENTER:
                    int w = Math.min(intrinsicWidth, width);
                    int h = Math.min(intrinsicHeight, height);
                    minSize = Math.min(w, h);

                    mHelperView.setTranslationX((width - minSize) / 2f);
                    mHelperView.setTranslationY((height - minSize) / 2f);
                    width = minSize;
                    height = minSize;
                    break;
            }

            // 需要重新设置缩放类型为CENTER_CROP，否则动画不会无缝衔接
            // 重新调整缩放类型，全部都是为了适配 Transition动画ChangeImageTransform过渡
            mHelperView.setScaleType(ScaleType.CENTER_CROP);
            setViewSize(mHelperView, width, height);
            return;
        }

        switch (mThumbnailViewScaleType) {
            case FIT_START:
            case FIT_END:
            case FIT_CENTER:
            case CENTER_INSIDE:
                if (intrinsicWidth < width && intrinsicHeight < height) {
                    if (mThumbnailViewScaleType == ScaleType.CENTER_INSIDE) {
                        mHelperView.setTranslationX((width - intrinsicWidth) / 2f);
                        mHelperView.setTranslationY((height - intrinsicHeight) / 2f);
                        width = intrinsicWidth;
                        height = intrinsicHeight;
                    } else {
                        float widthScale = width * 1f / intrinsicWidth;
                        float heightScale = height * 1f / intrinsicHeight;
                        if (widthScale < heightScale) {
                            // 根据宽度缩放值缩放高度，放大
                            intrinsicHeight = (int) (widthScale * intrinsicHeight);
                            if (mThumbnailViewScaleType == ScaleType.FIT_END) {
                                mHelperView.setTranslationY(height - intrinsicHeight);
                            } else if (mThumbnailViewScaleType == ScaleType.FIT_CENTER) {
                                mHelperView.setTranslationY((height - intrinsicHeight) / 2f);
                            }
                            height = intrinsicHeight;
                        } else if (widthScale > heightScale) {
                            // 根据高度缩放值缩放宽度，放大
                            intrinsicWidth = (int) (heightScale * intrinsicWidth);
                            if (mThumbnailViewScaleType == ScaleType.FIT_END) {
                                mHelperView.setTranslationX(width - intrinsicWidth);
                            } else if (mThumbnailViewScaleType == ScaleType.FIT_CENTER) {
                                mHelperView.setTranslationX((width - intrinsicWidth) / 2f);
                            }
                            width = intrinsicWidth;
                        }
                    }
                } else if (intrinsicWidth > width && intrinsicHeight > height) {
                    float widthScale = intrinsicWidth * 1f / width;
                    float heightScale = intrinsicHeight * 1f / height;

                    if (widthScale > heightScale) {
                        // 根据宽度缩放值缩放高度，缩小
                        intrinsicHeight = (int) (intrinsicHeight / widthScale);
                        if (mThumbnailViewScaleType == ScaleType.FIT_END) {
                            mHelperView.setTranslationY(height - intrinsicHeight);
                        } else if (mThumbnailViewScaleType == ScaleType.FIT_CENTER
                                || mThumbnailViewScaleType == ScaleType.CENTER_INSIDE) {
                            mHelperView.setTranslationY((height - intrinsicHeight) / 2f);
                        }
                        height = intrinsicHeight;
                    } else if (widthScale < heightScale) {
                        // 根据高度缩放值缩放宽度，缩小
                        intrinsicWidth = (int) (intrinsicWidth / heightScale);
                        if (mThumbnailViewScaleType == ScaleType.FIT_END) {
                            mHelperView.setTranslationX(width - intrinsicWidth);
                        } else if (mThumbnailViewScaleType == ScaleType.FIT_CENTER
                                || mThumbnailViewScaleType == ScaleType.CENTER_INSIDE) {
                            mHelperView.setTranslationX((width - intrinsicWidth) / 2f);
                        }
                        width = intrinsicWidth;
                    }
                } else if (intrinsicWidth < width) {
                    if (intrinsicHeight > height) {
                        // 根据高度缩放值缩放宽度，缩小
                        float heightScale = intrinsicHeight * 1f / height;
                        intrinsicWidth = (int) (intrinsicWidth / heightScale);
                    }

                    if (mThumbnailViewScaleType == ScaleType.FIT_END) {
                        mHelperView.setTranslationX(width - intrinsicWidth);
                    } else if (mThumbnailViewScaleType == ScaleType.FIT_CENTER
                            || mThumbnailViewScaleType == ScaleType.CENTER_INSIDE) {
                        mHelperView.setTranslationX((width - intrinsicWidth) / 2f);
                    }
                    width = intrinsicWidth;
                } else {
                    if (intrinsicWidth > width) {
                        // 根据宽度缩放值缩放高度，缩小
                        float widthScale = intrinsicWidth * 1f / width;
                        intrinsicHeight = (int) (intrinsicHeight / widthScale);
                    }

                    if (mThumbnailViewScaleType == ScaleType.FIT_END) {
                        mHelperView.setTranslationY(height - intrinsicHeight);
                    } else if (mThumbnailViewScaleType == ScaleType.FIT_CENTER
                            || mThumbnailViewScaleType == ScaleType.CENTER_INSIDE) {
                        mHelperView.setTranslationY((height - intrinsicHeight) / 2f);
                    }
                    height = intrinsicHeight;
                }

                if (mThumbnailViewScaleType == ScaleType.FIT_CENTER
                        || mThumbnailViewScaleType == ScaleType.CENTER_INSIDE) {
                    // 需要重新设置缩放类型为CENTER_CROP，否则动画不会无缝衔接
                    // 重新调整缩放类型，全部都是为了适配 Transition动画ChangeImageTransform过渡
                    mHelperView.setScaleType(ScaleType.CENTER_CROP);
                }
                break;

            case CENTER:
                if (width > intrinsicWidth) {
                    mHelperView.setTranslationX((width - intrinsicWidth) / 2f);
                }

                if (height > intrinsicHeight) {
                    mHelperView.setTranslationY((height - intrinsicHeight) / 2f);
                }

                width = Math.min(intrinsicWidth, width);
                height = Math.min(intrinsicHeight, height);
                // 需要重新设置缩放类型为CENTER_CROP，否则动画不会无缝衔接
                // 重新调整缩放类型，全部都是为了适配 Transition动画ChangeImageTransform过渡
                mHelperView.setScaleType(ScaleType.CENTER_CROP);
                break;

            case MATRIX:
                width = Math.min(intrinsicWidth, width);
                height = Math.min(intrinsicHeight, height);
                mHelperView.setScaleType(ScaleType.FIT_START);
                break;
        }

        setViewSize(mHelperView, width, height);
    }

    /**
     * 执行预览打开过渡动画
     */
    private void doEnterAnimByTransition() {
        TransitionSet transitionSet = new TransitionSet()
                .setDuration(mAnimDuration)
                .addTransition(new ChangeBounds())
                .addTransition(new ChangeTransform())
                .setInterpolator(INTERPOLATOR)
                .addListener(new TransitionListenerAdapter() {
                    @Override
                    public void onTransitionStart(@NonNull Transition transition) {
                        callOnOpen(ANIM_START);
                        mHelpViewCanSetImage = false;
                        doViewBgAnim(Color.BLACK, mAnimDuration, null);
                        mHelperViewParent.setVisibility(View.VISIBLE);
                        // 不延迟会有闪屏
                        mThumbnailView.postDelayed(() -> showThumbnailViewMask(true), mAnimDuration / 10);
                    }

                    @Override
                    public void onTransitionEnd(@NonNull Transition transition) {
                        showThumbnailViewMask(false);
                        mHelpViewCanSetImage = true;
                        mHelperViewParent.setVisibility(View.INVISIBLE);
                        callOnOpen(ANIM_END);
                    }
                });

        if (mShareData.config.shapeTransformType != null) {
            if (mShareData.config.shapeTransformType == ShapeTransformType.CIRCLE) {
                transitionSet.addTransition(
                        new ChangeShape(Math.min(mSrcViewSize[0], mSrcViewSize[1]) / 2f, 0)
                                .addTarget(mHelperView));
            } else {
                transitionSet.addTransition(
                        new ChangeShape(mShareData.config.shapeCornerRadius, 0)
                                .addTarget(mHelperView));
            }
        }

        if (mHelperView.getDrawable() != null) {
            mHelpViewCanSetImage = false;
            // ChangeImageTransform执行MATRIX变换，因此一定需要最终加载完成图片
            transitionSet.addTransition(new ChangeImageTransform().addTarget(mHelperView));
        }

        TransitionManager.beginDelayedTransition((ViewGroup) mHelperViewParent.getParent(), transitionSet);

        mHelperViewParent.setTranslationX(0);
        mHelperViewParent.setTranslationY(0);
        setViewSize(mHelperViewParent, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

        mHelperView.setTranslationX(0);
        mHelperView.setTranslationY(0);
        mHelperView.setScaleType(ScaleType.FIT_CENTER);
        setViewSize(mHelperView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    /**
     * 设置View的大小
     */
    private void setViewSize(View target, int width, int height) {
        LayoutParams params = target.getLayoutParams();
        params.width = width;
        params.height = height;
        target.setLayoutParams(params);
    }

    /**
     * 退出预览
     *
     * @return {@code false}:未执行退出逻辑，可能当前界面已关闭或还未创建完成
     */
    public boolean exit() {
        if (!mFragment.getLifecycle().getCurrentState().isAtLeast(State.STARTED)) {
            return false;
        }

        if (mAnimDuration <= 0) {
            callOnExit(ANIM_START_PRE, ANIM_START, ANIM_END);
            return true;
        }

        View view = mFragment.mViewPager.findViewWithTag(mFragment.mViewPager.getCurrentItem());
        if (view == null) {
            callOnExit(ANIM_START_PRE, ANIM_START, ANIM_END);
            return true;
        }

        Object tag = view.getTag(R.id.view_holder);
        if (!(tag instanceof ImagePagerAdapter.ViewHolder)) {
            callOnExit(ANIM_START_PRE, ANIM_START, ANIM_END);
            return true;
        }

        ImagePagerAdapter.ViewHolder viewHolder = (ImagePagerAdapter.ViewHolder) tag;
        PhotoView photoView = viewHolder.getPhotoView();
        viewHolder.getLoading().setVisibility(View.GONE);

        if (photoView.getDrawable() == null) {
            callOnExit(ANIM_START_PRE, ANIM_START);
            doViewBgAnim(Color.TRANSPARENT, mAnimDuration, new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    callOnExit(ANIM_END);
                }
            });
            return true;
        }


        // 记录预览打开时，缩略图对象
        View openThumbnailView = mThumbnailView;
        if (mPosition != mOldPosition) {
            Log.d(TAG, "exit mPosition != mOldPosition:");
            mThumbnailView = getThumbnailView(mShareData);
            if (mThumbnailView != null) {
                mThumbnailViewVisibility = mThumbnailView.getVisibility();
            }
            mAnimDuration = getOpenAndExitAnimDuration(mThumbnailView, mShareData);
            initThumbnailViewScaleType();
            mOldPosition = mPosition;
        }

        //重置 辅助view 的大小 处理scale >1 的情况
        //if (photoView.getScale() > 1) {
        //    photoView.setScale(0.8f);
        //
        //}
        resetHelpViewSize(viewHolder);
        if (mShareData != null && mShareData.config != null && mShareData.config.mOnPagerItemPositionListener != null) {
            mShareData.config.mOnPagerItemPositionListener.onItemPositionWithInnerPosition(mShareData.config.mFlowPosition, mFragment.mViewPager.getCurrentItem());
        }

        if (mThumbnailView == null) {
            callOnOpen(ANIM_START_PRE);
            float scaleX = mHelperView.getScaleX();
            float scaleY = mHelperView.getScaleY();
            ObjectAnimator scaleOx = ObjectAnimator.ofFloat(mHelperView, "scaleX", 1f, 0f);
            ObjectAnimator scaleOy = ObjectAnimator.ofFloat(mHelperView, "scaleY", 1f, 0f);
            AnimatorSet set = new AnimatorSet();
            Log.d("onDrag", "onDrag mHelperView scale  scaleX:" + scaleX + ",scaleY:" + scaleY);
            set.setDuration(mAnimDuration);
            set.setInterpolator(INTERPOLATOR);
            set.playTogether(scaleOx, scaleOy, getViewBgAnim(Color.TRANSPARENT, mAnimDuration, null));

            set.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    callOnExit(ANIM_START);
                    mHelperViewParent.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    callOnExit(ANIM_END);
                }
            });

            set.start();
            return true;
        }

        exitAnimByTransition(mThumbnailView, photoView, openThumbnailView);
        return true;
    }

    /**
     * 重置辅助View的大小
     */
    private void resetHelpViewSize(ImagePagerAdapter.ViewHolder viewHolder) {
        PhotoView photoView = viewHolder.getPhotoView();
        float[] noScaleImageActualSize = viewHolder.getNoScaleImageActualSize();
        FrameLayout rootView = mFragment.mRootView;
        Log.d(TAG, "resetHelpViewSize 重置辅助View的大小:");

        if (mThumbnailViewScaleType == ScaleType.MATRIX || photoView.getScale() != 1) {
            Log.d(TAG, "resetHelpViewSize 重置辅助View的大小 111111111111111:");

            // thumbnailView是ScaleType.MATRIX需要设置ImageView与drawable大小一致,且如果是下拉后缩小后关闭预览，则肯能无法与缩略图无缝衔接
            // 得到关闭时预览图片真实绘制大小
            float[] imageActualSize = mFloatTemp;
            getImageActualSize(photoView, imageActualSize);
            if (noScaleImageActualSize[0] == 0 && noScaleImageActualSize[1] == 0) {
                // 此时只是降低偏移值，但是这是不准确的
                getImageActualSize(mHelperView, noScaleImageActualSize);
            }

            if (photoView.getScale() < 1 || (mThumbnailViewScaleType == ScaleType.MATRIX && photoView.getScale() == 1)) {
                Log.d(TAG, "resetHelpViewSize 重置辅助View的大小 222222222222222:");
                // 计算关闭时预览图片真实X,Y坐标
                // mPhotoView向下移动后缩放比例误差值，该值是手动调出来的，不清楚为什么超出了这么多
                // 只有mPhotoView.getScale() < 1时才会出现此误差
                float errorRatio = photoView.getScale() < 1 ? 0.066f : 0;
                // 此处取mRoot.getHeight()而不是mIvAnim.getHeight()是因为如果当前界面非默认预览的界面,
                // 那么mIvAnim.getHeight()获取的并不是可显示区域高度，只是图片实际绘制的高度，因此使用mRoot.getHeight()
                float y = rootView.getHeight() / 2f - noScaleImageActualSize[1] / 2 // 预览图片未移动未缩放时实际绘制drawable左上角Y轴值
                        - photoView.getScrollY() // 向下移动的距离，向上移动不会触发关闭
                        + imageActualSize[1] * (1 - photoView.getScale() - errorRatio); // 由于在向下移动时，伴随图片缩小，因此需要加上缩小高度
                float x = rootView.getWidth() / 2f - noScaleImageActualSize[0] / 2 // 预览图片未移动未缩放时实际绘制drawable左上角Z轴值
                        - photoView.getScrollX() // 向左或向右移动的距离
                        + imageActualSize[0] * (1 - photoView.getScale() - errorRatio); // 由于在向下移动时，伴随图片缩小，因此需要加上缩小宽度
                mHelperViewParent.setTranslationX(x);
                mHelperViewParent.setTranslationY(y);

            } else if (photoView.getScale() > 1) {
                Log.d(TAG, "resetHelpViewSize 重置辅助View的大小 33333333333333333:");
                Matrix imageMatrix = photoView.getImageMatrix();
                float scrollX = MatrixUtils.getValue(imageMatrix, Matrix.MTRANS_X);
                float scrollY = MatrixUtils.getValue(imageMatrix, Matrix.MTRANS_Y);
                float y = imageActualSize[1] > rootView.getHeight() ? scrollY : rootView.getHeight() / 2f - imageActualSize[1] / 2f;
                float x = imageActualSize[0] > rootView.getWidth() ? scrollX : rootView.getWidth() / 2f - imageActualSize[0] / 2f;
                //photoView.setScale(0.95f);
                //resetHelpViewSize(viewHolder);
                mHelperViewParent.setTranslationX(x);
                mHelperViewParent.setTranslationY(y);
            }
            setViewSize(mHelperViewParent, ((int) imageActualSize[0]), ((int) imageActualSize[1]));
            setViewSize(mHelperView, ((int) imageActualSize[0]), ((int) imageActualSize[1]));

        }
    }

    /**
     * 使用Transition库实现过度动画
     */
    private void exitAnimByTransition(final View thumbnailView, final PhotoView photoView, View openThumbnailView) {
        mHelperView.setScaleType(ScaleType.FIT_CENTER);
        mHelperView.setImageDrawable(photoView.getDrawable());

        // 计算初始进入和退出时，同一个缩略图Y轴差值，产生差值主要是非沉浸式模式下由全屏切换到非全屏(反之亦然)状态栏高度导致
        // 执行初始预览动画的ViewY轴坐标
        int oldY = mSrcImageLocation[1];
        final int offset;
        if (thumbnailView == openThumbnailView) {
            // 进入和退出缩略图位置不变
            // 获取退出时缩略图位置
            getSrcViewLocation(thumbnailView);
            // 进入和退出缩略图Y轴差值，基本是偏移状态栏高度
            offset = oldY - mSrcImageLocation[1];
        } else {
            if (openThumbnailView != null) {
                // 查找
                getSrcViewLocation(openThumbnailView);
                // 进入和脱出缩列图差值
                offset = oldY - mSrcImageLocation[1];
            } else {
                offset = 0;
            }

            getSrcViewLocation(thumbnailView);
            getSrcViewSize(thumbnailView);
        }

        callOnExit(ANIM_START_PRE);

        mHelperView.post(() -> {
            TransitionSet transitionSet = new TransitionSet()
                    .setDuration(mAnimDuration)
                    .addTransition(new ChangeBounds())
                    .addTransition(new ChangeTransform())
                    .addTransition(new ChangeImageTransform().addTarget(mHelperView))
                    .setInterpolator(INTERPOLATOR)
                    .addListener(new TransitionListenerAdapter() {
                        @Override
                        public void onTransitionEnd(@NonNull Transition transition) {
                            showThumbnailViewMask(false);
                            callOnExit(ANIM_END);
                        }

                        @Override
                        public void onTransitionStart(@NonNull Transition transition) {
                            callOnExit(ANIM_START);
                            if (photoView.getScale() >= 1) {
                                showThumbnailViewMask(true);
                            }
                            mHelperViewParent.setVisibility(View.VISIBLE);
                            doViewBgAnim(Color.TRANSPARENT, mAnimDuration, null);
                        }
                    });

            if (mShareData != null && mShareData.config.shapeTransformType != null) {
                if (mShareData.config.shapeTransformType == ShapeTransformType.CIRCLE) {
                    transitionSet.addTransition(
                            new ChangeShape(0, Math.min(mSrcViewSize[0], mSrcViewSize[1]) / 2f)
                                    .addTarget(mHelperView));
                } else {
                    transitionSet.addTransition(
                            new ChangeShape(0, mShareData.config.shapeCornerRadius)
                                    .addTarget(mHelperView));
                }
            }

            TransitionManager.beginDelayedTransition((ViewGroup) mHelperViewParent.getParent(), transitionSet);

            mHelperViewParent.setTranslationX(mSrcImageLocation[0]);
            mHelperViewParent.setTranslationY(mSrcImageLocation[1] + (mShareData != null && mShareData.config.exitAnimStartHideOrShowStatusBar ? offset : 0));
            setViewSize(mHelperViewParent, mSrcViewParentSize[0], mSrcViewParentSize[1]);

            setHelperViewDataByThumbnail();
        });
    }

    /**
     * 执行背景过渡动画
     */
    public void doViewBgAnim(final int endColor, long duration, AnimatorListenerAdapter listenerAdapter) {
        getViewBgAnim(endColor, duration, listenerAdapter).start();
    }

    /**
     * 返回背景过渡动画
     */
    Animator getViewBgAnim(final int endColor, long duration, AnimatorListenerAdapter listenerAdapter) {
        final int start = ((ColorDrawable) mRootViewBgMask.getBackground()).getColor();
        ValueAnimator animator = ValueAnimator.ofFloat(0, 1f);
        animator.addUpdateListener(animation ->
                mRootViewBgMask.setBackgroundColor((int) ARGB_EVALUATOR.evaluate(animation.getAnimatedFraction(), start, endColor)));
        animator.setDuration(duration);
        animator.setInterpolator(INTERPOLATOR);
        if (listenerAdapter != null) {
            animator.addListener(listenerAdapter);
        }
        return animator;
    }

    /**
     * 获取动画时间
     */
    private long getOpenAndExitAnimDuration(View thumbnailView, ShareData shareData) {
        if (shareData.config.animDuration != null) {
            return shareData.config.animDuration;
        }

        if (thumbnailView instanceof ImageView) {
            return OPEN_AND_EXIT_ANIM_DURATION_FOR_IMAGE;
        }
        return OPEN_AND_EXIT_ANIM_DURATION;
    }

    /**
     * 获取ImageView实际绘制的图片大小,如果没有设置图片，则返回数据为0。
     * 该方法调用时机不同，返回值有很大差别，如果刚设置imageView drawable，
     * 则可能返回的是drawable原图大小，而不是在imageView中实际绘制出来的大小
     */
    private void getImageActualSize(ImageView imageView, float[] size) {
        size[0] = 0;
        size[1] = 0;
        if (imageView == null || imageView.getDrawable() == null) {
            return;
        }

        Drawable drawable = imageView.getDrawable();
        // 获得ImageView中Image的真实宽高，
        int dw = drawable.getBounds().width();
        int dh = drawable.getBounds().height();

        // 获得ImageView中Image的变换矩阵
        Matrix m = imageView.getImageMatrix();
        // Image在绘制过程中的变换矩阵，从中获得x和y方向的缩放系数
        float sx = MatrixUtils.getValue(m, Matrix.MSCALE_X);
        float sy = MatrixUtils.getValue(m, Matrix.MSCALE_Y);

        // 计算Image在屏幕上实际绘制的宽高
        size[0] = dw * sx;
        size[1] = dh * sy;
    }

    /**
     * 获取当前预览图对应的缩略图View,如果未找到则查找默认位置View
     */
    @Nullable
    private View getThumbnailView(ShareData shareData) {
        View view = getThumbnailViewNotDefault(shareData, mPosition);
        if (view == null) {
            if (mPosition != shareData.config.defaultShowPosition) {
                return getThumbnailViewNotDefault(shareData, shareData.config.defaultShowPosition);
            }
        }

        return view;
    }

    /**
     * 获取指定位置的缩略图
     */
    @Nullable
    private View getThumbnailViewNotDefault(ShareData shareData, int position) {
        if (shareData.thumbnailView != null) {
            return shareData.thumbnailView;
        } else if (shareData.findThumbnailView != null) {
            return shareData.findThumbnailView.findView(position);
        }

        return null;
    }

    /**
     * 获取指定位置的略图的大小.
     * 结果存储在{@link #mSrcViewSize}
     */
    private void getSrcViewSize(View view) {
        mSrcViewSize[0] = 0;
        mSrcViewSize[1] = 0;
        mSrcViewParentSize[0] = 0;
        mSrcViewParentSize[1] = 0;

        if (view == null) {
            return;
        }

        mSrcViewSize[0] = view.getWidth();
        mSrcViewSize[1] = view.getHeight();

        mSrcViewParentSize[0] = mSrcViewSize[0];
        mSrcViewParentSize[1] = mSrcViewSize[1];
        // 暂时不处理缩略图父类比自己小的情况，主要原因是无法确定缩略图在父类布局方式
        // getSrcViewParentSize(view.getParent());
    }

    /**
     * 获取父类最小宽高，可能View设置了固定宽高，但是父类被裁剪，因此实际绘制区域可能没有设定宽高那么大
     */
    private void getSrcViewParentSize(ViewParent parent) {
        if (parent instanceof View) {
            int width = ((View) parent).getWidth();
            if (width < mSrcViewParentSize[0]) {
                mSrcViewParentSize[0] = width;
            }

            int height = ((View) parent).getHeight();
            if (height < mSrcViewParentSize[1]) {
                mSrcViewParentSize[1] = height;
            }

            if (width > mSrcViewParentSize[0] && height > mSrcViewParentSize[1]) {
                return;
            }

            getSrcViewParentSize(parent.getParent());
        }
    }

    /**
     * 获取指定位置缩略图的位置
     * 结果存储在{@link #mSrcImageLocation}
     */
    private void getSrcViewLocation(View view) {
        mSrcImageLocation[0] = 0;
        mSrcImageLocation[1] = 0;

        if (view == null) {
            return;
        }

        view.getLocationOnScreen(mSrcImageLocation);
        // 预览界面采用沉浸式全屏显示模式，如果手机系统支持，横竖屏都绘制到耳朵区域
        // 以下逻辑防止部分手机横屏时，耳朵区域不显示内容，此时设置的预览坐标不能采用OnScreen坐标
        mFragment.mRootView.getLocationOnScreen(mIntTemp);
        mSrcImageLocation[0] -= mIntTemp[0];
        mSrcImageLocation[1] -= mIntTemp[1];
    }

    /**
     * 获取指定位置的略图的背景颜色
     */
    private Drawable getSrcViewBg(View view) {
        ColorDrawable drawable = getSrcParentBg(view);
        if (drawable != null) {
            ColorDrawable colorDrawable = new ColorDrawable(drawable.getColor());
            if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
                colorDrawable.setColorFilter(drawable.getColorFilter());
            }
            colorDrawable.setAlpha(drawable.getAlpha());
            colorDrawable.setState(drawable.getState());
            return colorDrawable;
        }

        return null;
    }

    /**
     * 获取指定位置的略图的背景颜色
     */
    private ColorDrawable getSrcParentBg(View view) {
        Drawable background = view.getBackground();
        if (background instanceof ColorDrawable) {
            return (ColorDrawable) background;
        }

        ViewParent parent = view.getParent();
        if (!(parent instanceof View)) {
            return null;
        }

        return getSrcParentBg((View) parent);
    }

    /**
     * 预览界面打开时执行回调
     */
    private void callOnOpen(int... animTypes) {
        List<OnOpenListener> list = new ArrayList<>();
        if (mShareData != null && mShareData.onOpenListener != null) {
            list.add(mShareData.onOpenListener);
        }

        if (mOpenListenerList != null) {
            list.addAll(mOpenListenerList);
        }

        for (int type : animTypes) {
            if (type == ANIM_END) {
                mOpenAnimEnd = true;
                break;
            }
        }

        for (int type : animTypes) {
            for (OnOpenListener onOpenListener : list) {
                if (type == ANIM_START_PRE) {
                    onOpenListener.onStartPre();
                } else if (type == ANIM_START) {
                    onOpenListener.onStart();
                } else if (type == ANIM_END) {
                    onOpenListener.onEnd();
                }
            }
        }
    }

    /**
     * 预览界面关闭时执行回调
     */
    private void callOnExit(int... animTypes) {
        List<OnExitListener> list = new ArrayList<>();
        if (mShareData != null && mShareData.onExitListener != null) {
            list.add(mShareData.onExitListener);
        }

        if (mExitListenerList != null) {
            list.addAll(mExitListenerList);
        }

        boolean isAnimEnd = false;
        for (int type : animTypes) {
            if (type == ANIM_END) {
                isAnimEnd = true;
            }

            for (OnExitListener onExitListener : list) {
                if (type == ANIM_START_PRE) {
                    onExitListener.onStartPre();
                } else if (type == ANIM_START) {
                    onExitListener.onStart();
                } else if (type == ANIM_END) {
                    onExitListener.onExit();
                }
            }
        }

        if (isAnimEnd) {
            if (mOpenListenerList != null) {
                mOpenListenerList.clear();
            }

            if (mExitListenerList != null) {
                mExitListenerList.clear();
            }
        }
    }

    /**
     * 预览打开动画执行结束
     */
    boolean isOpenAnimEnd() {
        return mOpenAnimEnd;
    }

    /**
     * 设置当前预览图片的位置
     */
    public void setPosition(int position) {
        mPosition = position;
    }

    /**
     * 增加预览动画打开监听
     */
    void addOnOpenListener(OnOpenListener openListener) {
        if (openListener == null) {
            return;
        }

        if (mOpenListenerList == null) {
            mOpenListenerList = new ArrayList<>();
        }
        mOpenListenerList.add(openListener);
    }

    /**
     * 移除预览动画打开监听
     */
    public void removeOnOpenListener(OnOpenListener openListener) {
        if (openListener == null || mOpenListenerList == null) {
            return;
        }

        mOpenListenerList.remove(openListener);
    }

    /**
     * 增加预览动画关闭监听
     */
    public void addOnExitListener(OnExitListener onExitListener) {
        if (onExitListener == null) {
            return;
        }

        if (mExitListenerList == null) {
            mExitListenerList = new ArrayList<>();
        }
        mExitListenerList.add(onExitListener);
    }

    /**
     * 移除预览动画关闭监听
     */
    public void removeOnExitListener(OnExitListener onExitListener) {
        if (onExitListener == null || mExitListenerList == null) {
            return;
        }

        mExitListenerList.remove(onExitListener);
    }

    /**
     * 预览退出监听
     */
    public interface OnExitListener {
        /**
         * 动画开始之前数据准备阶段
         */
        void onStartPre();

        /**
         * 退出动作开始执行
         */
        void onStart();

        /**
         * 完全退出
         */
        void onExit();
    }

    /**
     * 预览打开监听
     */
    public interface OnOpenListener {
        /**
         * 动画开始之前数据准备阶段
         */
        void onStartPre();

        /**
         * 进入动画开始执行
         */
        void onStart();

        /**
         * 进入动画开始执行结束
         */
        void onEnd();
    }
}
