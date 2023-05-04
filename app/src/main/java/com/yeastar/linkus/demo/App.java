package com.yeastar.linkus.demo;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.hjq.toast.ToastUtils;
import com.igexin.sdk.PushManager;
import com.yeastar.linkus.demo.base.BaseActivityLifecycleCallbacks;
import com.yeastar.linkus.demo.call.CallManager;
import com.yeastar.linkus.demo.eventbus.CallLogChangeEvent;
import com.yeastar.linkus.demo.utils.NotificationUtils;
import com.yeastar.linkus.demo.utils.Utils;
import com.yeastar.linkus.demo.widget.MToastStyle;
import com.yeastar.linkus.service.base.YlsBaseManager;
import com.yeastar.linkus.service.callback.SdkCallback;

import org.greenrobot.eventbus.EventBus;

import java.lang.ref.WeakReference;

public class App extends Application {

    private static App instance;
    private Context context;
    private boolean isBackground = true;
    //系统通话中
    private boolean isInGsmCall = false;
    private WeakReference<Activity> mCurrentActivity;

    public Context getContext() {
        return context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (Utils.isMainProcesses(this)) {
            context = this.getApplicationContext();
            instance = this;
            YlsBaseManager.getInstance().initYlsSDK(this, null);
            CallManager.getInstance().initCallBack(this);
            NotificationUtils.createNotificationChannel(this);
            registerActivityLifecycleCallbacks(new BaseActivityLifecycleCallbacks());
            //个推初始化
            PushManager.getInstance().initialize(this);
            ToastUtils.init(this, new MToastStyle());
            YlsBaseManager.getInstance().setSdkCallback(new SdkCallback() {
                //cdr变更通知
                @Override
                public void onCdrChange(int syncResult) {
                    EventBus.getDefault().post(new CallLogChangeEvent(syncResult));
                }
                //退出登录通知
                @Override
                public void onLogout(int type) {
                    Intent intent = new Intent(context, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            });
        }
    }

    public static App getInstance() {
        if (instance == null) {
            instance = new App();
        }
        return instance;
    }

    public boolean isBackground() {
        return isBackground;
    }

    public void setBackground(boolean background) {
        isBackground = background;
    }

    public boolean isInGsmCall() {
        return isInGsmCall;
    }

    public void setInGsmCall(boolean gsmCall) {
        isInGsmCall = gsmCall;
    }

    public void setmCurrentActivity(WeakReference<Activity> mCurrentActivity) {
        this.mCurrentActivity = mCurrentActivity;
    }

    public Activity getCurrentActivity() {
        if (mCurrentActivity != null) {
            return mCurrentActivity.get();
        }
        return null;
    }

}
