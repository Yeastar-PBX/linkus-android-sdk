package com.yeastar.linkus.demo.push;

import android.text.TextUtils;
import android.util.Log;

import com.huawei.hms.push.HmsMessageService;
import com.huawei.hms.push.RemoteMessage;
import com.yeastar.linkus.constant.YlsConstant;
import com.yeastar.linkus.service.base.YlsBaseManager;
import com.yeastar.linkus.service.call.YlsCallManager;
import com.yeastar.linkus.service.callback.RequestCallback;

import org.json.JSONException;
import org.json.JSONObject;

public class HuaWeiPushService extends HmsMessageService {
    public static final String TAG = "HuaWeiPushService";
    @Override
    public void onNewToken(String token) {
        if (!TextUtils.isEmpty(token)) {
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
            Log.w(TAG,"HuaWei token==" + token);
        }
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        String pushMsg = remoteMessage.getData();
        Log.w(TAG,"HuaWei onPushMsg==" + pushMsg);
        if (!TextUtils.isEmpty(pushMsg)) {
            String data = new String(pushMsg);
            Log.w(TAG,"receiver payload = " + data);//透传消息文本内容
            try {
                JSONObject jsonObject = new JSONObject(data);
                YlsCallManager.getInstance().handlerPushMessage(getApplicationContext(), jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }
}
