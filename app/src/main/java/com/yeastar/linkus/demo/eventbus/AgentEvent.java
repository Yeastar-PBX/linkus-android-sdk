package com.yeastar.linkus.demo.eventbus;

import android.content.Intent;

/**
 * Created by ted on 17-12-19.
 */

public class AgentEvent {
    private int requestCode;
    private int resultCode;
    private Intent data;

    public AgentEvent(int requestCode, int resultCode, Intent data) {
        this.requestCode = requestCode;
        this.resultCode = resultCode;
        this.data = data;
    }

    public int getRequestCode() {
        return requestCode;
    }

    public int getResultCode() {
        return resultCode;
    }

    public Intent getData() {
        return data;
    }
}
