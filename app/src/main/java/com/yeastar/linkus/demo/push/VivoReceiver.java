package com.yeastar.linkus.demo.push;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.vivo.push.model.UnvarnishedMessage;
import com.vivo.push.sdk.OpenClientPushMessageReceiver;
import com.yeastar.linkus.constant.YlsConstant;
import com.yeastar.linkus.service.base.YlsBaseManager;
import com.yeastar.linkus.service.call.YlsCallManager;
import com.yeastar.linkus.service.callback.RequestCallback;

import org.json.JSONException;
import org.json.JSONObject;

public class VivoReceiver extends OpenClientPushMessageReceiver {
    public static final String TAG = "VivoReceiver";
    @Override
    public void onReceiveRegId(Context context, String regId) {
        super.onReceiveRegId(context, regId);
        //RegId结果返回，只有首次获取到或regId发生变化时才会回调
        YlsBaseManager.getInstance().setPushInfo(YlsConstant.PUSH_MODE_VIVO, regId, new RequestCallback() {
            @Override
            public void onSuccess(Object o) {}

            @Override
            public void onFailed(int i) {}

            @Override
            public void onException(Throwable throwable) {}
        });
    }

    @Override
    public void onTransmissionMessage(Context context, UnvarnishedMessage unvarnishedMessage) {
        super.onTransmissionMessage(context, unvarnishedMessage);
        String pushMsg = unvarnishedMessage.getMessage();
        Log.i(TAG,"VIVO onTransmissionMessage==" + pushMsg);
        if (!TextUtils.isEmpty(pushMsg)) {
            try {
                JSONObject jsonObject = new JSONObject(pushMsg);
                YlsCallManager.getInstance().handlerPushMessage(context.getApplicationContext(), jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
