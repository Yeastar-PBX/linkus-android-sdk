package com.yeastar.linkus.demo.base.fragmentBackHandler;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

public abstract class BackHandledFragment extends Fragment implements FragmentBackHandler {
    public BackHandledFragment() {
    }

    @Override
    public final boolean onBackPressed() {
        return interceptBackPressed()
                || (getBackHandleViewPager() == null
                ? BackHandlerHelper.handleBackPress(this)
                : BackHandlerHelper.handleBackPress(getBackHandleViewPager()));
    }

    public boolean interceptBackPressed() {
        return false;
    }

    /**
     * 2.1 版本已经不在需要单独对ViewPager处理
     *
     * @deprecated
     */
    @Deprecated
    public ViewPager getBackHandleViewPager() {
        return null;
    }
}
