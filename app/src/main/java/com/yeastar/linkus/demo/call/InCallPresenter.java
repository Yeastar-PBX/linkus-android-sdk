package com.yeastar.linkus.demo.call;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import com.yeastar.linkus.constant.CallType;
import com.yeastar.linkus.demo.Constant;
import com.yeastar.linkus.demo.R;
import com.yeastar.linkus.demo.utils.ToastUtil;
import com.yeastar.linkus.nativecode.YlsCall;
import com.yeastar.linkus.service.call.YlsCallManager;
import com.yeastar.linkus.service.call.vo.InCallVo;
import com.yeastar.linkus.service.call.vo.LoginResultVo;
import com.yeastar.linkus.service.log.LogUtil;
import com.yeastar.linkus.service.login.YlsLoginManager;
import com.yeastar.linkus.utils.CommonUtil;
import com.yeastar.linkus.utils.MediaUtil;
import com.yeastar.linkus.utils.SoundManager;

import java.util.LinkedList;

/**
 * Created by ted on 17-4-19.
 */

public class InCallPresenter extends InCallContract.Presenter {

    private InCallContract.View view;
    private Context context;
    private InCallVo inCallVo;
    private LinkedList<InCallVo> list;
    private AudioManager audioManager;

    public InCallPresenter(InCallContract.View view, Context context) {
        this.view = view;
        this.context = context;
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        list = YlsCallManager.getInstance().getCallList();
        if (CommonUtil.isListNotEmpty(list)) {
            inCallVo = list.getFirst();
        }
        attachView(view);
    }

    @Override
    void callOut() {
        if (inCallVo == null) {
            return;
        }
        String number = inCallVo.getCallNumber();
        String calleeName = inCallVo.getCallName();
        String routPrefix = inCallVo.getTrunk();
        String trunkName = "";
        CallManager.getInstance().makeCall(context, calleeName, number, trunkName, routPrefix, inCallVo.getVideoStatus());
        view.singleCall(inCallVo);
    }

    @Override
    public void transfer(int transferType) {
        LogUtil.w("通话界面 按转移按钮 type=%d (0:咨询转 1:盲转)", transferType);
        if (isEndCall()) {
            return;
        }
        YlsCallManager.getInstance().holdCall(inCallVo);
        YlsCallManager.getInstance().setInTransfer(true);
        if (isViewAttached()) {
            InCallTransferFragment inCallTransferFragment = new InCallTransferFragment();
            Bundle bundle = new Bundle();
            bundle.putString(Constant.EXTRA_NUMBER, inCallVo.getCallNumber());
            bundle.putInt(Constant.EXTRA_DATA, transferType);
            inCallTransferFragment.setArguments(bundle);
            replaceFragment(inCallTransferFragment);
        }
    }

    private void replaceFragment(Fragment fragment) {
        replaceFragment(fragment, true);
    }

    private void replaceFragment(Fragment fragment, boolean anim) {
        if (context != null && context instanceof FragmentActivity) {
            FragmentTransaction ft = ((FragmentActivity) context).getSupportFragmentManager().beginTransaction();
            if (anim) {
                ft.setCustomAnimations(
                        R.anim.anim_fragment_enter,
                        R.anim.anim_fragment_exit,
                        R.anim.anim_fragment_enter,
                        R.anim.anim_fragment_exit
                );
            }
            ft.replace(R.id.call_container, fragment);
            ft.addToBackStack(null);
            ft.commit();
        }
    }

    @Override
    public void mute() {
        LogUtil.w("通话界面 按下静音按钮");
        if (isEndCall()) {
            return;
        }
        if (inCallVo.isAccept()) {
            if (inCallVo.isMute()) {
                YlsCallManager.getInstance().unMute(inCallVo);
            } else {
                YlsCallManager.getInstance().mute(inCallVo);
            }
        }
        if (isViewAttached()) {
            view.updateInCallDialPad(inCallVo);
        }
    }

    @Override
    public void record() {
        LogUtil.w("通话界面 按下录音按钮");
        if (isEndCall()) {
            return;
        }
        singleRecord(inCallVo);
    }

    public void singleRecord(InCallVo vo) {
        if (vo == null) return;
        //hold时录音，提示先unHold
        if (vo.isHold() && !vo.isRecord()) {
            ToastUtil.showToast(R.string.call_tip_hold);
            return;
        } else if (vo.isHold() && vo.isRecord()) {
            //已经在录音，保持中，结束录音提示先取消保持
            ToastUtil.showToast(R.string.call_tip_hold);
            return;
        }
        LoginResultVo loginResultVo = YlsLoginManager.getInstance().getLoginResultVo();
//        LoginResultModel loginResultModel = LoginManager.getInstance().getLoginResultModel();
        if (loginResultVo == null) {
            ToastUtil.showToast(R.string.call_record_faild);
            return;
        }
        String recordCode = TextUtils.isEmpty(loginResultVo.getRecordcode()) ? "*1" : loginResultVo.getRecordcode();
        int ret = YlsCallManager.getInstance().record(vo.getCallId(), recordCode);
//        int ret = AppSdk3.record(model.getCallId(), recordCode);
        if (ret == -1) {
            ToastUtil.showToast(R.string.call_record_faild);
        }
    }

    @Override
    public void hold() {
        if (isEndCall()) {
            return;
        }
        // 可能出现的场景，a打给b，再呼叫c，进入c的转移，然后hold c这种操作...此时相当于hold单通
        boolean isHold = !inCallVo.isHold();
        if (isHold) {
            YlsCallManager.getInstance().holdCall(inCallVo);
        } else {
            YlsCallManager.getInstance().unHoldCall(context, inCallVo);
        }
        if (isViewAttached()) {
            refresh();
        }
    }

    @Override
    void dialPad() {
        LogUtil.w("通话界面 按下键盘按钮");
        if (isEndCall()) {
            return;
        }
        InCallSignatureFragment inCallSignatureFragment = new InCallSignatureFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(Constant.EXTRA_FROM, inCallVo.getCallId());
        bundle.putString(Constant.EXTRA_NUMBER, inCallVo.getCallNumber());
        inCallSignatureFragment.setArguments(bundle);
        replaceFragment(inCallSignatureFragment);
    }

    @Override
    void speaker() {
        if (isEndCall()) {
            return;
        }
        if (MediaUtil.getInstance().isBTConnected()) {
            LogUtil.w("通话界面 按下音频切换按钮");
            CallManager.getInstance().showSoundChannelSelector(context);
        } else {
            LogUtil.w("通话界面 按下免提按钮");
            audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if (inCallVo != null && audioManager != null) {
                YlsCall.sndSetAudioDevBlock();
                SoundManager.getInstance().controlSpeaker(audioManager, context);
                if (isViewAttached()) {
                    view.updateInCallDialPad(inCallVo);
                }
            }
        }
    }

    @Override
    public void hangupPhone() {
        LogUtil.w("通话界面 挂断当前通话 总通话数=" + YlsCallManager.getInstance().getCallListCount());
        if (isEndCall()) {
            return;
        }
        LogUtil.w("通话界面 挂断当前通话 当前通话=" + inCallVo);
        int callId = inCallVo.getCallId();
        String callNumber = inCallVo.getCallNumber();
        YlsCallManager.getInstance().hangUpCall(context, callId);
    }

    @Override
    public void confirmTransfer() {
        if (isEndCall()) {
            return;
        }
        CallManager.getInstance().confirmTransfer();
    }

    @Override
    public void cancelTransfer() {
        LogUtil.w("通话界面 取消转移");
        if (isEndCall()) {
            return;
        }
        InCallVo hangupModel = list.getFirst();
        if (hangupModel.isTransfer()) {
            YlsCallManager.getInstance().hangUpCall(context, hangupModel.getCallId());
        }
        YlsCallManager.getInstance().setInTransfer(false);
    }

    @Override
    void switchCallWaiting() {
        LogUtil.w("通话界面 切换通话");
        if (isEndCall()) {
            return;
        }
        InCallVo switchModel = list.getFirst();
        YlsCallManager.getInstance().holdCall(switchModel);
        list.removeFirst();
        list.add(switchModel);
        inCallVo = list.getFirst();
        YlsCallManager.getInstance().unHoldCall(context, inCallVo);
//        MediaUtil.getInstance().setAudioRoute(CallManager.getInstance().getAudioRoute(), inCallVo.getCallNumber());
        view.updateInCallDialPad(inCallVo);
        view.callWaitingConnected(list);
    }

    @Override
    public void refresh() {
        if (isViewAttached()) {
            if (isEndCall()) {
                return;
            }
            CallType callType = YlsCallManager.getInstance().getCallType();
            LogUtil.w("refresh:" + callType.name());
            switch (callType) {
                case Null:
                    LogUtil.w("通话界面 refresh callId is null");
                    CallManager.getInstance().finishAllCall(context);
                    ((Activity) context).finish();
                    list = null;
                    inCallVo = null;
                    break;
                case Ring:
                    view.ring(inCallVo);
                    break;
                case Calling:
                    view.calling(inCallVo);
                    break;
                case SingleCall:
                    view.singleCall(inCallVo);
                    break;
                case CallWaiting:
                    view.callWaitingConnected(list);
                    break;
                case CallWaitingRing:
                    view.callWaitingRing(list);
                    break;
                case CallTransfer:
                    view.transferCall(list);
                    break;
                case ThreeCall:
                    view.threeCall(list);
                    break;
            }

        } else {
            list = null;
            inCallVo = null;
        }
    }

    /**
     * 重新获取当前通话对象
     */
    private boolean isEndCall() {
        if (!YlsCallManager.getInstance().isInCall()) {
            CallManager.getInstance().finishAllCall(context);
            ((Activity) context).finish();
            inCallVo = null;
            list = null;
            return true;
        }
        inCallVo = YlsCallManager.getInstance().getCallList().getFirst();
        list = YlsCallManager.getInstance().getCallList();
        return false;
    }
}
