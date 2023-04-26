package com.yeastar.linkus.demo.utils.receiver;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.yeastar.linkus.demo.call.CallContainerActivity;
import com.yeastar.linkus.service.call.YlsCallManager;
import com.yeastar.linkus.service.call.vo.InCallVo;
import com.yeastar.linkus.service.log.LogUtil;
import com.yeastar.linkus.demo.App;
import com.yeastar.linkus.demo.utils.NotificationUtils;
import com.yeastar.linkus.demo.utils.Utils;
import com.yeastar.linkus.utils.CommonUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.LinkedList;
import java.util.concurrent.FutureTask;


/**
 * Created by root on 17-4-12.
 * 屏幕解锁广播监听
 */

public class LockScreenReceiver extends BroadcastReceiver {

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onReceive(Context context, Intent intent) {
//        String action = intent.getAction();
//        FutureTask<Void> task = new FutureTask<>(() -> {
//            try {
//                Activity currentActivity = App.getInstance().getCurrentActivity();
//                //屏幕解锁且当前activity在前台需要清除掉未接来电通知
//                if (Intent.ACTION_USER_PRESENT.equals(action) && currentActivity != null
//                        && Utils.isAppOnForeground(currentActivity)) {
//                    LogUtil.w("LockScreenReceiver 解锁屏幕 清除通知栏");
//                    NotificationUtils.cancelAllNotification(currentActivity);
//                    EventBus.getDefault().post(new CallLogChangeEvent(Constant.SYNC_SUCCESFUL));
//                }
//                LinkedList<InCallVo> list = YlsCallManager.getInstance().getCallList();
//                LogUtil.w("LockScreenReceiver  onReceive  action=" + action);
//                if (CommonUtil.isListNotEmpty(list)) {
//                    //屏幕解锁且非悬浮窗开启状态
//                    if (Intent.ACTION_USER_PRESENT.equals(action)) {
//                        //关闭锁屏页
//                        CallManager.getInstance().jump2CallActivity(context);
//                    } else if (Intent.ACTION_SCREEN_ON.equals(action) && Utils.isKeyguardLocked(context)) {
//                        Activity activity = GlobalCache.getActivityByName(CallContainerActivity.class.getName());
//                        if (activity != null) {
//                            LogUtil.w("activity not null");
//                            if (!Utils.isActivityForeground(context, CallContainerActivity.class.getName())) {
//                                LogUtil.w("moveToFront");
//                                Utils.moveToFront(context);
//                            }
//                        } else {
//                            CallManager.getInstance().jump2CallActivity(context);
//                        }
//                    }
//                }
//            } catch (Exception e) {
//                LogUtil.e(e,"LockScreenReceiver");
//            } finally {
//                ThreadPoolManager.getInstance().clearSdkList();
//            }
//            return null;
//        });
//        ThreadPoolManager.getInstance().handlerSdkEvent(task);
    }

}
