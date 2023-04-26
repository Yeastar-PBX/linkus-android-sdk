package com.yeastar.linkus.demo.utils;

import android.view.View;

/**
 * view 防止重复点击处理方案
 */

public abstract class OnNoDoubleClickListener implements View.OnClickListener {

    // 0.9秒内防止多次点击
    public static final int MIN_CLICK_DELAY_TIME = 900;
    private long lastClickTime = 0;
    private long minDelayTime = -1;

    @Override
    public void onClick(View v) {
        long currentTime = System.currentTimeMillis();
        if (minDelayTime == -1) {
            minDelayTime = MIN_CLICK_DELAY_TIME;
        }
        if (currentTime - lastClickTime > minDelayTime) {
            lastClickTime = currentTime;
            onNoDoubleClick(v);
        }
    }

    public OnNoDoubleClickListener(long minDelayTime) {
        this.minDelayTime = minDelayTime;
    }

    public OnNoDoubleClickListener() {
    }

    // 用户需要进一步实现
    protected abstract void onNoDoubleClick(View v);

}
