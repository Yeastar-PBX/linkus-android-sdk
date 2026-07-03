package com.yeastar.linkus.demo.push;

import android.text.TextUtils;
import android.util.Log;

import com.hihonor.push.sdk.HonorMessageService;
import com.hihonor.push.sdk.HonorPushDataMsg;
import com.yeastar.linkus.constant.YlsConstant;
import com.yeastar.linkus.service.base.YlsBaseManager;
import com.yeastar.linkus.service.call.YlsCallManager;
import com.yeastar.linkus.service.callback.RequestCallback;
import com.yeastar.linkus.service.log.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 荣耀推送透传消息服务
 */
public class DemoHonorMessageService extends HonorMessageService {
    public static final String TAG = "DemoHonorMessageService";

    /**
     * Token发生变化时回调
     */
    @Override
    public void onNewToken(String pushToken) {
        super.onNewToken(pushToken);
        if (!TextUtils.isEmpty(pushToken)) {
            Log.w(TAG, "Honor token==" + pushToken);
            YlsBaseManager.getInstance().setPushInfo(YlsConstant.PUSH_MODE_HONOR, pushToken, new RequestCallback() {
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
    }

    /**
     * 接收荣耀推送透传消息
     */
    @Override
    public void onMessageReceived(HonorPushDataMsg msg) {
        super.onMessageReceived(msg);
        String pushMsg = msg.getData();
        LogUtil.w("Honor onMessageReceived==" + pushMsg);
        if (!TextUtils.isEmpty(pushMsg)) {
            try {
                JSONObject jsonObject = new JSONObject(pushMsg);
                YlsCallManager.getInstance().handlerPushMessage(getApplicationContext(), jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
