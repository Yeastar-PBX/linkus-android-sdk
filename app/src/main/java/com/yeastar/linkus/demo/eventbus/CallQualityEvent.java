package com.yeastar.linkus.demo.eventbus;


import com.yeastar.linkus.service.call.vo.CallQualityVo;

import java.util.List;

/**
 * Created by ted on 18-11-15.
 */
public class CallQualityEvent {

    //区分多方通话管理页面与通话页面
    private boolean p2p;
    private CallQualityVo callQualityVo;
    private List<CallQualityVo> callQualityVos;

    public CallQualityEvent(CallQualityVo callQualityVo) {
        this.p2p = true;
        this.callQualityVo = callQualityVo;
    }

    public CallQualityEvent(List<CallQualityVo> callQualityVos) {
        this.p2p = false;
        this.callQualityVos = callQualityVos;
    }

    public CallQualityVo getCallQualityVo() {
        return callQualityVo;
    }

    public List<CallQualityVo> getCallQualityVos() {
        return callQualityVos;
    }

    public boolean isP2p() {
        return p2p;
    }
}
