package com.yeastar.linkus.demo;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.hjq.toast.ToastUtils;
import com.huawei.hms.aaid.HmsInstanceId;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.support.common.ActivityMgr;
import com.vivo.push.PushClient;
import com.vivo.push.PushConfig;
import com.vivo.push.util.VivoPushException;
import com.yeastar.linkus.constant.YlsConstant;
import com.yeastar.linkus.demo.base.BaseActivityLifecycleCallbacks;
import com.yeastar.linkus.demo.call.CallManager;
import com.yeastar.linkus.demo.conference.ConferenceManager;
import com.yeastar.linkus.demo.eventbus.CallLogChangeEvent;
import com.yeastar.linkus.demo.utils.NotificationUtils;
import com.yeastar.linkus.demo.utils.Utils;
import com.yeastar.linkus.demo.widget.MToastStyle;
import com.yeastar.linkus.service.base.YlsBaseManager;
import com.yeastar.linkus.service.base.YlsInitConfig;
import com.yeastar.linkus.service.callback.RequestCallback;
import com.yeastar.linkus.service.callback.SdkCallback;

import org.greenrobot.eventbus.EventBus;

import java.lang.ref.WeakReference;

public class App extends Application {

    public static final String TAG = "App";
    private static App instance;
    private Context context;
    private boolean isBackground = true;
    //系统通话中
    private boolean isInGsmCall = false;
    private boolean isMain = false;//MainActivity是否启动
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
            String projectPath = YlsBaseManager.getInstance().getProjectPath(this);
            YlsInitConfig config = new YlsInitConfig.Builder(projectPath).key("123").supportCallWaiting(true).build();
            YlsBaseManager.getInstance().initYlsSDK(this, config);
            CallManager.getInstance().initCallBack(this);
            NotificationUtils.createNotificationChannel(this);
            registerActivityLifecycleCallbacks(new BaseActivityLifecycleCallbacks());
            //个推初始化
//            PushManager.getInstance().initialize(this);
            //vivo推送初始化
//            initVovoPush(getApplicationContext());
            //华为推送初始化
            ActivityMgr.INST.init(this);

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

                //重连成功通知
                @Override
                public void onReconnectSuccess() {
                    //个推重连成功后重新设置cid
//                    YlsBaseManager.getInstance().setPushInfo("getui", PushManager.getInstance().getClientid(getContext()), new RequestCallback() {
//                        @Override
//                        public void onSuccess(Object result) {
//
//                        }
//
//                        @Override
//                        public void onFailed(int code) {
//
//                        }
//
//                        @Override
//                        public void onException(Throwable exception) {
//
//                        }
//                    });

                    // vivo推送重连成功后重新设置regid
//                    PushClient.getInstance(context).turnOnPush(new IPushActionListener() {
//                        @Override
//                        public void onStateChanged(int state) {
//                            if (state == 0) {
//                                PushClient.getInstance(context).getRegId(new IPushQueryActionListener() {
//                                    @Override
//                                    public void onSuccess(String regid) {
//                                        //获取成功，回调参数即是当前应用的regid；
//                                        YlsBaseManager.getInstance().setPushInfo(YlsConstant.PUSH_MODE_VIVO, regid, new RequestCallback() {
//                                            @Override
//                                            public void onSuccess(Object o) {
//
//                                            }
//
//                                            @Override
//                                            public void onFailed(int i) {
//
//                                            }
//
//                                            @Override
//                                            public void onException(Throwable throwable) {
//
//                                            }
//                                        });
//                                        Log.w(TAG, "VIVO 获取regid成功，regid:" + regid);
//                                    }
//
//                                    @Override
//                                    public void onFail(Integer errerCode) {
//                                        //获取失败，可以结合错误码参考查询失败原因；
//                                        Log.w(TAG, "VIVO 获取regid失败，错误码:" + errerCode);
//                                    }
//                                });
//                            } else {
//                                Log.w(TAG, "VIVO 开关状态处理失败，错误码:" + state);
//                            }
//                        }
//                    });


                    // 华为推送重连成功后重新设置token
                    try {
                        Log.w(TAG, "HuaWei 重新获取token");
                        // 从agconnect-services.json文件中读取APP_ID
                        String appId = "100031905";
                        // 输入token标识"HCM"
                        String tokenScope = "HCM";
                        String token = HmsInstanceId.getInstance(getContext()).getToken(appId, tokenScope);
                        Log.i(TAG, "get token: " + token);

                        // 判断token是否为空
                        if(!TextUtils.isEmpty(token)) {
                            Log.w(TAG, "HuaWei 获取token成功，token:" + token);
                            YlsBaseManager.getInstance().setPushInfo(YlsConstant.PUSH_MODE_HUAWEI, token, new RequestCallback() {
                                @Override
                                public void onSuccess(Object o) {

                                }

                                @Override
                                public void onFailed(int i) {

                                }

                                @Override
                                public void onException(Throwable throwable) {

                                }
                            });
                        }

                    } catch (ApiException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            ConferenceManager.getInstance().init(this);
        }
    }

    private static void initVovoPush(Context context) {
        //初始化vivo push
        try {
            PushConfig pushConfig = new PushConfig.Builder()
                    .agreePrivacyStatement(true)
                    .build();
            PushClient.getInstance(context).initialize(pushConfig);
        } catch (VivoPushException e) {
            //此处异常说明是有必须的vpush配置未配置所致，需要仔细检查集成指南的各项配置。
            e.printStackTrace();
        }
    }

    public static App getInstance() {
        if (instance == null) {
            instance = new App();
        }
        return instance;
    }

    public boolean isMain() {
        return isMain;
    }

    public void setMain(boolean main) {
        isMain = main;
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
