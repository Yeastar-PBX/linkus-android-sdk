package com.yeastar.linkus.demo.base;

import static android.content.Context.TELEPHONY_SERVICE;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyCallback;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.yeastar.linkus.demo.App;
import com.yeastar.linkus.demo.Constant;
import com.yeastar.linkus.demo.call.CallContainerActivity;
import com.yeastar.linkus.demo.call.CallManager;
import com.yeastar.linkus.demo.utils.NotificationUtils;
import com.yeastar.linkus.demo.utils.Utils;
import com.yeastar.linkus.service.call.YlsCallManager;
import com.yeastar.linkus.service.call.vo.InCallVo;
import com.yeastar.linkus.service.log.LogUtil;
import com.yeastar.linkus.utils.CommonUtil;
import com.yeastar.linkus.utils.ThreadPoolManager;

import org.greenrobot.eventbus.EventBus;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.concurrent.FutureTask;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class BaseActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {

    private int foregroundCount = 0;

    private void registerCallStateListener() {
        if (!callStateListenerRegistered) {
            TelephonyManager telephonyManager = (TelephonyManager) App.getInstance().getContext().getSystemService(TELEPHONY_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ContextCompat.checkSelfPermission(App.getInstance().getContext(), Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                    telephonyManager.registerTelephonyCallback(App.getInstance().getContext().getMainExecutor(), callStateListener);
                    callStateListenerRegistered = true;
                }
            } else {
                telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
                callStateListenerRegistered = true;
            }
        }
    }

    private void unRegisterCallStateListener() {
        if (callStateListenerRegistered) {
            TelephonyManager telephonyManager = (TelephonyManager) App.getInstance().getContext().getSystemService(TELEPHONY_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ContextCompat.checkSelfPermission(App.getInstance().getContext(), Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                    telephonyManager.unregisterTelephonyCallback(callStateListener);
                    callStateListenerRegistered = false;
                }
            } else {
                telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
                callStateListenerRegistered = false;
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private static abstract class CallStateListener extends TelephonyCallback implements TelephonyCallback.CallStateListener {
        @Override
        abstract public void onCallStateChanged(int state);
    }

    private boolean callStateListenerRegistered = false;

    private CallStateListener callStateListener = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) ?
            new CallStateListener() {
                @Override
                public void onCallStateChanged(int state) {
                    // Handle call state change
                    handlePhoneState(state);
                }
            }
            : null;

    private PhoneStateListener phoneStateListener = (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) ?
            new PhoneStateListener() {
                @Override
                public void onCallStateChanged(int state, String phoneNumber) {
                    // Handle call state change
                    handlePhoneState(state);
                }
            }
            : null;

    //监听来电状态的监听事件
    private void handlePhoneState(int state) {
        LogUtil.w("PhoneStateListener onCallStateChanged state=%d", state);
        if (state != TelephonyManager.CALL_STATE_OFFHOOK && state != TelephonyManager.CALL_STATE_RINGING) {
            App.getInstance().setInGsmCall(false);
        } else {
            App.getInstance().setInGsmCall(true);
            if (state == TelephonyManager.CALL_STATE_RINGING) {
                EventBus.getDefault().post(Constant.EVENT_SYSTEM_RING);
            }
        }
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        LogUtil.w("Activity lifecycle: %s onCreated", activity.getLocalClassName());
        registerCallStateListener();
    }

    @Override
    public void onActivityStarted(Activity activity) {
        LogUtil.w("Activity lifecycle: %s onStarted", activity.getLocalClassName());
        if (foregroundCount <= 0) {
            LogUtil.w("app在前台");
            YlsCallManager.getInstance().registerSip();
            App.getInstance().setBackground(false);
        }
        foregroundCount++;
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        boolean currIsCallActivity = activity instanceof CallContainerActivity;
        boolean lastIsCallActivity = App.getInstance().getCurrentActivity() instanceof CallContainerActivity;
        jump2Call(activity, currIsCallActivity, lastIsCallActivity);
        App.getInstance().setmCurrentActivity(new WeakReference<>(activity));
        LogUtil.w("Activity lifecycle: %s onResumed", activity.getLocalClassName());
        if (!Utils.isKeyguardLocked(activity)) {//非锁屏条件下进入app清理通知
            NotificationUtils.cancelAllNotification(activity);
        }
        AudioManager audioManager = (AudioManager) App.getInstance().getContext().getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            LogUtil.w("onActivityResumed speakerOn:" + audioManager.isSpeakerphoneOn() + " mode:" + audioManager.getMode());
        }
    }

    private void jump2Call(Context context, boolean currIsCallActivity, boolean lastIsCallActivity) {
        FutureTask<Void> task = new FutureTask<>(() -> {
            try {
                LinkedList<InCallVo> callList = YlsCallManager.getInstance().getCallList();
                boolean inCall = CommonUtil.isListNotEmpty(callList)
                        && (TextUtils.isEmpty(callList.getFirst().getConfId()));
                //防止因为会议室导致的重复创建通话界面导致的register failed, the sensor listeners size has exceeded the maximum limit 128
                LogUtil.w("jump2Call incall:%b, currIsCallActivity:%b, lastIsCallActivity:%b",
                        inCall, currIsCallActivity, lastIsCallActivity);
                if (inCall && !currIsCallActivity && !lastIsCallActivity) {
                    //防止xiaomi vivo手机未打开后台弹出界面权限导致打开应用后该通通话无法再次弹出
                    CallManager.getInstance().realJump2CallActivity(context, null);
                }
            } catch (Exception e) {
                LogUtil.e(e, "onActivityResumed");
            } finally {
                ThreadPoolManager.getInstance().clearCallList();
            }
            return null;
        });
        ThreadPoolManager.getInstance().handlerCall(task);
    }

    @Override
    public void onActivityPaused(Activity activity) {
        LogUtil.w("Activity lifecycle: %s onPaused", activity.getLocalClassName());
    }

    @Override
    public void onActivityStopped(Activity activity) {
        LogUtil.w("Activity lifecycle: %s onStopped", activity.getLocalClassName());
        foregroundCount--;
        if (foregroundCount <= 0) {
            LogUtil.w("app在后台");
            if (YlsCallManager.getInstance().isInCall()) {
                CallManager.getInstance().makeNewCallNotification(activity, false);
            }
            App.getInstance().setBackground(true);
        }
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        LogUtil.w("Activity lifecycle: %s onDestroyed", activity.getLocalClassName());
        unRegisterCallStateListener();
    }

}
