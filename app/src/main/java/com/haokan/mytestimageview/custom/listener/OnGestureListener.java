/*
 Copyright 2011, 2012 Chris Banes.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package com.haokan.mytestimageview.custom.listener;

import android.view.View;
import android.widget.FrameLayout;

public interface OnGestureListener {
    
    void onDrag(float dx, float dy);
    
    void onFling(float startX, float startY, float velocityX,
                 float velocityY);
    
    void onScale(float scaleFactor, float focusX, float focusY);

    /**
     * call when preview view long press
     *
     * @author Created by wanggaowan on 2019/3/6 0006 17:19
     */
    interface OnLongClickListener {
        /**
         * 长按，可添加自定义处理选项，比如保存图片、分享等
         *
         * @param rootView 当前预览根布局,默认显示状态为{@link View#GONE}
         */
        boolean onLongClick(FrameLayout rootView);
    }
}