package com.yeastar.linkus.demo.push;

import android.content.Context;

import com.igexin.sdk.GTIntentService;
import com.igexin.sdk.message.GTCmdMessage;
import com.igexin.sdk.message.GTNotificationMessage;
import com.igexin.sdk.message.GTTransmitMessage;
import com.yeastar.linkus.service.base.YlsBaseManager;
import com.yeastar.linkus.service.call.YlsCallManager;
import com.yeastar.linkus.service.callback.RequestCallback;
import com.yeastar.linkus.service.log.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 继承 GTIntentService 接收来自个推的消息，所有消息在主线程中回调，如果注册了该服务，则务必要在 AndroidManifest 中声明，否则无法接受消息
 */
public class DemoIntentService extends GTIntentService {

    @Override
    public void onReceiveServicePid(Context context, int pid) {
    }

    /**
     * 此方法用于接收和处理透传消息。透传消息个推只传递数据，不做任何处理，客户端接收到透传消息后需要自己去做后续动作处理，如通知栏展示、弹框等。
     * 如果开发者在客户端将透传消息创建了通知栏展示，建议将展示和点击回执上报给个推。
     */
    @Override
    public void onReceiveMessageData(Context context, GTTransmitMessage msg) {
        byte[] payload = msg.getPayload();
        //taskid和messageid字段，是用于回执上报的必要参数。详情见下方文档“6.2 上报透传消息的展示和点击数据”
        String taskid = msg.getTaskId();
        String messageid = msg.getMessageId();
        String data = new String(payload);
        LogUtil.i("receiver payload = " + data);//透传消息文本内容
        try {
            JSONObject jsonObject = new JSONObject(data);
            YlsCallManager.getInstance().handlerPushMessage(context, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // 接收 cid
    @Override
    public void onReceiveClientId(Context context, String clientid) {
        LogUtil.i("onReceiveClientId -> " + "clientid = " + clientid);
        YlsBaseManager.getInstance().setPushInfo("getui", clientid, new RequestCallback() {
            @Override
            public void onSuccess(Object result) {

            }

            @Override
            public void onFailed(int code) {

            }

            @Override
            public void onException(Throwable exception) {

            }
        });
    }

    // cid 离线上线通知
    @Override
    public void onReceiveOnlineState(Context context, boolean online) {
    }

    // 各种事件处理回执
    @Override
    public void onReceiveCommandResult(Context context, GTCmdMessage cmdMessage) {
    }

    // 通知到达，只有个推通道下发的通知会回调此方法
    @Override
    public void onNotificationMessageArrived(Context context, GTNotificationMessage msg) {
    }

    // 通知点击，只有个推通道下发的通知会回调此方法
    @Override
    public void onNotificationMessageClicked(Context context, GTNotificationMessage msg) {
    }
}