package com.yeastar.linkus.demo.call.ring;

import android.content.Context;

import androidx.annotation.Nullable;

import com.yeastar.linkus.demo.call.CallManager;
import com.yeastar.linkus.service.call.YlsCallManager;
import com.yeastar.linkus.service.call.vo.InCallVo;

import java.util.Objects;

/**
 * Created by ted on 17-4-27.
 */

public class RingPresenter implements RingContract.Presenter {
    @Override
    public void answer(int callId, String conferenceId, String callerNumber) {
        final InCallVo curInCallModel = getCurInCallModel();
        if (Objects.equals(curInCallModel.getPreviewStatus(), "manual")) {
            curInCallModel.setPreviewStatus("");
        }
        CallManager.getInstance().answerCall(callId);
    }

    @Override
    public void reject(Context context, int callId, String callerNumber) {
        YlsCallManager.getInstance().answerBusy(context, callId);
    }

    @Nullable
    private InCallVo getCurInCallModel() {
        InCallVo inCallVo = YlsCallManager.getInstance().getFirstCall();
        return inCallVo;
    }
}
