package com.yeastar.linkus.demo.call;

import android.os.Build;
import android.view.View;
import android.widget.TextView;


import com.yeastar.linkus.demo.App;
import com.yeastar.linkus.demo.R;
import com.yeastar.linkus.demo.eventbus.NetWorkLevelEvent;
import com.yeastar.linkus.service.log.LogUtil;
import com.yeastar.linkus.demo.base.BaseFragment;
import com.yeastar.linkus.demo.utils.StatusBarUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by lwh on 18-5-8.
 * 通话相关界面用于转移过程中的出栈操作
 */

public abstract class InCallRelatedFragment extends BaseFragment {

    protected TextView txLevelTv;

    public InCallRelatedFragment(int layoutResID) {
        super(layoutResID);
    }

    @Override
    public void findView(View parent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            initStatusBarPadding(parent);
        }
        txLevelTv = parent.findViewById(R.id.tv_tx_level);
        if (!EventBus.getDefault().isRegistered(this)) {
            LogUtil.i("life:" +"注册事件"+TAG);
            EventBus.getDefault().register(this);
        }
        StatusBarUtil.setDarkMode(activity);
    }

    private void initStatusBarPadding(View parent) {
        int height = 0;
        int resourceId = App.getInstance().getContext().getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            height = App.getInstance().getContext().getResources().getDimensionPixelSize(resourceId);
        }
        parent.setPadding(0, height, 0, 0);
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void handlerCallStatus(NetWorkLevelEvent netWorkLevelEvent) {
        if (txLevelTv == null) return;
        CharSequence text = txLevelTv.getText();
        int visibility = txLevelTv.getVisibility();
        txLevelTv.setText(R.string.call_audio);
        int networkLevel = netWorkLevelEvent.getNetworkLevel();
        if (networkLevel < 3) {
            txLevelTv.setVisibility(View.VISIBLE);
        } else {
            txLevelTv.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
        LogUtil.i("life:" +"取消事件"+TAG);
    }
}
