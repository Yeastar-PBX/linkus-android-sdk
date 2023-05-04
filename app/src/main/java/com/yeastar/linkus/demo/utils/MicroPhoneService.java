package com.yeastar.linkus.demo.utils;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.yeastar.linkus.demo.Constant;
import com.yeastar.linkus.demo.call.CallManager;
import com.yeastar.linkus.service.log.LogUtil;


public class MicroPhoneService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtil.w("MicroPhoneService onStartCommand");
        if (intent != null && intent.hasExtra(Constant.EXTRA_DATA)) {
            Notification notification = intent.getParcelableExtra(Constant.EXTRA_DATA);
            if (notification != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                LogUtil.w("MicroPhoneService startForeground");
                startForeground(Constant.MICRO_PHONE_NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL);
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtil.w("MicroPhoneService onDestroy");
        CallManager.getInstance().setMicroPhoneServiceIntent(null);
    }
}
