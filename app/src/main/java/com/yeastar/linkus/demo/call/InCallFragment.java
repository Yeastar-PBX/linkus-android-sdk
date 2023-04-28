package com.yeastar.linkus.demo.call;

import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.View;
import android.widget.Chronometer;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import com.yeastar.linkus.demo.App;
import com.yeastar.linkus.demo.Constant;
import com.yeastar.linkus.demo.R;
import com.yeastar.linkus.demo.call.dialpad.CallDialPad;
import com.yeastar.linkus.demo.call.ring.RingFragment;
import com.yeastar.linkus.demo.eventbus.AgentEvent;
import com.yeastar.linkus.demo.eventbus.AudioRouteEvent;
import com.yeastar.linkus.demo.eventbus.CallQualityEvent;
import com.yeastar.linkus.demo.eventbus.CallStateEvent;
import com.yeastar.linkus.demo.eventbus.CallWaitingEvent;
import com.yeastar.linkus.demo.eventbus.ConnectionChangeEvent;
import com.yeastar.linkus.demo.eventbus.RecordEvent;
import com.yeastar.linkus.demo.utils.StatusBarUtil;
import com.yeastar.linkus.demo.utils.ToastUtil;
import com.yeastar.linkus.demo.utils.Utils;
import com.yeastar.linkus.demo.widget.AvatarImageView;
import com.yeastar.linkus.demo.widget.ClickImageView;
import com.yeastar.linkus.service.call.YlsCallManager;
import com.yeastar.linkus.service.call.vo.CallQualityVo;
import com.yeastar.linkus.service.call.vo.InCallVo;
import com.yeastar.linkus.service.log.LogUtil;
import com.yeastar.linkus.utils.CommonUtil;
import com.yeastar.linkus.utils.MediaUtil;
import com.yeastar.linkus.utils.remoteControlUtil.RemoteControlUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class InCallFragment extends InCallRelatedFragment implements InCallContract.View {

    static int REQUEST_CODE_TRANSFER = 0;

    private InCallPresenter inCallPresenter;

    private ConstraintLayout mFlRoot;//高斯模糊背景
    private LinearLayout mLlSwitch;
    private AvatarImageView mCivSwitchAvatar;
    private TextView mTvSwitchCallName;
    private Chronometer mTvIncallTime;
    private CallDialPad callDialPad;
    private InCallContractItem mInCallHoldContact;
    private InCallContractItem mInCallCenterContact;

    private CallWaitingFragment fragment;
    private ClickImageView mIvReportCallQuality;
    private CallQualityDialog callQualityDialog;
    private ScheduledExecutorService scheduledExec;


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handlerCallStatus(CallStateEvent callStateEvent) {
        //通话状态 1,响铃 2,接通 3,挂断 4,early 5,incoming 6,null
        int callStatus = callStateEvent.getStatus();
        int callStatusId = callStateEvent.getCallId();
        int statusCode = callStateEvent.getStatusCode();
        String callNumber = callStateEvent.getCallNumber();
        LogUtil.w("通话界面 handlerCallStatus callId=%d statusCode=%d  callStatus=%d " +
                "(通话状态：0 null,1 calling,2 incoming,3 响铃,4 连接中,5 接通,6 挂断 )", callStatusId, statusCode, callStatus);
        if (callStateEvent.isCallOut() && TextUtils.isEmpty(callStateEvent.getUnique())) {
            dealWithStatusCode(statusCode, callNumber);
            if (!CommonUtil.isListNotEmpty(YlsCallManager.getInstance().getCallList())) {
                return;
            }
        }
//        LinkedList<InCallVo> list = new LinkedList<>(YlsCallManager.getInstance().getCallList());
//        if (CommonUtil.isListNotEmpty(list) && callStatus == YlsConstant.SIP_DISCONNECT) {
//            InCallVo inCallVo = list.getFirst();
////            MediaUtil.getInstance().setAudioRoute(CallManager.getInstance().getAudioRoute(), inCallVo.getCallNumber());
//        }
        inCallPresenter.refresh();
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void handleConnectionChange(ConnectionChangeEvent connectionChangeEvent) {
        LogUtil.w("通话界面 connectionChangeEvent事件通知");
        inCallPresenter.refresh();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handlerCallCallWaiting(CallWaitingEvent event) {
        LogUtil.w("通话界面 收到call waiting事件通知");
        inCallPresenter.refresh();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleRecord(RecordEvent recordEvent) {
        boolean hasRecord = recordEvent.isRecord();
        LogUtil.w("通话界面 录音通知事件 " + hasRecord + " (true:开始录音 false：结束录音)");
        LinkedList<InCallVo> list = YlsCallManager.getInstance().getCallList();
        if (CommonUtil.isListNotEmpty(list)) {
            InCallVo inCallVo = list.getFirst();
            LogUtil.w("handleRecord:%s", inCallVo.toString());
            updateInCallDialPad(inCallVo);
        } else {
            CallManager.getInstance().finishAllCall(activity);
        }
    }

    // 响应来自 AgentActivity 的事件。
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onAgentEvent(AgentEvent event) {
        // [requestCode]区分转移还是添加通话
        int resultCode = event.getRequestCode();
        LogUtil.w("onAgentEvent:%d", resultCode);
        if (YlsCallManager.getInstance().isInCall()) {
            InCallVo inCallVo = YlsCallManager.getInstance().getFirstCall();
            if (REQUEST_CODE_TRANSFER == resultCode) {
                YlsCallManager.getInstance().setInTransfer(false);
                YlsCallManager.getInstance().unHoldCall(getContext(), inCallVo);
            }
            inCallPresenter.refresh();
        } else {
            CallManager.getInstance().finishAllCall(activity);
        }
        EventBus.getDefault().removeStickyEvent(event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleCallQuality(CallQualityEvent callQualityEvent) {
        if (!callQualityEvent.isP2p()) return;
        CallQualityVo callQualityVo = callQualityEvent.getCallQualityVo();
        if (callQualityVo == null) return;
        LogUtil.w("音质调试事件：%s", callQualityVo.toString());
        if (callQualityDialog != null) {
            callQualityDialog.showCallQuality(callQualityVo);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void handleCallKitAction(String action) {
        LogUtil.w("InCallFragment callKit Action = %s", action);
        if (Constant.EVENT_ON_HOLD.equals(action)) {//系统通话解hold,linkus需要hold住?
            LogUtil.w("通话界面 系统通话接听 hold linkus通话");
            inCallPresenter.hold();
            EventBus.getDefault().removeStickyEvent(action);
        } else if (Constant.EVENT_ON_UN_HOLD.equals(action)) {
            LogUtil.w("通话界面 系统通话挂断 unHold linkus通话");
            inCallPresenter.hold();
            EventBus.getDefault().removeStickyEvent(action);
        } else if (Constant.EVENT_ON_DISCONNECT_OR_ABORT.equals(action)) {
            inCallPresenter.hangupPhone();
            EventBus.getDefault().removeStickyEvent(action);
        } else if (Constant.EVENT_OUTGOING_FAILED.equals(action)) {
            inCallPresenter.hangupPhone();
            EventBus.getDefault().removeStickyEvent(action);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handlerAudioRoute(AudioRouteEvent event) {
        LogUtil.w("通话界面 收到AudioRoute事件通知");
        updateInCallDialPad(getInCallModel());
    }

    private void dealWithStatusCode(int statusCode, String number) {
        //状态码404，且呼叫号码为外部号码
        if (statusCode == 404 || statusCode == 603) {
            ToastUtil.showToastInCenter(R.string.sip_wrongnumber);
        } else if (statusCode == 486) {
            ToastUtil.showToastInCenter(R.string.sip_extbusy);
        } else if (statusCode == 408) {
            ToastUtil.showToastInCenter(R.string.sip_request_timeout);
        } else if (statusCode == 480) {
            ToastUtil.showToastInCenter(R.string.sip_noresponse);
        } else if (statusCode == 410) {
            ToastUtil.showToastInCenter(R.string.sip_number_change);
        } else if (statusCode == 484) {
            ToastUtil.showToastInCenter(R.string.sip_number_invalid);
        } else if (statusCode == 488) {
            ToastUtil.showToastInCenter(R.string.sip_notacceptable);
        } else if (statusCode == 403) {
            ToastUtil.showToastInCenter(R.string.sip_forbidden);
        } else if (statusCode == 400) {
            ToastUtil.showToastInCenter(R.string.sip_bad_request);
        } else if (statusCode == 502) {
            ToastUtil.showToastInCenter(R.string.nonetworktip_error);
        } else if (statusCode == 503) {//状态码503，且呼叫号码为外部号码
            ToastUtil.showToastInCenter(R.string.sip_trunkbusy);
        } else if (statusCode == 402 || statusCode == 405 || statusCode == 406 || statusCode == 407 || statusCode == 413 || statusCode == 414 || statusCode == 415 || statusCode == 416 || statusCode == 420 || statusCode == 421 || statusCode == 423 || statusCode == 481 || statusCode == 482 || statusCode == 483 || statusCode == 485 || statusCode == 491 || statusCode == 493 || statusCode == 500 || statusCode == 501 || statusCode == 504 || statusCode == 505 || statusCode == 513 || statusCode == 600 || statusCode == 604 || statusCode == 606) {
            ToastUtil.showToastInCenter(getResources().getString(R.string.sip_unknown) + "  " + statusCode);
        }
    }

    public InCallFragment() {
        super(R.layout.fragment_incall);
    }

    @Override
    public void findView(View v) {
        StatusBarUtil.transparentStatusBar(getActivity());
        initView(v);
        inCallPresenter = new InCallPresenter(this, activity);
        super.findView(v);
        LogUtil.w("启动通话界面");
        LogUtil.w("外放是否开启==" + audioManager.isSpeakerphoneOn()
                + "  通话mode==" + audioManager.getMode());
        int streamVolume = audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
        LogUtil.w("通话页面 当前通话音量=%d", streamVolume);
        InCallVo inCallVo = getInCallModel();
        //不支持callKit才监听通话,否则监听无效
        //避免通话过程转移时候,通话状态未变更,callId仍为-1的情况,再次拨号,导致callKit相关异常
        if (YlsCallManager.getInstance().isSingleCall() && inCallVo != null && inCallVo.getCallId() == -1
                && TextUtils.isEmpty(inCallVo.getLinkedId()) && !inCallVo.isTransfer()) {
            LogUtil.w("通话界面  呼出电话：" + inCallVo.getCallNumber());
            inCallPresenter.callOut();
        } else {
            LogUtil.w("通话界面  来电");
            inCallPresenter.refresh();
        }
        initRemoteControl();
        setListener();
    }

    private void initRemoteControl() {
        RemoteControlUtil.getInstance().setRemoteControlListener(new RemoteControlUtil.OnRemoteControlListener() {
            @Override
            public void onPlay() {

            }

            @Override
            public void onPause() {
                LogUtil.w("通话界面 耳机挂断当前通话 总通话数=" + YlsCallManager.getInstance().getCallList().size());
                inCallPresenter.hangupPhone();
            }
        });
    }

    private void initView(View view) {
        mLlSwitch = view.findViewById(R.id.ll_switch);
        mCivSwitchAvatar = view.findViewById(R.id.civ_switch_avatar);
        mTvSwitchCallName = view.findViewById(R.id.tv_switch_call_name);
        mTvIncallTime = view.findViewById(R.id.tv_incall_time);
        mInCallCenterContact = view.findViewById(R.id.incall_center_contact);
        mFlRoot = view.findViewById(R.id.fl_ring_bg);
        mInCallHoldContact = view.findViewById(R.id.incall_hold_contact);
        mIvReportCallQuality = view.findViewById(R.id.iv_report_call_quality);
        callDialPad = new CallDialPad(getActivity(), view);
    }

    private void observePhoneState() {
        if (getActivity() instanceof CallContainerActivity) {
            ((CallContainerActivity) getActivity()).setPhoneStateCallback(new CallingContract.PhoneStateCallback() {

                @Override
                public void onRing() {
                    LogUtil.w("通话界面 系统来电响铃");
                    activity.moveTaskToBack(true);
                }

                @Override
                public void onCalling() {
                    LogUtil.w("通话界面 系统来电接听");
                    //系统来电接起来的时候处理linkus通话
                    InCallVo hangupCall = null;
                    if (YlsCallManager.getInstance().isInCall()) {
                        for (InCallVo inCallVo : YlsCallManager.getInstance().getCallList()) {
                            if (inCallVo.isAccept()) {//通话中，对普通通话进行hold处理
                                LogUtil.w("通话界面 hold当前通话 callId=" + inCallVo.getCallId());
                                YlsCallManager.getInstance().holdCall(inCallVo);
                            } else {//响铃中的进入挂断队列
                                hangupCall = inCallVo;
                            }
                        }
                    }
                    if (hangupCall != null) {
                        LogUtil.w("通话界面 挂断当前通话 callId=" + hangupCall.getCallId());
                        if (hangupCall.isCallOut()) {
                            YlsCallManager.getInstance().hangUpCall(activity, hangupCall.getCallId());
                        } else {
                            YlsCallManager.getInstance().answerBusy(activity, hangupCall.getCallId());
                        }
                    }
                    if (inCallPresenter != null) {
                        inCallPresenter.refresh();
                    }
                    if (!YlsCallManager.getInstance().isInCall()) {
                        CallManager.getInstance().finishAllCall(activity);
                    }
                }

                @RequiresApi(api = Build.VERSION_CODES.Q)
                @Override
                public void onHangup() {
                    LogUtil.w("通话界面 系统来电挂断");
                    try {
                        //处理部分手机接通系统来电挂断后,免提功能失效
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Utils.moveToFront(activity);
                    if (YlsCallManager.getInstance().isInCall()) {
                        InCallVo firstVo = YlsCallManager.getInstance().getCallList().getFirst();
                        InCallVo lastVo = YlsCallManager.getInstance().getCallList().getLast();
                        YlsCallManager.getInstance().unHoldCall(getContext(), firstVo);
                        if (firstVo != lastVo) {
                            LogUtil.w("通话界面 多通通话 解hold当前通话 callId=" + firstVo.getCallId());
                        } else {
                            LogUtil.w("通话界面 恢复一通通话 解hold当前通话 callId=" + firstVo.getCallId());
                        }
                    }
                    if (inCallPresenter != null) {
                        inCallPresenter.refresh();
                    }
                    resumeBluetoothSco();
                }

                @RequiresApi(api = Build.VERSION_CODES.Q)
                @Override
                public void onAnswerBusy() {
                    LogUtil.w("通话界面 系统来电拒接");
                    Utils.moveToFront(activity);
                    resumeBluetoothSco();
                }
            });
        }
    }

    private void resumeBluetoothSco() {
        new Handler().postDelayed(() -> MediaUtil.getInstance().openSco(), 1500);
    }

    public void setListener() {
        callDialPad.setCallBack(action -> {
            if (action == CallDialPad.DIAL_PAD) {
                inCallPresenter.dialPad();
            } else if (action == CallDialPad.ATTENDED_TRANSFER) {
                inCallPresenter.transfer(0);
            } else if (action == CallDialPad.BLIND_TRANSFER) {
                inCallPresenter.transfer(1);
            } else if (action == CallDialPad.HOLD) {
                LogUtil.w("通话界面 按下hold按钮");
                inCallPresenter.hold();
            } else if (action == CallDialPad.RECORD) {
                inCallPresenter.record();
            } else if (action == CallDialPad.CANCEL) {
                inCallPresenter.cancelTransfer();
            } else if (action == CallDialPad.TRANSFER_CONFIRM) {
                inCallPresenter.confirmTransfer();
            } else if (action == CallDialPad.END_CALL) {
                inCallPresenter.hangupPhone();
            } else if (action == CallDialPad.MUTE) {
                inCallPresenter.mute();
            } else if (action == CallDialPad.AUDIO) {
                inCallPresenter.speaker();
            }
        });
        //切换通话
        mLlSwitch.setOnClickListener(v -> {
            boolean isTransfer = YlsCallManager.getInstance().isInCall() && YlsCallManager.getInstance().getCallList().getFirst().isTransfer();
            // 转移中无法切换通话
            if (isTransfer) return;
            mInCallHoldContact.setClickable(false);
            inCallPresenter.switchCallWaiting();
            mInCallHoldContact.setClickable(true);
        });
        mIvReportCallQuality.setOnClickListener(v -> initCallQuality());

    }

    @SuppressWarnings("rawtypes")
    private void initCallQuality() {
        callQualityDialog = new CallQualityDialog(getActivity());
        callQualityDialog.show();
        scheduledExec = Executors.newSingleThreadScheduledExecutor();
        Runnable task = () -> {
            CallQualityVo callQualityVo = YlsCallManager.getInstance().getCallQuality();
            if (callQualityVo == null) {
                callQualityVo = new CallQualityVo("--", "--", "--", "--", "--", "--", "--", "--", "--", "--", "--", "--", "--", "--", "--");
            }
            EventBus.getDefault().post(new CallQualityEvent(callQualityVo));
        };
        scheduledExec.scheduleAtFixedRate(task, 0, 1000 * 3, TimeUnit.MILLISECONDS);
        callQualityDialog.setOnDismissListener(dialog -> {
            callQualityDialog = null;
            if (scheduledExec != null && !scheduledExec.isShutdown()) {
                List<Runnable> runnableList = scheduledExec.shutdownNow();
                if (CommonUtil.isListNotEmpty(runnableList)) {
                    for (Runnable runnable : runnableList) {
                        FutureTask cancelTask = (FutureTask) runnable;
                        if (!cancelTask.isCancelled()) {
                            cancelTask.cancel(true);
                        }
                    }
                }
                scheduledExec = null;
            }
        });
    }

    @Override
    public void updateInCallDialPad(InCallVo inCallVo) {
        if (inCallVo.isAccept()) {
            if (inCallVo.isTransfer()) {
                callDialPad.transferConnected(inCallVo);
            } else {
                callDialPad.updateCallDialPad(inCallVo);
            }
        } else {
            callDialPad.calling();
        }
    }

    @Override
    public void ring(InCallVo inCallVo) {
        RingFragment ringFragment = new RingFragment();
        FragmentActivity activity = getActivity();
        if (activity != null) {
            FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.call_container, ringFragment);
            ft.commit();
        }
    }

    @Override
    public void calling(InCallVo inCallVo) {
        //避免手动进入添加页面，又马上退出后的操作，影响其他功能
        mInCallHoldContact.setVisibility(View.GONE);
        mLlSwitch.setVisibility(View.GONE);
        initCallContactItem(inCallVo);
        RemoteControlUtil.getInstance().setClickCount(1);
        updateInCallDialPad(inCallVo);
    }

    @Override
    public void onResume() {
        super.onResume();
        StatusBarUtil.transparentStatusBar(activity);
    }

    private InCallVo getInCallModel() {
        if (YlsCallManager.getInstance().isInCall()) {
            return YlsCallManager.getInstance().getCallList().getFirst();
        } else {
            activity.finish();
            return null;
        }
    }

    @Override
    public void singleCall(InCallVo inCallVo) {
        mInCallHoldContact.setVisibility(View.GONE);
        mLlSwitch.setVisibility(View.GONE);
        initCallContactItem(inCallVo);
        RemoteControlUtil.getInstance().setClickCount(1);
        updateInCallDialPad(inCallVo);
    }

    /**
     * 设置通话方的信息
     *
     * @param inCallVo
     */
    private void initCallContactItem(InCallVo inCallVo) {
        mInCallCenterContact.setVisibility(View.VISIBLE);
        mInCallCenterContact.setContact(inCallVo, true);
        mInCallCenterContact.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        mInCallCenterContact.setTimerText(inCallVo);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void callWaitingRing(LinkedList<InCallVo> list) {
        if (App.getInstance().isBackground()) {
            Utils.moveToFront(activity);
        }
        if (fragment == null) {
            fragment = new CallWaitingFragment();
            fragment.setCallWaitingInterface(() -> {
                activity.getSupportFragmentManager().popBackStackImmediate();
                fragment = null;
            });
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.call_container, fragment);
            ft.addToBackStack(null);
            ft.commit();
        }
    }

    @Override
    public void transferCall(LinkedList<InCallVo> list) {
        //transferVo 转移到的号码
        //transferToVo   被转移号码
        InCallVo transferVo = list.get(1);
        InCallVo transferToVo = list.getFirst();
        LogUtil.w("通话界面 转移来电UI transferVo=" + transferVo.toString()
                + " transferToVo=" + transferToVo.toString());
        //左边是被转移的号码
        mInCallHoldContact.setVisibility(View.VISIBLE);
        mInCallHoldContact.setContact(transferVo, false);
        mInCallHoldContact.setTimerText(transferVo);
        //有三通通话时,最后一通是顶部hold住的
        if (list.size() == 3) {
            InCallVo lastVo = list.getLast();
            setTopHoldContact(lastVo);
        }
        //中间是转移号码
        mInCallCenterContact.setContact(transferToVo, true);
        mInCallCenterContact.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        mInCallCenterContact.setTimerText(transferToVo);
        updateInCallDialPad(transferToVo);
    }

    @Override
    public void threeCall(LinkedList<InCallVo> list) {
        //第一通转移目的地，第二通被转移的，第三通call waiting
        LogUtil.w("通话界面 call waiting+转移 三通通话UI " + list.toString());
        transferCall(list);
    }

    @Override
    public void callWaitingConnected(LinkedList<InCallVo> list) {
        InCallVo inCallVo = list.getFirst();
        InCallVo lastVo = list.getLast();
        initCallContactItem(inCallVo);
        setTopHoldContact(lastVo);
        mInCallHoldContact.setVisibility(View.GONE);
        updateInCallDialPad(inCallVo);
    }


    private void setTopHoldContact(InCallVo topHoldContact) {
        mLlSwitch.setVisibility(View.VISIBLE);
        String topPhotoUri = null;
        mCivSwitchAvatar.loadCirclePhotoUrl(topPhotoUri);
        String topHoldName = getCallName(topHoldContact.getCallName(), topHoldContact.getCallNumber());
        mTvSwitchCallName.setText(topHoldName);
        mTvIncallTime.setBase(SystemClock.elapsedRealtime() - (System.currentTimeMillis() - topHoldContact.getHoldStartTime()));
        mTvIncallTime.setFormat("%s");
        mTvIncallTime.start();
    }

    private String getCallName(String name, String number) {
        if (TextUtils.isEmpty(name)) {
            name = number;
        }
        return name;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ((CallContainerActivity) activity).setPhoneStateCallback(null);
        if (callQualityDialog != null && callQualityDialog.isShowing()) {
            callQualityDialog.dismiss();
        }
        if (inCallPresenter != null) {
            inCallPresenter.detachView();
        }
    }


    @Override
    public void setPresenter(InCallContract.Presenter presenter) {

    }

}