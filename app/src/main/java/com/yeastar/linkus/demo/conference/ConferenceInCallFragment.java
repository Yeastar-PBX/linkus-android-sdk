package com.yeastar.linkus.demo.conference;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.yeastar.linkus.constant.YlsConstant;
import com.yeastar.linkus.demo.Constant;
import com.yeastar.linkus.demo.R;
import com.yeastar.linkus.demo.call.CallContainerActivity;
import com.yeastar.linkus.demo.call.CallManager;
import com.yeastar.linkus.demo.call.CallingContract;
import com.yeastar.linkus.demo.call.InCallRelatedFragment;
import com.yeastar.linkus.demo.eventbus.AgentEvent;
import com.yeastar.linkus.demo.eventbus.AudioRouteEvent;
import com.yeastar.linkus.demo.eventbus.ConferenceStatusEvent;
import com.yeastar.linkus.demo.eventbus.ConnectionChangeEvent;
import com.yeastar.linkus.demo.utils.StatusBarUtil;
import com.yeastar.linkus.demo.utils.TimeUtil;
import com.yeastar.linkus.demo.utils.ToastUtil;
import com.yeastar.linkus.demo.widget.ActionSheetDialog;
import com.yeastar.linkus.demo.widget.AvatarImageView;
import com.yeastar.linkus.nativecode.YlsCall;
import com.yeastar.linkus.service.base.vo.ResultVo;
import com.yeastar.linkus.service.call.YlsCallManager;
import com.yeastar.linkus.service.call.vo.InCallVo;
import com.yeastar.linkus.service.conference.YlsConferenceManager;
import com.yeastar.linkus.service.conference.vo.ConferenceMemberVo;
import com.yeastar.linkus.service.conference.vo.ConferenceVo;
import com.yeastar.linkus.service.log.LogUtil;
import com.yeastar.linkus.service.login.YlsLoginManager;
import com.yeastar.linkus.utils.BetterAsyncTask;
import com.yeastar.linkus.utils.CommonUtil;
import com.yeastar.linkus.utils.MediaUtil;
import com.yeastar.linkus.utils.NetWorkUtil;
import com.yeastar.linkus.utils.SoundManager;
import com.yeastar.linkus.utils.remoteControlUtil.RemoteControlUtil;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import kale.adapter.CommonAdapter;
import kale.adapter.item.AdapterItem;

public class ConferenceInCallFragment extends InCallRelatedFragment implements View.OnClickListener {

    private TextView nameTv = null;
    private Chronometer timeTv = null;
    private ImageView speakerIv = null;
    private ImageView muteIv = null;
    private GridView memberGv = null;
    private Button exitBtn = null;
    private CommonAdapter<ConferenceMemberVo> commonAdapter;
    private ConferenceVo conferenceVo = null;
    private String conferenceId = null;
    private String myExtension;
    private InCallVo inCallVo;
    private boolean isAdmin = false;
    private AvatarImageView adminPhotoIv;
    private TextView adminNameTv;
    private TextView adminNumberTv;
    private ImageView adminCallStatusIv;

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void handleConnectionChange(ConnectionChangeEvent connectionChangeEvent) {
        txLevelTv.setText(R.string.sip_nonetwork);
        txLevelTv.setVisibility(NetWorkUtil.isNetworkConnected(activity) ? View.GONE : View.VISIBLE);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleConferenceStatus(ConferenceStatusEvent conferenceStatusEvent) {
        String conferenceId = conferenceStatusEvent.getConferenceId();
        if (YlsConferenceManager.getInstance().getConferenceVo() == null
                || !CommonUtil.isListNotEmpty(YlsConferenceManager.getInstance().getConferenceVo().getMemberList())) {
            LogUtil.w("ConferenceInCallFragment handleConferenceStatus finish");
            activity.finish();
            return;
        }
        conferenceVo = (ConferenceVo) CommonUtil.deepClone(YlsConferenceManager.getInstance().getConferenceVo());
//        LogUtil.w("handleConferenceStatus ConferenceVo=%s", conferenceVo);
        if (conferenceVo != null && conferenceId.equals(conferenceVo.getConferenceId())) {
            String tmpExtension = conferenceStatusEvent.getExtension();
            int confStatus = conferenceStatusEvent.getConfstatus();
            //status: 0.响铃 1.进入会议室 2.离开会议室 3.静音 4.取消静音 5.异常掉线通知 6.未接来电通知
            LogUtil.w("ConferenceInCallFragment confStatus  " + tmpExtension + "   " + confStatus);
            if (myExtension.equals(tmpExtension) && confStatus == YlsConstant.SIP_CONFERENCE_JOIN) {
                initInCallModel();
            }
            notifyUi();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAgentEvent(AgentEvent event) {
        int resultCode = event.getResultCode();
        Intent data = event.getData();
        if (resultCode == Activity.RESULT_OK) {
            if (data != null) {
                String number = data.getStringExtra(Constant.EXTRA_NUMBER);
                YlsConferenceManager.getInstance().inviteConferenceMemberBlock(getContext(), conferenceVo.getConferenceId(), number);
            }
        } else {
            conferenceVo = (ConferenceVo) CommonUtil.deepClone(YlsConferenceManager.getInstance().getConferenceVo());
            if (conferenceVo != null) {
                YlsConferenceManager.getInstance().addNullMember(conferenceVo.getMemberList());
            }
            notifyUi();
        }
        StatusBarUtil.transparentStatusBar(getActivity());
        activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handlerAudioRoute(AudioRouteEvent event) {
        LogUtil.w("会议通话界面 收到AudioRoute事件通知");
        updateAudioRoute();
    }

    public ConferenceInCallFragment() {
        super(R.layout.fragment_conference_incall);
    }

    @Override
    public void findView(View view) {
        StatusBarUtil.transparentStatusBar(getActivity());
        super.findView(view);
        initInCallModel();
        initPhoneState();
        initRemoteControl();
        initView(view);

        myExtension = YlsLoginManager.getInstance().getMyExtension();
        conferenceVo = (ConferenceVo) CommonUtil.deepClone(YlsConferenceManager.getInstance().getConferenceVo());
        if (conferenceVo != null) {
            LogUtil.w("conferenceVo not null");
            String name = TextUtils.isEmpty(conferenceVo.getName()) ? getString(R.string.conference_conference) : conferenceVo.getName();
            nameTv.setText(name);
            initAdminUi();
            initAdapter();
            memberGv.setAdapter(commonAdapter);
        } else {
            LogUtil.w("会议室数据为空");
            activity.finish();
        }
        setStartTime();
        initData();
        setListener();
    }

    private void initAdminUi() {
        String admin = conferenceVo.getAdmin();
        adminPhotoIv.setImageResource(R.mipmap.default_contact_avatar);
        adminNameTv.setText(getString(R.string.conference_host, admin));
        adminNumberTv.setText(admin);
        adminCallStatusIv.setImageResource(R.drawable.conference_status_succ);
    }

    private void initInCallModel() {
        LogUtil.w("initInCallModel()");
        if (YlsCallManager.getInstance().isInCall()) {
            inCallVo = YlsCallManager.getInstance().getCallList().getFirst();
            LogUtil.w("initInCallModel()-->" + inCallVo);
        }
    }

    private void initRemoteControl() {
        RemoteControlUtil.getInstance().setRemoteControlListener(new RemoteControlUtil.OnRemoteControlListener() {
            @Override
            public void onPlay() {
            }

            @Override
            public void onPause() {
                exitConference();
            }
        });
    }

    private void initPhoneState() {
        if (getActivity() instanceof CallContainerActivity) {
            ((CallContainerActivity) getActivity()).setPhoneStateCallback(new CallingContract.PhoneStateCallback() {
                @Override
                public void onRing() {
                    LogUtil.w("会议室界面 系统来电响铃");
                    activity.moveTaskToBack(true);
                }

                @Override
                public void onCalling() {
                    LogUtil.w("会议室界面 接听系统来电");
                    //双向静音
                    if (inCallVo != null) {
                        YlsCall.doubleMute(inCallVo.getCallId());
                    }
                }

                @RequiresApi(api = Build.VERSION_CODES.Q)
                @Override
                public void onHangup() {
                    LogUtil.w("会议室界面 系统来电接听完毕");
                    if (inCallVo != null) {
                        YlsCall.doubleUnmute(inCallVo.getCallId());
                    }
                    resumeBluetoothSco();
                }

                @RequiresApi(api = Build.VERSION_CODES.Q)
                @Override
                public void onAnswerBusy() {
                    LogUtil.w("会议室界面 系统来电拒接");
                    resumeBluetoothSco();
                }
            });
        }
    }

    private void resumeBluetoothSco() {
        new Handler().postDelayed(() -> MediaUtil.getInstance().openSco(), 1500);
    }

    private void initView(View view) {
        nameTv = view.findViewById(R.id.conference_name_tv);
        timeTv = view.findViewById(R.id.conference_time_tv);
        speakerIv = view.findViewById(R.id.conference_speaker_iv);
        muteIv = view.findViewById(R.id.conference_mute_iv);
        memberGv = view.findViewById(R.id.conference_member_gv);
        exitBtn = view.findViewById(R.id.conference_exit_btn);

        adminPhotoIv = view.findViewById(R.id.admin_photo_civ);
        adminNameTv = view.findViewById(R.id.admin_name_tv);
        adminNumberTv = view.findViewById(R.id.admin_number_tv);
        adminCallStatusIv = view.findViewById(R.id.admin_call_status_iv);

        new CountDownTimer(2000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                exitBtn.setEnabled(false);
                exitBtn.setAlpha(0.5f);
            }

            @Override
            public void onFinish() {
                exitBtn.setEnabled(true);
                exitBtn.setAlpha(1f);
            }
        }.start();
    }

    public void initAdapter() {
        String admin = conferenceVo.getAdmin();
        List<ConferenceMemberVo> withoutAdmin = conferenceVo.getMemberListWithoutAdmin(admin);
        commonAdapter = new CommonAdapter<ConferenceMemberVo>(withoutAdmin, 1) {

            @NonNull
            @Override
            public AdapterItem<ConferenceMemberVo> createItem(Object o) {
                return new ConferenceInCallItem(new ConferenceInCallItem.OnConferenceInCallListener() {
                    @Override
                    public void operate(final ConferenceMemberVo memberVo) {

                        ActionSheetDialog actionSheetDialog = new ActionSheetDialog(activity)
                                .builder().setCancelable(true).setCanceledOnTouchOutside(true);
                        if (isAdmin()) {
                            //管理员对自己有静音所有，取消静音所有
                            if (memberVo.getNumber().equals(myExtension)) {
                                actionSheetDialog.addSheetItem(R.string.conference_mute_all, ActionSheetDialog.SheetItemColor.Blue, which -> muteAllMember())
                                        .addSheetItem(R.string.conference_unmute_all, ActionSheetDialog.SheetItemColor.Blue, which -> unMuteAllMember());
                            } else {//管理员对他人有静音/取消静音，删除成员
                                if (canMute(memberVo)) {//只有进入会议室中的成员可以静音
                                    actionSheetDialog.addSheetItem(memberVo.isMute() ? R.string.conference_unmute : R.string.conference_mute, ActionSheetDialog.SheetItemColor.Blue, which -> muteMember(memberVo));
                                }
                                actionSheetDialog.addSheetItem(R.string.public_delete, ActionSheetDialog.SheetItemColor.Blue, which -> kickMember(memberVo));
                            }
                        }
                        //异常用户大家都可以再次邀请,掉线用户则不可再次邀请
                        if (memberVo.getStatus() == YlsConstant.SIP_CONFERENCE_DISCONNECTED) {
                            actionSheetDialog.addSheetItem(R.string.conference_call_again, ActionSheetDialog.SheetItemColor.Blue, which -> reInviteMember(memberVo));
                        }
                        if (actionSheetDialog.getSheetItemCount() > 0) {
                            actionSheetDialog.show();
                        }

                    }

                    @Override
                    public void onAddMemberClick() {
                        if (checkLoginStatusInvalid()) {
                            return;
                        }
                        ConferenceAddActivity.start(activity, conferenceVo, Constant.IN_CONFERENCE);
                    }
                });
            }
        };
    }

    private boolean canMute(ConferenceMemberVo memberVo) {
        return memberVo.getStatus() == 1 || memberVo.getStatus() == 3 || memberVo.getStatus() == 4;
    }

    private void muteAllMember() {
        if (checkLoginStatusInvalid() || checkConferenceIdValid()) {
            return;
        }
        ResultVo resultVo = YlsConferenceManager.getInstance().muteAllConferenceMemberBlock(getContext(), conferenceId, myExtension, true);
        switch (resultVo.getCode()) {
            case YlsConstant.CONFERENCE_PERMISSION_ERROR:
                ToastUtil.showToast("非会议室管理员不能操作!");
                break;
            case YlsConstant.CONFERENCE_NULL_ERROR:
                ToastUtil.showToast("会议室为空!");
                break;
            case YlsConstant.SDK_LOGIN_DISABLE:
            case YlsConstant.SDK_NETWORK_DISABLE:
                ToastUtil.showToast(R.string.connectiontip_connect_fail);
                break;
            default:
                break;
        }
    }

    private void unMuteAllMember() {
        if (checkLoginStatusInvalid() || checkConferenceIdValid()) {
            return;
        }
        ResultVo resultVo = YlsConferenceManager.getInstance().muteAllConferenceMemberBlock(getContext(), conferenceId, myExtension, false);
        switch (resultVo.getCode()) {
            case YlsConstant.CONFERENCE_PERMISSION_ERROR:
                ToastUtil.showToast("非会议室管理员不能操作!");
                break;
            case YlsConstant.CONFERENCE_NULL_ERROR:
                ToastUtil.showToast("会议室为空!");
                break;
            case YlsConstant.SDK_LOGIN_DISABLE:
            case YlsConstant.SDK_NETWORK_DISABLE:
                ToastUtil.showToast(R.string.connectiontip_connect_fail);
                break;
            default:
                break;
        }
    }

    private void reInviteMember(final ConferenceMemberVo vo) {
        if (checkLoginStatusInvalid() || checkConferenceIdValid() || checkMemberValid(vo)) {
            return;
        }
        YlsConferenceManager.getInstance().reInviteConferenceMemberBlock(getContext(), conferenceId, vo.getNumber());
    }

    private void muteMember(final ConferenceMemberVo vo) {
        if (checkLoginStatusInvalid() || checkConferenceIdValid() || checkMemberValid(vo)) {
            return;
        }
        final boolean mute = !vo.isMute();
        ResultVo resultVo = YlsConferenceManager.getInstance().muteConferenceMemberBlock(getContext(), conferenceId, vo.getNumber(), mute);
        switch (resultVo.getCode()) {
            case YlsConstant.CONFERENCE_PERMISSION_ERROR:
                ToastUtil.showToast("非会议室管理员不能操作!");
                break;
            case YlsConstant.CONFERENCE_NULL_ERROR:
                ToastUtil.showToast("会议室为空!");
                break;
            case YlsConstant.SDK_LOGIN_DISABLE:
            case YlsConstant.SDK_NETWORK_DISABLE:
                ToastUtil.showToast(R.string.connectiontip_connect_fail);
                break;
            default:
                break;
        }
    }

    private void kickMember(final ConferenceMemberVo vo) {
        if (checkLoginStatusInvalid() || checkConferenceIdValid() || checkMemberValid(vo)) {
            return;
        }
        ResultVo resultVo = YlsConferenceManager.getInstance().kickConferenceMemberBlock(getContext(), conferenceId, vo.getNumber());
        switch (resultVo.getCode()) {
            case YlsConstant.CONFERENCE_PERMISSION_ERROR:
                ToastUtil.showToast("非会议室管理员不能操作!");
                break;
            case YlsConstant.CONFERENCE_NULL_ERROR:
                ToastUtil.showToast("会议室为空!");
                break;
            case YlsConstant.SDK_LOGIN_DISABLE:
            case YlsConstant.SDK_NETWORK_DISABLE:
                ToastUtil.showToast(R.string.connectiontip_connect_fail);
                break;
            default:
                break;
        }
    }

    private boolean isAdmin() {
        return isAdmin;
    }

    public void setListener() {
        adminPhotoIv.setOnClickListener(this);
        speakerIv.setOnClickListener(this);
        muteIv.setOnClickListener(this);
        exitBtn.setOnClickListener(this);
    }

    private void initData() {
        if (conferenceVo != null) {
            conferenceId = conferenceVo.getConferenceId();
            isAdmin = conferenceVo.getAdmin().equals(myExtension);
        }
        initInCallModel();
        if (inCallVo != null) {
            changeMute();
            SoundManager.getInstance().setAudioRoute(SoundManager.getInstance().getAudioRoute());
        }
        updateAudioRoute();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.admin_photo_civ) {
            if (!isAdmin()) {
                return;
            }
            new ActionSheetDialog(activity)
                    .builder()
                    .setCancelable(true)
                    .setCanceledOnTouchOutside(true)
                    .addSheetItem(R.string.conference_mute_all, ActionSheetDialog.SheetItemColor.Blue, which -> muteAllMember())
                    .addSheetItem(R.string.conference_unmute_all, ActionSheetDialog.SheetItemColor.Blue, which -> unMuteAllMember())
                    .show();
        } else if (i == R.id.conference_speaker_iv) {
            if (MediaUtil.getInstance().isBTConnected()) {
                CallManager.getInstance().showSoundChannelSelector(activity);
            } else {
                SoundManager.getInstance().controlSpeaker(audioManager, activity);
                updateAudioRoute();
            }
        } else if (i == R.id.conference_mute_iv) {
            if (!checkLoginStatusInvalid() && inCallVo != null) {
                YlsCallManager.getInstance().mute(inCallVo);
                changeMute();
            }

        } else if (i == R.id.conference_exit_btn) {
            if (inCallVo != null) {//此时可能由于通知事件的先后顺序导致前面拿到的inCallModel为空,需要再获取一次
                initInCallModel();
            }
            LogUtil.w("手动退出会议室");
            exitConference();

        }
    }

    private void updateAudioRoute() {
        LogUtil.i("updateAudioRoute mAudioRoute=%d", SoundManager.getInstance().getAudioRoute());
        //有蓝牙耳机时
        if (MediaUtil.getInstance().isBTConnected()) {
            if (SoundManager.getInstance().isBluetoothAudio()) {
                speakerIv.setImageResource(R.drawable.icon_bluetooth);
            } else if (SoundManager.getInstance().isSpeakerOn()) {
                speakerIv.setImageResource(R.drawable.icon_speaker_bluetooth);
            } else if (SoundManager.getInstance().isWiredHeadset()) {
                speakerIv.setImageResource(R.drawable.icon_headset);
            } else {
                speakerIv.setImageResource(R.drawable.icon_earpiece);
            }
        } else {
            if (SoundManager.getInstance().isSpeakerOn()) {
                speakerIv.setImageResource(R.drawable.incall_speaker_on);
            } else {
                speakerIv.setImageResource(R.drawable.icon_speaker);
            }
        }


    }

    /**
     * 注意:
     * 点击退出会议按钮结束会议操作
     * 主持人网络连上的情况,先执行退出会议再挂断通话,退出不成功不结束通话界面
     * 其他情况直接挂断通话即可
     */
    private void exitConference() {
        if (NetWorkUtil.isNetworkConnected(activity) && isAdmin()) {
            new BetterAsyncTask<Void, Void, ResultVo>() {
                @Override
                public ResultVo doInBackground(Void... voids) {
                    return YlsConferenceManager.getInstance().endConferenceBlock(getContext(), conferenceId, YlsLoginManager.getInstance().getMyExtension());
                }

                @Override
                public void onPostExecute(ResultVo resultVo) {
                    super.onPostExecute(resultVo);
                    if (resultVo.getCode() != YlsConstant.OPERATE_SUCCESS) {
                        ToastUtil.showLongToast(R.string.connectiontip_connect_fail);
                    } else {
                        doHangup();
                    }
                }
            }.executeParallel();
        } else {
            doHangup();
        }
    }

    private void doHangup() {
        if (inCallVo != null) {
            YlsCallManager.getInstance().hangUpCall(activity, inCallVo.getCallId());
        }
        YlsConferenceManager.getInstance().setConferenceVo(null);
        activity.finish();
    }

    private void setStartTime() {
        long startTime;
        //刚进入的会议或者异常会议返回
        if (inCallVo == null || inCallVo.getStartTime() == 0) {
            startTime = System.currentTimeMillis();
        } else {
            startTime = inCallVo.getStartTime();
        }
        timeTv.setBase(SystemClock.elapsedRealtime() - (System.currentTimeMillis() - startTime));
        timeTv.setFormat("%s");
        timeTv.setOnChronometerTickListener(chronometer -> TimeUtil.setChronometerFormat(timeTv));
        timeTv.start();
        timeTv.setVisibility(View.VISIBLE);
        speakerIv.setAlpha(1f);
        muteIv.setAlpha(1f);
        exitBtn.setAlpha(1f);
        speakerIv.setEnabled(true);
        muteIv.setEnabled(true);
        exitBtn.setEnabled(true);
    }

    private void changeMute() {
        if (inCallVo.isMute()) {
            muteIv.setImageResource(R.drawable.incall_mute_on);
        } else {
            muteIv.setImageResource(R.drawable.icon_mute);
        }
    }

    @Override
    public void onDestroy() {
        if (isAdmin() && YlsConferenceManager.getInstance().getConferenceVo() == null) {
            YlsConferenceManager.getInstance().setEndConferenceTime(System.currentTimeMillis());
        }
        timeTv.stop();
        ((CallContainerActivity) activity).setPhoneStateCallback(null);
        super.onDestroy();
    }

    private boolean checkConferenceIdValid() {
        return TextUtils.isEmpty(conferenceId);
    }

    private boolean checkMemberValid(ConferenceMemberVo vo) {
        return vo == null;
    }

    /**
     * @return true/false 未登录/已登录
     */
    private boolean checkLoginStatusInvalid() {
        if (!YlsLoginManager.getInstance().isLoginEd()) {
            ToastUtil.showToast(R.string.connectiontip_connect_fail);
            return true;
        }
        return false;
    }

    private void notifyUi() {
        initAdminUi();
        commonAdapter.setData(conferenceVo.getMemberListWithoutAdmin(conferenceVo.getAdmin()));
        commonAdapter.notifyDataSetChanged();
        memberGv.setAdapter(commonAdapter);
    }

}