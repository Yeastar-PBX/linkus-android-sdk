package com.yeastar.linkus.demo.eventbus;


import com.yeastar.linkus.service.call.vo.CallStateVo;

/**
 * Created by ted on 17-5-17.
 */

public class CallStateEvent {
    private int statusCode;
    /**
     * 0 null,1 CALLING,2 incoming,3 响铃,4 连接中,5 接通,6 挂断,7 无网络
     */
    private int status;
    private int callId;
    /**
     * 是否呼出电话
     */
    private boolean isCallOut;
    /**
     * 呼出号码
     */
    private String callNumber;
    /**
     * call switch使用字段
     */
    private String unique;

    public CallStateEvent(int statusCode, int status, int callId, boolean isCallOut, String callNumber) {
        this.statusCode = statusCode;
        this.status = status;
        this.callId = callId;
        this.isCallOut = isCallOut;
        this.callNumber = callNumber;
    }

    public CallStateEvent(CallStateVo callStateVo) {
        this.statusCode = callStateVo.getStatusCode();
        this.status = callStateVo.getStatus();
        this.callId = callStateVo.getCallId();
        this.isCallOut = callStateVo.isCallOut();
        this.callNumber = callStateVo.getCallNumber();
    }

    public int getStatusCode() {
        return statusCode;
    }

    public int getStatus() {
        return status;
    }

    public int getCallId() {
        return callId;
    }

    public boolean isCallOut() {
        return isCallOut;
    }

    public String getCallNumber() {
        return callNumber;
    }

    public String getUnique() {
        return unique;
    }
}
