package com.yeastar.linkus.demo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.yeastar.linkus.demo.call.CallContainerActivity;
import com.yeastar.linkus.service.call.YlsCallManager;
import com.yeastar.linkus.service.login.YlsLoginManager;
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
            if (YlsCallManager.getInstance().isInCall()) {
                startActivity(new Intent(this, CallContainerActivity.class));
            } else {
                startActivity(new Intent(this, DialPadActivity.class));
            }
        } else {
            startActivity(new Intent(this, LoginActivity.class));
        }
    }
}