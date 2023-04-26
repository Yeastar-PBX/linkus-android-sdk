package com.yeastar.linkus.demo.eventbus;

/**
 * Created by ted on 19-5-25.
 */
public class NetWorkLevelEvent {

    private int networkLevel;
    private int callId;

    public NetWorkLevelEvent(int callId, int networkLevel) {
        this.callId = callId;
        this.networkLevel = networkLevel;
    }

    public int getNetworkLevel() {
        return networkLevel;
    }

    public int getCallId() {
        return callId;
    }
}
