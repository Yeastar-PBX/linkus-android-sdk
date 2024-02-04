package com.yeastar.linkus.demo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.yeastar.linkus.demo.call.CallContainerActivity;
import com.yeastar.linkus.service.call.YlsCallManager;
import com.yeastar.linkus.service.login.YlsLoginManager;
import com.yeastar.linkus.utils.CommonUtil;
import com.yeastar.linkus.utils.NetWorkUtil;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        boolean isLoginEd = YlsLoginManager.getInstance().isLoginEd();
        if (isLoginEd) {
            int networkType = NetWorkUtil.getNetWorkType(this);
            YlsLoginManager.getInstance().cacheLogin(networkType);
            //vivo推送和荣耀推送需要支持此方法
            boolean isClickNotification = CommonUtil.isNotifyClick(getApplicationContext(), getIntent());
            if (isClickNotification) {
                Log.w("WelcomeActivity", "isClickNotification");
            }
            if (YlsCallManager.getInstance().isInCall() || isClickNotification) {
                Log.w("WelcomeActivity", "isInCall");
                Intent callIntent = new Intent(this, CallContainerActivity.class);
                callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (!App.getInstance().isMain()) {//MainActivity是否启动
                    Intent mainIntent = new Intent(this, DialPadActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    Intent[] intents = new Intent[]{mainIntent, callIntent};
                    startActivities(intents);
                } else {
                    startActivity(callIntent);
                }
            } else {
                Log.w("WelcomeActivity", "DialPadActivity");
                startActivity(new Intent(this, DialPadActivity.class));
            }
        } else {
            startActivity(new Intent(this, LoginActivity.class));
        }
        finish();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }
}