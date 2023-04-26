package com.yeastar.linkus.demo.call.ring;

import android.Manifest;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.alibaba.fastjson.JSONObject;
import com.yeastar.linkus.constant.YlsConstant;
import com.yeastar.linkus.demo.Constant;
import com.yeastar.linkus.demo.R;
import com.yeastar.linkus.demo.base.BaseActivity;
import com.yeastar.linkus.demo.call.CallContainerActivity;
import com.yeastar.linkus.demo.call.CallManager;
import com.yeastar.linkus.demo.call.InCallContractItem;
import com.yeastar.linkus.demo.call.InCallFragment;
import com.yeastar.linkus.demo.call.InCallRelatedFragment;
import com.yeastar.linkus.demo.eventbus.CallStateEvent;
import com.yeastar.linkus.demo.eventbus.ConnectionChangeEvent;
import com.yeastar.linkus.demo.utils.StatusBarUtil;
import com.yeastar.linkus.demo.utils.permission.PermissionRequest;
import com.yeastar.linkus.demo.widget.ClickImageView;
import com.yeastar.linkus.service.call.YlsCallManager;
import com.yeastar.linkus.service.call.vo.InCallVo;
import com.yeastar.linkus.service.log.LogUtil;
import com.yeastar.linkus.utils.remoteControlUtil.RemoteControlUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

public class RingFragment extends InCallRelatedFragment {

    private RingContract.Presenter presenter;
    private InCallContractItem mInCallCenterContact;
    private String conferenceId;
    private ConstraintLayout mFlRingBg;
    private TextView mTvTxLevel;
    private ClickImageView mIvRingDecline;
    private ClickImageView mIvRingAccept;
    private long startTime;


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handlerCallStatus(CallStateEvent callStateEvent) {
        if (YlsCallManager.getInstance().isInCall()) {
            InCallVo inCallVo = YlsCallManager.getInstance().getFirstCall();
            LogUtil.w("响铃界面 来电状态通知 总的通话数=" + YlsCallManager.getInstance().getCallList().size() +
                    " 当前通话callId==" + inCallVo.getCallId() + "  当前通话状态=" + inCallVo.getCallState() +
                    "  通知的通话的callId=" + callStateEvent.getCallId() + "  通知通话的状态=" + callStateEvent.getStatus()
                    + " 0 null,1 calling,2 incoming,3 响铃,4 连接中,5 接通,6 挂断");
            if (YlsConstant.SIP_DISCONNECT == inCallVo.getCallState()) {
                activity.finish();
            } else if (YlsConstant.SIP_CONFIRMED != inCallVo.getCallState()) {
                updateData();
            }
        } else {
            LogUtil.w("callId is null");
            activity.finish();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void handleConnectionChange(ConnectionChangeEvent connectionChangeEvent) {
        InCallVo inCallVo = YlsCallManager.getInstance().getFirstCall();
        if (mInCallCenterContact != null && inCallVo != null) {
            mInCallCenterContact.setTimerText(inCallVo);
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void handleCallKitAction(String action) {
        LogUtil.w("RingFragment callKit Action = %s", action);
        if (Constant.EVENT_ANSWER_CALL.equals(action)) {
            answerAction();
        } else if (Constant.EVENT_REJECT_CALL.equals(action)
                || Constant.EVENT_INCOMING_FAILED.equals(action)
                || Constant.EVENT_ON_DISCONNECT_OR_ABORT.equals(action)) {
            rejectAction();
        }
        EventBus.getDefault().removeStickyEvent(action);
    }

    /**
     * 在初始化过程中,通话状态变更后切换页面
     */
    private void switchInCallFragment() {
        InCallVo inCallVo = YlsCallManager.getInstance().getFirstCall();
        //避免短时间内的重复调用replace方法
        if (inCallVo == null || (System.currentTimeMillis() - startTime) < 500) {
            return;
        }
        LogUtil.w("switchInCallFragment");
        //判断会议室还是普通来电
        InCallFragment inCallFragment = new InCallFragment();
        inCallFragment.setContainerId(R.id.call_container);
        ((BaseActivity) activity).switchContent(inCallFragment, Constant.TAG_FRAGMENT_CALL);
        startTime = System.currentTimeMillis();
    }

    public RingFragment() {
        super(R.layout.fragment_ring);
        presenter = new RingPresenter();
    }

    @Override
    public void findView(View v) {
        StatusBarUtil.transparentStatusBar(activity);
        InCallVo inCallVo=null;
        if (YlsCallManager.getInstance().isInCall()) {
            inCallVo = YlsCallManager.getInstance().getFirstCall();
            if (YlsConstant.SIP_CONFIRMED == inCallVo.getCallState()) {
                switchInCallFragment();
            }
        } else {
            CallManager.getInstance().finishAllCallActivity();
        }
        super.findView(v);
        initRemoteControl();
        if (inCallVo != null) {
            conferenceId = inCallVo.getConfId();
        }
        initView(v);
        setListener();
    }


    public boolean isConference() {
        return !TextUtils.isEmpty(conferenceId);
    }

    private void initRemoteControl() {
        RemoteControlUtil.getInstance().setRemoteControlListener(new RemoteControlUtil.OnRemoteControlListener() {
            @Override
            public void onPlay() {
                answerAction();
            }

            @Override
            public void onPause() {
                rejectAction();
            }
        });
    }

    private void initView(View view) {
        InCallVo inCallVo = YlsCallManager.getInstance().getFirstCall();
        mFlRingBg = view.findViewById(R.id.fl_ring_bg);
        mTvTxLevel = view.findViewById(R.id.tv_tx_level);
        mInCallCenterContact = view.findViewById(R.id.incall_center_contact);
        mIvRingDecline = view.findViewById(R.id.iv_ring_decline);
        mIvRingAccept = view.findViewById(R.id.iv_ring_accept);
        if (inCallVo != null) {
            LogUtil.w("响铃页面 inCallVo = %s", JSONObject.toJSONString(inCallVo));
            updateData();
        } else {
            activity.finish();
            CallManager.getInstance().finishAllCallActivity();
        }
    }

    private void updateData() {
        InCallVo inCallVo = YlsCallManager.getInstance().getFirstCall();
        if (inCallVo==null) {
            CallManager.getInstance().finishAllCall(getContext());
        } else {
            mInCallCenterContact.setContact(inCallVo, true);
            mInCallCenterContact.setTimerText(inCallVo);
            mInCallCenterContact.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        }
    }

    public void setListener() {
        mIvRingDecline.setOnClickListener(v -> rejectAction());
        mIvRingAccept.setOnClickListener(v -> answerAction());
    }

    private void answerAction() {
        PermissionRequest request = new PermissionRequest(this,
                new PermissionRequest.PermissionCallback() {
                    @Override
                    public void onSuccessful(List<String> permissions) {
                        InCallVo inCallVo = YlsCallManager.getInstance().getFirstCall();
                        if (inCallVo == null) {
                            CallManager.getInstance().finishAllCall(getContext());
                            return;
                        }
                        if (isConference()) {
                            answerConference(inCallVo);
                        } else {
                            answerCall(inCallVo);
                        }
                    }

                    @Override
                    public void onFailure(List<String> permissions) {
                    }

                });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            request.hasPermission(Manifest.permission.READ_PHONE_STATE, Manifest.permission.RECORD_AUDIO, Manifest.permission.BLUETOOTH_CONNECT);
        } else {
            request.hasPermission(Manifest.permission.READ_PHONE_STATE, Manifest.permission.RECORD_AUDIO);
        }

    }

    private void rejectAction() {
        InCallVo inCallVo = YlsCallManager.getInstance().getFirstCall();
        if (inCallVo == null) {
            CallManager.getInstance().finishAllCall(getContext());
            return;
        }
        if (isConference()) {
            rejectConference(inCallVo);
        } else {
            rejectCall(inCallVo);
        }
    }

    private void answerConference(InCallVo inCallVo) {
        LogUtil.w("响铃界面 接听会议室来电");
        presenter.answer(inCallVo.getCallId(), conferenceId, inCallVo.getConfAdmin());
        switchInCallFragment();
    }

    private void answerCall(InCallVo inCallVo) {
        LogUtil.w("响铃界面 接听普通来电");
        presenter.answer(inCallVo.getCallId(), "", inCallVo.getCallNumber());
        switchInCallFragment();
    }

    private void rejectConference(InCallVo inCallVo) {
        LogUtil.w("响铃界面 拒接会议室来电");
        presenter.reject(activity, inCallVo.getCallId(), inCallVo.getConfAdmin());
        activity.finish();
    }

    private void rejectCall(InCallVo inCallVo) {
        LogUtil.w("响铃界面 拒接普通来电");
        presenter.reject(activity, inCallVo.getCallId(), inCallVo.getCallNumber());
        activity.finish();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ((CallContainerActivity) activity).setPhoneStateCallback(null);
    }
}