package com.yeastar.linkus.demo.push;

import com.huawei.hms.push.HmsMessageService;
import com.huawei.hms.push.RemoteMessage;
import com.yeastar.linkus.service.base.YlsBaseManager;
import com.yeastar.linkus.service.call.YlsCallManager;
import com.yeastar.linkus.service.callback.RequestCallback;
import com.yeastar.linkus.service.log.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

public class DemoHmsMessageService extends HmsMessageService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        if (remoteMessage != null) {
            String data = remoteMessage.getData();
            LogUtil.i("receiver payload = " + data);//透传消息文本内容
            try {
                JSONObject jsonObject = new JSONObject(data);
                YlsCallManager.getInstance().handlerPushMessage(getApplicationContext(), jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        LogUtil.i("onNewToken -> " + "clientid = " + s);
        YlsBaseManager.getInstance().setPushInfo("huawei", s, new RequestCallback() {
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

    @Override
    public void onTokenError(Exception e) {
        super.onTokenError(e);
        LogUtil.i("onTokenError -> " + "clientid = " + e.getMessage());
    }
}
