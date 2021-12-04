package com.haokan.mytestimageview.custom.preview;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.text.SpannableString;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle.State;
import androidx.viewpager.widget.ViewPager.SimpleOnPageChangeListener;

import com.haokan.mytestimageview.R;


/**
 * 预览界面根布局
 */
@RestrictTo(Scope.LIBRARY)
public class PreviewDialogFragment extends DialogFragment {

    static final String FRAGMENT_TAG = "PhotoPreview:59bd2d0f-8474-451d-9bee-3cca00182b31";

    FrameLayout mRootView;
    NoTouchExceptionViewPager mViewPager;
    //private LinearLayout mLlDotIndicator;
    //private ImageView mIvSelectDot;
    private TextView mTvTextIndicator;
    private FrameLayout mLlCustom;

    /**
     * 用于当前Fragment与预览Fragment之间的通讯
     */
    @NonNull
    ShareData mShareData;

    /**
     * 当前展示预览图下标
     */
    private int mCurrentPagerIndex = 0;

    /**
     * 是否添加到Activity
     */
    private boolean mAdd;

    /**
     * 是否已经Dismiss
     */
    private boolean mDismiss;

    /**
     * 界面关闭时是否需要调用{@link OnDismissListener}
     */
    private boolean mCallOnDismissListener = true;

    /**
     * 是否在当前界面OnDismiss调用{@link OnDismissListener}
     */
    private boolean mCallOnDismissListenerInThisOnDismiss;

    /**
     * 是否自己主动调用Dismiss(包括用户主动关闭、程序主动调用dismiss相关方法)
     */
    private Boolean mSelfDismissDialog;
    private PhotoPreviewHelper mPhotoPreviewHelper;

    public PreviewDialogFragment() {
        setCancelable(false);
        // 全屏处理
        setStyle(STYLE_NO_TITLE, 0);
        mShareData = new ShareData();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // 说明是被回收后恢复，此时不恢复
            super.onActivityCreated(savedInstanceState);
            return;
        }

        if (getDialog() == null || getDialog().getWindow() == null) {
            super.onActivityCreated(null);
            return;
        }

        Window window = getDialog().getWindow();
        // 无论是否全屏显示，都允许内容绘制到耳朵区域
        //NotchAdapterUtils.adapter(window, CutOutMode.ALWAYS);
        super.onActivityCreated(null);

        // 以下代码必须在super.onActivityCreated之后调用才有效
        boolean isParentFullScreen = isParentFullScreen();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            // 需要设置这个才能设置状态栏和导航栏颜色，此时布局内容可绘制到状态栏之下
            window.addFlags(LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
        }

        LayoutParams lp = window.getAttributes();
        lp.dimAmount = 0;
        lp.flags |= LayoutParams.FLAG_DIM_BEHIND;

        if (mShareData.config.fullScreen == null) {
            // 跟随父窗口
            if (isParentFullScreen) {
                lp.flags |= LayoutParams.FLAG_FULLSCREEN;
            } else {
                lp.flags |= LayoutParams.FLAG_FORCE_NOT_FULLSCREEN;
            }
        }

        lp.width = LayoutParams.MATCH_PARENT;
        lp.height = LayoutParams.MATCH_PARENT;
        window.setAttributes(lp);

        // 沉浸式处理
        // OPPO ANDROID P 之后的系统需要设置沉浸式配合异形屏适配才能将内容绘制到耳朵区域
        // 防止系统栏隐藏时内容区域大小发生变化
        int uiFlags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        // window全屏显示，但状态栏不会被隐藏，状态栏依然可见，内容可绘制到状态栏之下
        uiFlags |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        // window全屏显示，但导航栏不会被隐藏，导航栏依然可见，内容可绘制到导航栏之下
        uiFlags |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        if (VERSION.SDK_INT >= VERSION_CODES.KITKAT) {
            // 对于OPPO ANDROID P 之后的系统,一定需要清除此标志，否则异形屏无法绘制到耳朵区域下面
            window.clearFlags(LayoutParams.FLAG_TRANSLUCENT_STATUS);
            // 设置之后不会通过触摸屏幕调出导航栏
            // uiFlags |= View.SYSTEM_UI_FLAG_IMMERSIVE; // 通过系统上滑或者下滑拉出导航栏后不会自动隐藏
            uiFlags |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY; // 通过系统上滑或者下滑拉出导航栏后会自动隐藏
        }

        if (mShareData.config.fullScreen == null && isParentFullScreen) {
            // 隐藏状态栏
            uiFlags |= View.INVISIBLE;
        }

        window.getDecorView().setSystemUiVisibility(uiFlags);
        window.getDecorView().setPadding(0, 0, 0, 0);
    }

    /**
     * 初始化是否全屏展示
     */
    private void initFullScreen(boolean start) {
        if (mShareData.config.fullScreen == null) {
            return;
        }

        boolean isParentFullScreen = isParentFullScreen();
        if (isParentFullScreen == mShareData.config.fullScreen) {
            return;
        }

        Dialog dialog = getDialog();
        if (dialog == null) {
            return;
        }

        Window window = dialog.getWindow();
        if (window == null) {
            return;
        }

        if (start) {
            if (mShareData.config.fullScreen) {
                window.clearFlags(LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                window.addFlags(LayoutParams.FLAG_FULLSCREEN);
                View decorView = window.getDecorView();
                decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() | View.INVISIBLE);
            } else {
                window.clearFlags(LayoutParams.FLAG_FULLSCREEN);
                window.addFlags(LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            }
        } else if (isParentFullScreen()) {
            window.clearFlags(LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            window.addFlags(LayoutParams.FLAG_FULLSCREEN);
            View decorView = window.getDecorView();
            decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() | View.INVISIBLE);
        } else {
            window.clearFlags(LayoutParams.FLAG_FULLSCREEN);
            window.addFlags(LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        }
    }

    @SuppressLint("InflateParams")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = (FrameLayout) inflater.inflate(R.layout.view_preview_root, null);
            mViewPager = mRootView.findViewById(R.id.viewpager);
            //mLlDotIndicator = mRootView.findViewById(R.id.ll_dot_indicator_photo_preview);
            //mIvSelectDot = mRootView.findViewById(R.id.iv_select_dot_photo_preview);
            mTvTextIndicator = mRootView.findViewById(R.id.tv_text_indicator_photo_preview);
            mLlCustom = mRootView.findViewById(R.id.fl_custom);
        }

        if (mSelfDismissDialog == null && savedInstanceState == null) {
            initEvent();
            initViewData();
            mDismiss = false;
        } else if (savedInstanceState != null || !mSelfDismissDialog) {
            // 被回收后恢复，则关闭弹窗
            dismissAllowingStateLoss();
        }

        return mRootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mLlCustom.removeAllViews();
        if (mRootView != null) {
            ViewParent parent = mRootView.getParent();
            if (parent instanceof ViewGroup) {
                // 为了下次重用mRootView
                ((ViewGroup) parent).removeView(mRootView);
            }
        }

        if (mSelfDismissDialog == null) {
            mSelfDismissDialog = false;
        }
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        mSelfDismissDialog = null;
        mAdd = false;
        mDismiss = true;

        if (mShareData.config.onDismissListener != null
                && mCallOnDismissListenerInThisOnDismiss
                && mCallOnDismissListener) {
            mShareData.config.onDismissListener.onDismiss();
        }
        mShareData.release();
    }

    /**
     * 父窗口是否全屏显示
     */
    boolean isParentFullScreen() {
        FragmentActivity activity = getActivity();
        if (activity == null || activity.getWindow() == null) {
            return true;
        }

        // 跟随打开预览界面的显示状态
        return (activity.getWindow().getAttributes().flags & LayoutParams.FLAG_FULLSCREEN) != 0;
    }

    public void show(Context context, FragmentManager fragmentManager, Config config, View thumbnailView) {
        mShareData.applyConfig(config);
        mShareData.findThumbnailView = null;
        mShareData.thumbnailView = thumbnailView;
        showInner(context, fragmentManager);
    }

    /**
     *
     * @param context
     * @param fragmentManager
     * @param config
     * @param findThumbnailView 获取指定位置的缩略图
     */
    public void show(Context context, FragmentManager fragmentManager, Config config, IFindThumbnailView findThumbnailView) {
        mShareData.applyConfig(config);
        mShareData.thumbnailView = null;
        mShareData.findThumbnailView = findThumbnailView;
        showInner(context, fragmentManager);
    }

    private void showInner(Context context, FragmentManager fragmentManager) {
        // 预加载启动图图片
        PreloadImageView imageView = new PreloadImageView(context);
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(displayMetrics.widthPixels, displayMetrics.heightPixels);
        imageView.setLayoutParams(params);
        imageView.setDrawableLoadListener(drawable -> {
            mShareData.preLoadDrawable = drawable;
            PreloadImageView.DrawableLoadListener listener = mShareData.preDrawableLoadListener;
            if (listener != null) {
                listener.onLoad(drawable);
            }
        });
        loadImage(imageView);

        mSelfDismissDialog = null;
        mShareData.showNeedAnim = getDialog() == null || !getDialog().isShowing();

        if (isStateSaved()) {
            dismissAllowingStateLoss();
        } else if (isAdded() || mAdd) {
            // isAdded()并不一定靠谱，可能存在一定的延时性，为此当前对象在创建时，已经优先返回fragmentManager存在的对象
            // 对象获取逻辑查看PhotoPreview getDialog相关方法
            if (!getLifecycle().getCurrentState().isAtLeast(State.INITIALIZED)) {
                dismissAllowingStateLoss();
            } else if (mRootView != null) {
                initViewData();
                initEvent();
                return;
            }
        }

        mAdd = true;
        showNow(fragmentManager, FRAGMENT_TAG);
    }

    /**
     * 加载图片
     */
    private void loadImage(ImageView imageView) {
        if (mShareData.config.imageLoader != null) {
            int mPosition = mShareData.config.defaultShowPosition;
            if (mShareData.config.sources != null && mPosition < mShareData.config.sources.size() && mPosition >= 0) {
                mShareData.config.imageLoader.onLoadImage(mPosition, mShareData.config.sources.get(mPosition), imageView);
            } else {
                mShareData.config.imageLoader.onLoadImage(mPosition, null, imageView);
            }
        }
    }

    /**
     * 退出预览
     *
     * @param callBack 是否需要执行{@link OnDismissListener}回调
     */
    public void dismiss(boolean callBack) {
        if (mSelfDismissDialog != null || mDismiss || !getLifecycle().getCurrentState().isAtLeast(State.CREATED)) {
            return;
        }

        mSelfDismissDialog = true;
        mCallOnDismissListener = callBack;
        if (mPhotoPreviewHelper == null) {
            mCallOnDismissListenerInThisOnDismiss = true;
            dismissAllowingStateLoss();
        } else {
            boolean exit = mPhotoPreviewHelper.exit();
            if (!exit) {
                mCallOnDismissListenerInThisOnDismiss = true;
                dismissAllowingStateLoss();
            }
        }
    }

    private void initViewData() {
        mCurrentPagerIndex = mShareData.config.defaultShowPosition;
        mPhotoPreviewHelper = new PhotoPreviewHelper(this, mCurrentPagerIndex);

        //mLlDotIndicator.setVisibility(View.GONE);
        //mIvSelectDot.setVisibility(View.GONE);
        mTvTextIndicator.setVisibility(View.GONE);
        //setIndicatorVisible(false);

        //prepareIndicator();
        prepareViewPager();
    }

    private void initEvent() {
        mShareData.onOpenListener = new PhotoPreviewHelper.OnOpenListener() {

            @Override
            public void onStartPre() {
                if (mShareData.config.openAnimStartHideOrShowStatusBar) {
                    initFullScreen(true);
                }
            }

            @Override
            public void onStart() {
                // 对于强制指定是否全屏，需要此处初始化状态栏隐藏逻辑，否则在MIUI系统上，从嵌套多层的Fragment预览会出现卡顿
                mViewPager.setTouchEnable(false);
            }

            @Override
            public void onEnd() {
                if (!mShareData.config.openAnimStartHideOrShowStatusBar) {
                    initFullScreen(true);
                }
                //setIndicatorVisible(true);
                mViewPager.setTouchEnable(true);
            }
        };

        mShareData.onExitListener = new PhotoPreviewHelper.OnExitListener() {
            @Override
            public void onStartPre() {
                if (mShareData.config.exitAnimStartHideOrShowStatusBar) {
                    initFullScreen(false);
                }
            }

            @Override
            public void onStart() {
                //setIndicatorVisible(false);
                mViewPager.setTouchEnable(false);
            }

            @Override
            public void onExit() {
                if (!mShareData.config.exitAnimStartHideOrShowStatusBar) {
                    initFullScreen(false);
                }
                mViewPager.setTouchEnable(true);
                if (mSelfDismissDialog != null) {
                    return;
                }

                mSelfDismissDialog = true;
                OnDismissListener onDismissListener = mShareData.config.onDismissListener;
                dismissAllowingStateLoss();
                if (onDismissListener != null && mCallOnDismissListener) {
                    onDismissListener.onDismiss();
                }
            }
        };

        mShareData.onLongClickListener = v -> {
            if (mShareData.config.onLongClickListener != null) {
                return mShareData.config.onLongClickListener.onLongClick(mLlCustom);
            }
            return false;
        };
    }

    /**
     * 准备用于展示预览图的ViePager数据
     */
    private void prepareViewPager() {
        // 每次预览的时候，如果不动态修改每个ViewPager的Id
        // 那么预览多张图片时，如果第一次点击位置1预览然后关闭，再点击位置2，预览图片打开的还是位置1预览图
        mViewPager.setTouchEnable(false);
        if (mViewPager.getId() == R.id.view_pager_id) {
            mViewPager.setId(R.id.view_pager_id_next);
        } else {
            mViewPager.setId(R.id.view_pager_id);
        }

        ImagePagerAdapter adapter = new ImagePagerAdapter(mPhotoPreviewHelper, mShareData);
        mViewPager.addOnPageChangeListener(new SimpleOnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //if (mLlDotIndicator.getVisibility() == View.VISIBLE) {
                //    float dx = mLlDotIndicator.getChildAt(1).getX() - mLlDotIndicator.getChildAt(0).getX();
                //    //mIvSelectDot.setTranslationX((position * dx) + positionOffset * dx);
                //}
            }

            @Override
            public void onPageSelected(int position) {
                mCurrentPagerIndex = position;
                mPhotoPreviewHelper.setPosition(position);

                // 设置文字版本当前页的值
                if (mTvTextIndicator.getVisibility() == View.VISIBLE) {
                    updateTextIndicator();
                }
            }
        });

        mViewPager.setAdapter(adapter);
        mViewPager.setCurrentItem(mCurrentPagerIndex);
    }

    /**
     * 准备滑动指示器数据
     */
    //private void prepareIndicator() {
    //    int sourceSize = mShareData.config.sources == null ? 0 : mShareData.config.sources.size();
    //    if (sourceSize >= 2 && sourceSize <= mShareData.config.maxIndicatorDot
    //        && IndicatorType.DOT == mShareData.config.indicatorType) {
    //        mLlDotIndicator.removeAllViews();
    //
    //        Context context = requireContext();
    //        if (mShareData.config.selectIndicatorColor != 0xFFFFFFFF) {
    //            //Drawable drawable = mIvSelectDot.getDrawable();
    //            //GradientDrawable gradientDrawable;
    //            //if (drawable instanceof GradientDrawable) {
    //            //    gradientDrawable = (GradientDrawable) drawable;
    //            //} else {
    //            //    gradientDrawable = (GradientDrawable) ContextCompat.getDrawable(context, R.drawable.selected_dot);
    //            //}
    //            //
    //            //Objects.requireNonNull(gradientDrawable).setColorFilter(mShareData.config.selectIndicatorColor, Mode.SRC_OVER);
    //            //mIvSelectDot.setImageDrawable(gradientDrawable);
    //        }
    //
    //        final LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(
    //            ViewGroup.LayoutParams.WRAP_CONTENT,
    //            ViewGroup.LayoutParams.WRAP_CONTENT);
    //
    //        // 未选中小圆点的间距
    //        dotParams.rightMargin = Utils.dp2px(context, 12);
    //
    //        // 创建未选中的小圆点
    //        for (int i = 0; i < sourceSize; i++) {
    //            AppCompatImageView iv = new AppCompatImageView(context);
    //            GradientDrawable shapeDrawable = (GradientDrawable) ContextCompat.getDrawable(context, R.drawable.no_selected_dot);
    //            if (mShareData.config.normalIndicatorColor != 0xFFAAAAAA) {
    //                Objects.requireNonNull(shapeDrawable).setColorFilter(mShareData.config.normalIndicatorColor, Mode.SRC_OVER);
    //            }
    //            iv.setImageDrawable(shapeDrawable);
    //            iv.setLayoutParams(dotParams);
    //            mLlDotIndicator.addView(iv);
    //        }
    //
    //        mLlDotIndicator.post(() -> {
    //            View childAt = mLlDotIndicator.getChildAt(0);
    //            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mIvSelectDot.getLayoutParams();
    //            // 设置选中小圆点的左边距
    //            params.leftMargin = (int) childAt.getX();
    //            mIvSelectDot.setLayoutParams(params);
    //            float tx = (dotParams.rightMargin * mCurrentPagerIndex + childAt.getWidth() * mCurrentPagerIndex);
    //            mIvSelectDot.setTranslationX(tx);
    //        });
    //    } else if (sourceSize > 1) {
    //        updateTextIndicator();
    //    }
    //}

    //private void setIndicatorVisible(boolean visible) {
    //    int sourceSize = mShareData.config.sources == null ? 0 : mShareData.config.sources.size();
    //    if (sourceSize >= 2 && sourceSize <= mShareData.config.maxIndicatorDot
    //        && IndicatorType.DOT == mShareData.config.indicatorType) {
    //        int visibility = visible ? View.VISIBLE : View.INVISIBLE;
    //        mLlDotIndicator.setVisibility(visibility);
    //        mIvSelectDot.setVisibility(visibility);
    //        mTvTextIndicator.setVisibility(View.GONE);
    //    } else if (sourceSize > 1) {
    //        mLlDotIndicator.setVisibility(View.GONE);
    //        mIvSelectDot.setVisibility(View.GONE);
    //        mTvTextIndicator.setVisibility(visible ? View.VISIBLE : View.GONE);
    //    } else {
    //        mLlDotIndicator.setVisibility(View.GONE);
    //        mIvSelectDot.setVisibility(View.GONE);
    //        mTvTextIndicator.setVisibility(View.GONE);
    //    }
    //}
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //if (mLlDotIndicator.getVisibility() == View.VISIBLE) {
        //    mLlDotIndicator.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
        //        @Override
        //        public void onGlobalLayout() {
        //            //mLlDotIndicator.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        //            //
        //            //View childAt = mLlDotIndicator.getChildAt(0);
        //            //FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mIvSelectDot.getLayoutParams();
        //            //// 设置选中小圆点的左边距
        //            //params.leftMargin = (int) childAt.getX();
        //            //mIvSelectDot.setLayoutParams(params);
        //            //LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) childAt.getLayoutParams();
        //            //float tx = (layoutParams.rightMargin * mCurrentPagerIndex + childAt.getWidth() * mCurrentPagerIndex);
        //            //mIvSelectDot.setTranslationX(tx);
        //        }
        //    });
        //}
    }

    private void updateTextIndicator() {
        mTvTextIndicator.setVisibility(View.VISIBLE);
        int sourceSize = mShareData.config.sources == null ? 0 : mShareData.config.sources.size();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.valueOf(mCurrentPagerIndex + 1))
                .append(" / " + sourceSize);
        mTvTextIndicator.setText(stringBuilder.toString());
        //SpannableString.Builder.appendMode()
        //    .addSpan(String.valueOf(mCurrentPagerIndex + 1))
        //    .color(mShareData.config.selectIndicatorColor)
        //    .addSpan(" / " + sourceSize)
        //    .color(mShareData.config.normalIndicatorColor)
        //    .apply(mTvTextIndicator);
    }
}
