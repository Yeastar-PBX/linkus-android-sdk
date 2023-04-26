package com.yeastar.linkus.demo.call;

import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.yeastar.linkus.demo.R;
import com.yeastar.linkus.demo.eventbus.CallStateEvent;
import com.yeastar.linkus.demo.eventbus.ConnectionChangeEvent;
import com.yeastar.linkus.demo.widget.ClickImageView;
import com.yeastar.linkus.service.call.YlsCallManager;
import com.yeastar.linkus.service.call.vo.InCallVo;
import com.yeastar.linkus.service.log.LogUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.LinkedList;
import java.util.Objects;

/**
 * Created by ted on 17-7-27.
 */

public class CallWaitingFragment extends InCallRelatedFragment implements View.OnClickListener {

    private InCallContractItem mInCallContact;
    private TextView mTvTxLevel;
    private CallWaitingInterface callWaitingInterface;
    private ClickImageView mIvZoomOut;
    private ClickImageView mIvHangupAnswer;
    private ClickImageView mIvReject;
    private ClickImageView mIvAnswerHold;


    public CallWaitingFragment() {
        super(R.layout.fragment_call_waiting);
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void handleConnectionChange(ConnectionChangeEvent connectionChangeEvent) {
        notifyData();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handlerCallStatus(CallStateEvent callStateEvent) {
        notifyData();
    }

    @Override
    public void findView(View parent) {
        super.findView(parent);
        mTvTxLevel = parent.findViewById(R.id.tv_tx_level);
        mIvZoomOut = parent.findViewById(R.id.iv_zoom_out);
        mIvHangupAnswer = parent.findViewById(R.id.iv_hangup_answer);
        mIvReject = parent.findViewById(R.id.iv_reject);
        mIvAnswerHold = parent.findViewById(R.id.iv_answer_hold);
        mInCallContact = parent.findViewById(R.id.incall_contact);
        mInCallContact.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        notifyData();
        setListener();
    }

    void setCallWaitingInterface(CallWaitingInterface callWaitingInterface) {
        this.callWaitingInterface = callWaitingInterface;
    }

    public void setListener() {
        mIvHangupAnswer.setOnClickListener(this);
        mIvReject.setOnClickListener(this);
        mIvAnswerHold.setOnClickListener(this);
    }

    public void notifyData() {
        if (!YlsCallManager.getInstance().isInCall()) {
            CallManager.getInstance().finishAllCall(getContext());
            return;
        }
        InCallVo lastInCallVo = YlsCallManager.getInstance().getCallList().getLast();
        if (YlsCallManager.getInstance().getCallListCount() == 2 && lastInCallVo != null) {
            mInCallContact.setContact(lastInCallVo, true);
            mInCallContact.setTimerText(lastInCallVo);
        } else {
            // 添加日志排查未弹出callWaiting页面的问题
            LogUtil.w("callWaiting 当前通话数:" + YlsCallManager.getInstance().getCallListCount());
            if (callWaitingInterface != null) {
                callWaitingInterface.dealFinish();
            }
        }
    }

    @Override
    public void onClick(View v) {
        // 添加日志排查未弹出callWaiting页面的问题
        LinkedList<InCallVo> callList = YlsCallManager.getInstance().getCallList();
        LogUtil.w("callWaiting 当前通话情况:" + callList);
        if (callList.size() == 2) {
            if (v == mIvHangupAnswer) {
                InCallVo hangupVo = callList.getFirst();
                YlsCallManager.getInstance().hangUpCall(activity, hangupVo.getCallId());
                InCallVo inCallVo = callList.getLast();
                if (Objects.equals(inCallVo.getPreviewStatus(), "manual")) {
                    inCallVo.setPreviewStatus("");
                }
                CallManager.getInstance().answerCall(inCallVo.getCallId());
            } else if (v == mIvAnswerHold) {
                InCallVo firstInCallVo = callList.removeLast();
                InCallVo secondInCallModel = callList.getFirst();
                callList.addFirst(firstInCallVo);
                if (Objects.equals(firstInCallVo.getPreviewStatus(), "manual")) {
                    firstInCallVo.setPreviewStatus("");
                }
                YlsCallManager.getInstance().holdCall(secondInCallModel);
                CallManager.getInstance().answerCall(firstInCallVo.getCallId());
            } else if (v == mIvReject) {
                InCallVo rejectInCallVo = callList.getLast();
                YlsCallManager.getInstance().answerBusy(activity, rejectInCallVo.getCallId());
            }
            if (callWaitingInterface != null) {
                callWaitingInterface.dealFinish();
            }

        }

    }

    public interface CallWaitingInterface {
        void dealFinish();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
