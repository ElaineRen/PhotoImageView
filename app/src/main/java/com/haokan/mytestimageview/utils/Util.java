package com.haokan.mytestimageview.utils;

import android.content.Context;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;

/**
 * @author Created by wanggaowan on 2019/2/28 0028 13:59
 */
public class Util {
    public static int dp2px(Context context, int dipValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
            dipValue, context.getResources().getDisplayMetrics());
    }
    
    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources()
            .getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
    
    /**
     * 是否是沉浸式状态栏或无状态栏，此种情况都无需处理状态栏导致的偏移值
     */
    public static boolean isImmersionBar(Window window) {
        if ((window.getAttributes().flags & LayoutParams.FLAG_FULLSCREEN) == LayoutParams.FLAG_FULLSCREEN) {
            return true;
        }
        
        View decorView = window.getDecorView();
        if ((decorView.getSystemUiVisibility() & View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN) == View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN) {
            return true;
        } else if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN) {
            return (decorView.getSystemUiVisibility() & View.SYSTEM_UI_FLAG_LAYOUT_STABLE) == View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        }
        
        return false;
    }

  public   static void checkZoomLevels(float minZoom, float midZoom,
                                float maxZoom) {
        if (minZoom >= midZoom) {
            throw new IllegalArgumentException(
                    "Minimum zoom has to be less than Medium zoom. Call setMinimumZoom() with a more appropriate value");
        } else if (midZoom >= maxZoom) {
            throw new IllegalArgumentException(
                    "Medium zoom has to be less than Maximum zoom. Call setMaximumZoom() with a more appropriate value");
        }
    }

    public static boolean hasDrawable(ImageView imageView) {
        return imageView.getDrawable() != null;
    }

    public  static boolean isSupportedScaleType(final ImageView.ScaleType scaleType) {
        if (scaleType == null) {
            return false;
        }
        switch (scaleType) {
            case MATRIX:
                throw new IllegalStateException("Matrix scale type is not supported");
        }
        return true;
    }

    public   static int getPointerIndex(int action) {
        return (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
    }
}
