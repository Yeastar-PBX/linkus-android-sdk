package com.yeastar.linkus.demo.base;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.yeastar.linkus.demo.base.fragmentBackHandler.BackHandlerHelper;
import com.yeastar.linkus.service.log.LogUtil;
import com.yeastar.linkus.demo.R;
import com.yeastar.linkus.demo.utils.StatusBarUtil;
import com.yeastar.linkus.demo.utils.ToastUtil;
import com.yeastar.linkus.demo.widget.CustomProgressDialog;
import com.yeastar.linkus.utils.NetWorkUtil;

public abstract class BaseActivity extends AppCompatActivity implements BaseActivityInterface {

    protected Activity activity = this;
    protected int layoutResID;
    private CustomProgressDialog progressDialog = null;
    protected String TAG = this.getClass().getSimpleName();

    public BaseActivity(int layoutResID) {
        this.layoutResID = layoutResID;
    }

    public void beforeCreate(){

    }

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        beforeCreate();
        super.onCreate(savedInstanceState);
        try {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } catch (Exception e) {
            LogUtil.i("setRequestedOrientation:%s", e.toString());
        }
        // 设置进入动画
        overridePendingTransition(R.anim.anim_activity_open_enter, R.anim.anim_activity_open_exit);
        beforeSetView();
        setContentView(layoutResID);
        setStatusBarColor();
        findView();
    }

    protected void setStatusBarColor() {
        int mode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && mode == Configuration.UI_MODE_NIGHT_NO) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        int statusBarColor;
        if (mode == Configuration.UI_MODE_NIGHT_YES) {
            statusBarColor = ContextCompat.getColor(this, android.R.color.transparent);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setNavigationBarColor(statusBarColor);
            }
        } else {
            statusBarColor = ContextCompat.getColor(this, R.color.white);
            int navigationBarColor = ContextCompat.getColor(this, R.color.black);
            getWindow().setNavigationBarColor(navigationBarColor);
        }
        StatusBarUtil.setColor(activity, statusBarColor, 1);
    }

    public void showToast(int textId) {
        if (activity != null) {
            ToastUtil.showToast(textId);
        }
    }

    public void showToastWithView(int msgId, int layoutId) {
        if (activity != null) {
            ToastUtil.showToastView(msgId, layoutId);
        }
    }

    public void showToast(int textId, Object... args) {
        if (activity != null) {
            String text = activity.getString(textId, args);
            ToastUtil.showToast(text);
        }
    }

    protected void showToast(String text) {
        if (activity != null) {
            ToastUtil.showToast(text);
        }
    }

    protected void showLongToast(int textId) {
        if (activity != null) {
            ToastUtil.showLongToast(textId);
        }
    }

    protected boolean isNetworkConnected() {
        return isNetworkConnected(true);
    }

    protected boolean isNetworkConnected(boolean showToast) {
        boolean networkConnected = NetWorkUtil.isNetworkConnected(this);
        if (activity != null && !networkConnected && showToast) {
            ToastUtil.showLongToast(R.string.nonetworktip_error);
        }
        return networkConnected;
    }

    public void showProgressDialog(int textId) {
        String text = getString(textId);
        showProgressDialog(text);
    }

    public void showProgressDialog(String text) {
        if (progressDialog == null) {
            progressDialog = new CustomProgressDialog(activity, text, true);
        }
        if (!isDestroyed() && !isFinishing()) {
            progressDialog.show();
        }
    }

    public void showLargeProgressDialog(int textId) {
        String text = getString(textId);
        showLargeProgressDialog(text);
    }

    public void showLargeProgressDialog(String text) {
        if (progressDialog == null) {
            progressDialog = new CustomProgressDialog(activity, CustomProgressDialog.TYPE_TEXT_MULTIPLE, text, true);
        }
        if (!isDestroyed() && !isFinishing()) {
            progressDialog.show();
        }
    }

    public void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new CustomProgressDialog(activity, "", false);
        }
        if (!isDestroyed() && !isFinishing()) {
            progressDialog.show();
        }
    }

    public void updateProgressDialog(String text) {
        if (progressDialog != null) {
            progressDialog.setText(text);
        }
    }

    public void closeProgressDialog() {
        if (isDestroyed() || isFinishing()) {
            return;
        }
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    public void closeProgressDialogDelay() {
        new Handler().postDelayed(() -> {
            if (isDestroyed() || isFinishing()) {
                return;
            }
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }
        }, 700);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeProgressDialog();
    }

    public BaseFragment switchContent(BaseFragment fragment) {
        return switchContent(fragment, null);
    }

    public BaseFragment switchContent(BaseFragment fragment, String tag) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        //开启堆栈优化
        fragmentTransaction.setReorderingAllowed(true);
        if (tag != null) {
            fragmentTransaction.addToBackStack(tag);
        }
        fragmentTransaction.replace(fragment.getContainerId(), fragment);

        try {
            fragmentTransaction.commit();
            LogUtil.w("switchContent commit");
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e(e,"switchContent");
        }

        return fragment;
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //解决9.0上部分手机无法自动触发recreate()方法
        LogUtil.i("onConfigurationChanged activityName=%s", this.getLocalClassName());
        recreate();
    }

    protected void showKeyboard(boolean isShow) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            if (isShow) {
                if (getCurrentFocus() == null) {
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                } else {
                    imm.showSoftInput(getCurrentFocus(), 0);
                }
            } else {
                if (getCurrentFocus() != null) {
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }
        }
    }

    /**
     * 延时弹出键盘
     *
     * @param focus 键盘的焦点项
     */
    protected void showKeyboardDelayed(View focus) {
        final View viewToFocus = focus;
        if (focus != null) {
            focus.requestFocus();
        }
        new Handler(getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (viewToFocus == null || viewToFocus.isFocused()) {
                    showKeyboard(true);
                }
            }
        }, 200);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        AudioManager audioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_VOLUME_UP:
                    if (audioManager.getMode() == AudioManager.MODE_NORMAL) {
                        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE,
                                AudioManager.FLAG_SHOW_UI);
                    } else if (audioManager.getMode() == AudioManager.MODE_IN_COMMUNICATION) {
                        audioManager.adjustStreamVolume(AudioManager.STREAM_VOICE_CALL, AudioManager.ADJUST_RAISE,
                                AudioManager.FLAG_SHOW_UI);
                    }
                    return true;
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    if (audioManager.getMode() == AudioManager.MODE_NORMAL) {
                        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER,
                                AudioManager.FLAG_SHOW_UI);
                    } else if (audioManager.getMode() == AudioManager.MODE_IN_COMMUNICATION) {
                        audioManager.adjustStreamVolume(AudioManager.STREAM_VOICE_CALL, AudioManager.ADJUST_LOWER,
                                AudioManager.FLAG_SHOW_UI);
                    }
                    return true;
                default:
                    return super.onKeyDown(keyCode, event);
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        if (!BackHandlerHelper.handleBackPress(this)) {
            super.onBackPressed();
        }
    }


    // 重新finish方法
    @Override
    public void finish() {
        super.finish();
        // 设置退出动画
        overridePendingTransition(R.anim.anim_activity_close_enter, R.anim.anim_activity_close_exit);
    }
}