package com.haokan.mytestimageview.custom.listener;

/**
 * 退出动画时监听
 * 当前viewpager的选中的position
 * 当前作品在流里面的位置
 */
public interface OnPagerItemPositionListener {
    /**
     *
     * @param position 在里面的position 位置
     * @param innerPosition viewpager 内部的 position
     */
    void onItemPositionWithInnerPosition(int position, int innerPosition);

}
