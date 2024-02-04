package com.yeastar.linkus.demo.push;

import android.content.Context;

import com.vivo.push.sdk.OpenClientPushMessageReceiver;

public class VivoReceiver extends OpenClientPushMessageReceiver {
    @Override
    public void onReceiveRegId(Context context, String regId) {
        super.onReceiveRegId(context, regId);
        //RegId结果返回，只有首次获取到或regId发生变化时才会回调
    }

}
