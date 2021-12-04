package com.haokan.mytestimageview.custom.preview;

import androidx.annotation.IntDef;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 图片指示器类型
 *
 * @author Created by wanggaowan on 2019/3/6 0006 17:15
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.SOURCE)
@IntDef({IndicatorType.DOT, IndicatorType.TEXT})
public @interface IndicatorType {
    /**
     * 圆点,如果图片多于{@link Config#maxIndicatorDot}则采用{@link #TEXT}
     */
    int DOT = 0;
    
    /**
     * 文本
     */
    int TEXT = 1;
}
