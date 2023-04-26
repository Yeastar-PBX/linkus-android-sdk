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
//        updateAnswerCdr(conferenceId, curInCallModel);
    }

//    private void updateAnswerCdr(String conferenceId, InCallVo curInCallModel) {
//        FutureTask<Void> task = new FutureTask<>(() -> {
//            try {
//                if (curInCallModel != null) {
//                    CallLogManager.getInstance().updateCdr(curInCallModel, CdrModel.CALL_STATUS_ANSWERED, CdrModel.CDR_READ_YES);
//                }
//                if (!TextUtils.isEmpty(conferenceId) && !Constant.PUSH_CONFERENCE.equals(conferenceId)) {
//                    //会议室来电一定要调用,服务端才知道这个分机上线了
//                    ConferenceManger.getInstance().getConferenceModel().setInMeeting(1);
//                    ConferenceManger.getInstance().updateAllConferenceStatus(Constant.NO);
//                }
//            } catch (Exception e) {
//                FileCache.reportBuggly(e, "ring answer");
//            } finally {
//                ThreadPoolManager.getInstance().clearCallList();
//            }
//            return null;
//        });
//        ThreadPoolManager.getInstance().handlerCall(task);
//    }

    @Override
    public void reject(Context context, int callId, String callerNumber) {
        final InCallVo curInCallModel = getCurInCallModel();
        YlsCallManager.getInstance().answerBusy(context, callId);
//        updateRejectCdr(curInCallModel);
    }

//    private void updateRejectCdr(InCallVo curInCallModel) {
//        FutureTask<Void> task = new FutureTask<>(() -> {
//            try {
//                if (curInCallModel != null) {
//                    CallLogManager.getInstance().updateCdr(curInCallModel, CdrModel.CALL_STATUS_NO_ANSWER, CdrModel.CDR_READ_YES);
//                }
//            } catch (Exception e) {
//                FileCache.reportBuggly(e, "ring reject");
//            } finally {
//                ThreadPoolManager.getInstance().clearCallList();
//            }
//            return null;
//        });
//        ThreadPoolManager.getInstance().handlerCall(task);
//    }

    @Nullable
    private InCallVo getCurInCallModel() {
        InCallVo inCallVo = YlsCallManager.getInstance().getFirstCall();
        return inCallVo;
    }
}
