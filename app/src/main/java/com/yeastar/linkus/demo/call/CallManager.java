package com.yeastar.linkus.demo.call;

import static com.yeastar.linkus.demo.utils.Utils.isShowBannerNotification;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.core.BasePopupView;
import com.yeastar.linkus.constant.YlsConstant;
import com.yeastar.linkus.demo.App;
import com.yeastar.linkus.demo.Constant;
import com.yeastar.linkus.demo.R;
import com.yeastar.linkus.demo.call.Audio.AudioPopupView;
import com.yeastar.linkus.demo.eventbus.AudioRouteEvent;
import com.yeastar.linkus.demo.eventbus.CallStateEvent;
import com.yeastar.linkus.demo.eventbus.CallWaitingEvent;
import com.yeastar.linkus.demo.eventbus.ClearNumberEvent;
import com.yeastar.linkus.demo.eventbus.ConnectionChangeEvent;
import com.yeastar.linkus.demo.eventbus.NetWorkLevelEvent;
import com.yeastar.linkus.demo.eventbus.RecordEvent;
import com.yeastar.linkus.demo.utils.MicroPhoneService;
import com.yeastar.linkus.demo.utils.NotificationUtils;
import com.yeastar.linkus.demo.utils.ToastUtil;
import com.yeastar.linkus.demo.utils.Utils;
import com.yeastar.linkus.demo.utils.permission.PermissionRequest;
import com.yeastar.linkus.service.call.YlsCallManager;
import com.yeastar.linkus.service.call.vo.CallStateVo;
import com.yeastar.linkus.service.call.vo.InCallVo;
import com.yeastar.linkus.service.callback.ActionCallback;
import com.yeastar.linkus.service.callback.CallStateCallback;
import com.yeastar.linkus.service.log.LogUtil;
import com.yeastar.linkus.utils.CommonUtil;
import com.yeastar.linkus.utils.MediaUtil;
import com.yeastar.linkus.utils.NetWorkUtil;
import com.yeastar.linkus.utils.SoundManager;
import com.yeastar.linkus.utils.remoteControlUtil.RemoteControlUtil;

import org.greenrobot.eventbus.EventBus;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class CallManager {

    //最后呼出通话时间
    private long lastCallOutTime;
    //通话音频
    private int mAudioRoute = YlsConstant.AUDIO_DEFAULT;
    //
    private BasePopupView basePopupView;
    private Intent microPhoneServiceIntent;
    private boolean isUnfoldDialPad;//通话键盘是否展开
    private WeakReference<Activity> mCallActivity;

    private volatile static CallManager instance;

    public static CallManager getInstance() {
        if (instance == null) {
            synchronized (CallManager.class) {
                if (instance == null) {
                    instance = new CallManager();
                }
            }
        }
        return instance;
    }

    public void initCallBack(Context context) {

        YlsCallManager.getInstance().setCallStateCallback(new CallStateCallback() {
            @Override
            public void onCallStateChange(CallStateVo callStateVo) {
                EventBus.getDefault().post(new CallStateEvent(callStateVo));
            }

            @Override
            public void onNetWorkLevelChange(int callId, int networkLevel) {
                EventBus.getDefault().postSticky(new NetWorkLevelEvent(callId, networkLevel));
            }

            @Override
            public void onConnectChange() {
                EventBus.getDefault().postSticky(new ConnectionChangeEvent());
            }

            @Override
            public void onRecordChange(boolean isRecording) {
                EventBus.getDefault().post(new RecordEvent(isRecording));
            }

        });

        YlsCallManager.getInstance().setActionCallback(new ActionCallback() {
            @Override
            public void onFinishCall() {
                finishAllCall(context);
            }

            @Override
            public void onNewCall() {
                jump2CallActivity(context);
            }

            @Override
            public void onCallWaiting() {
                EventBus.getDefault().post(new CallWaitingEvent());
                SoundManager.getInstance().startPlay(context, YlsConstant.SOUND_CALL_WAITING_TYPE);
            }

            @Override
            public void onMissCallClick() {

            }

            @Override
            public void onStopMicroPhoneService() {

            }

            @Override
            public void onDismissPopupView() {
                dismissPopupView();
            }

            @Override
            public void onNotifyAudioChange() {
                notifyAudioChange();
            }

        });

    }

    public Activity getCallActivity() {
        if (mCallActivity == null) {
            return null;
        }
        return mCallActivity.get();
    }

    public void setCallActivity(WeakReference<Activity> mCallActivity) {
        this.mCallActivity = mCallActivity;
    }

    public Intent getMicroPhoneServiceIntent() {
        return microPhoneServiceIntent;
    }

    public void setMicroPhoneServiceIntent(Intent microPhoneServiceIntent) {
        this.microPhoneServiceIntent = microPhoneServiceIntent;
    }

    public boolean isUnfoldDialPad() {
        return isUnfoldDialPad;
    }

    public void setUnfoldDialPad(boolean unfoldDialPad) {
        isUnfoldDialPad = unfoldDialPad;
    }


    public void finishAllCallActivity() {
        // TODO: 2022/7/14 红米8A出现关不掉通话界面
        if (mCallActivity == null) {
            return;
        }
        Activity activity = mCallActivity.get();
        LogUtil.w("结束通话页面情况: " + (activity != null ? activity.isDestroyed() : "null"));
        if (activity != null && !activity.isDestroyed()) {
            activity.finish();
        }
    }

    private void finishAllTransferFragment(Activity activity) {
        if (activity instanceof FragmentActivity) {
            ((FragmentActivity) activity).getSupportFragmentManager().popBackStack(Constant.TAG_FRAGMENT_CALL, 0);
        } else {
            activity.getFragmentManager().popBackStack(Constant.TAG_FRAGMENT_CALL, 0);
        }
    }

    public void jump2CallActivity(Context context, boolean isNotifyClicked) {
        LogUtil.w("jump2CallActivity isNotifyClicked:%b", isNotifyClicked);
        if (isShowCallNotification(context, isNotifyClicked)) {
            makeNewCallNotification(context, true);
        } else {
            realJump2CallActivity(context, null);
        }
    }

    public void jump2CallActivity(Context context) {
        LogUtil.w("启动来电");
        jump2CallActivity(context, false);
    }

    private boolean isShowCallNotification(Context context, boolean isNotifyClicked) {
        boolean isShowCallNotification = isShowBannerNotification()
                && (Utils.isKeyguardLocked(context)
                || App.getInstance().isBackground())
                && isMiUIShowNotification(isNotifyClicked);
        LogUtil.w("isShowCallNotification:%b", isShowCallNotification);
        return isShowCallNotification;
    }

    //小米必须使用直接后台弹出界面
    private boolean isMiUIShowNotification(boolean isNotifyClicked) {
        return !(Utils.isXiaomi() && isNotifyClicked);
    }

    public void makeNewCallNotification(Context context, boolean isFullScreen) {
        if (microPhoneServiceIntent != null) {
            return;
        }
        LogUtil.w("makeNewCallNotification");
        Intent callIntent = new Intent(context, CallContainerActivity.class);
        callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, callIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        String callName = "";
        String msg = "";
        LinkedList<InCallVo> callList = YlsCallManager.getInstance().getCallList();
        if (CommonUtil.isListNotEmpty(callList)) {
            InCallVo inCallVo = callList.getFirst();
            callName = inCallVo.getCallName();
            if (TextUtils.isEmpty(inCallVo.getConfId())) {
                if (inCallVo.isCallOut() || inCallVo.isAccept()) {
                    msg = context.getString(R.string.call_notification_inacall);
                } else {
                    msg = context.getString(R.string.call_notification);
                }
            } else {
                if (inCallVo.isAccept()) {
                    callName = context.getString(R.string.conference_conference);
                    msg = context.getString(R.string.call_notification_conference_inacall);
                } else {
                    msg = context.getString(R.string.call_notification_conference);
                }
            }
        }
        NotificationUtils.sendNewCallNotification(context, pendingIntent,
                callName, msg, isFullScreen);
    }

    public void makeMicroPhoneNotification(Context context) {
        //如果您的应用以 Android 11 或更高版本为目标平台，且在前台服务中访问摄像头或麦克风，则必须添加前台服务类型 camera 和 microphone。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !App.getInstance().isBackground()) {
            LogUtil.w("makeMicroPhoneNotification");
            Intent callIntent = new Intent(context, CallContainerActivity.class);
            callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, callIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
            LogUtil.w("start microPhoneServiceIntent");
            String title = context.getString(R.string.call_call);
            microPhoneServiceIntent = new Intent(context, MicroPhoneService.class);
            Notification notification = NotificationUtils.getMicroPhoneNotification(context, pendingIntent,
                    title);
            microPhoneServiceIntent.putExtra(Constant.EXTRA_DATA, notification);
            try {
                ContextCompat.startForegroundService(context, microPhoneServiceIntent);
            } catch (Exception exception) {
                LogUtil.e(exception, "startForegroundService");
            }
        }
    }

    public void realJump2CallActivity(Context context, InCallVo inCallVo) {
        LogUtil.w("realJump2CallActivity");
        Intent callIntent = new Intent(context, CallContainerActivity.class);
        callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (inCallVo != null) {
            callIntent.putExtra(Constant.EXTRA_DATA, inCallVo);
        }
        context.startActivity(callIntent);
    }

    //不带路由呼出(voice call)
    public void call(Activity activity, String callee, String name) {
        callWithRoute(activity, callee, "", name);
    }

    //带路由呼出(voice call)
    private void callWithRoute(Activity activity, String callee, String routePrefix, String name) {
        //与最后一通呼出通话时间间隔小于500ms，这通呼出就取消，防止双击导致的呼出多通问题
        synchronized (this) {
            if (System.currentTimeMillis() - lastCallOutTime < 500) {
                LogUtil.w("重复呼出，忽略这通呼出！！");
                return;
            }
            lastCallOutTime = System.currentTimeMillis();
        }

        // 判断当前是否系统通话中
        if (App.getInstance().isInGsmCall()) {
            LogUtil.w("gsm通话中,限制呼出");
        }

        // 判断当前网络是否连接
        if (!NetWorkUtil.isNetworkConnected(activity)) {
            LogUtil.w("callWithRoute isNetworkConnected :" + false);
            ToastUtil.showToast(R.string.nonetworktip_error);
            return;
        }

        // 判断手机号是否存在
        if (TextUtils.isEmpty(callee)) {
            String number = activity.getString(R.string.contacts_number);
            ToastUtil.showToast(activity.getString(R.string.me_tip_empty, number));
            return;
        }
        YlsCallManager.getInstance().registerSip();
        //　发送清除拨号框事件
        EventBus.getDefault().postSticky(new ClearNumberEvent());
        judgeCallPermission(activity, callee, routePrefix, name);
    }


    private void judgeCallPermission(Activity activity, String callee, String routePrefix, String name) {
        PermissionRequest request = new PermissionRequest(activity,
                new PermissionRequest.PermissionCallback() {
                    @Override
                    public void onSuccessful(List<String> permissions) {
                        jumpToInCallFragment(activity, callee, routePrefix, name);
                    }

                    @Override
                    public void onFailure(List<String> permissions) {
                        for (String str : permissions) {
                            LogUtil.w("onFailure:" + str);
                        }
                    }

                });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            request.hasPermission(Manifest.permission.READ_PHONE_STATE, Manifest.permission.RECORD_AUDIO, Manifest.permission.BLUETOOTH_CONNECT);
        } else {
            request.hasPermission(Manifest.permission.READ_PHONE_STATE, Manifest.permission.RECORD_AUDIO);
        }

    }


    //跳转到呼出界面
    private void jumpToInCallFragment(Context context, String callee, String routePrefix, String name) {
        InCallVo inCallVo = new InCallVo(-1, name, callee, routePrefix);
        inCallVo.setCallOut(true);
        inCallVo.setIncomeTime(System.currentTimeMillis());
        realJump2CallActivity(context, inCallVo);
    }

    //来电转移
    public void transfer(Activity activity, String callee, String prefix, String prefixName, boolean isAttended) {
        if (TextUtils.isEmpty(callee)) {
            String number = activity.getString(R.string.contacts_number);
            ToastUtil.showToast(activity.getString(R.string.me_tip_empty, number));
            finishAllTransferFragment(activity);
            return;
        }
        if (isAttended) {
            attendedTransferCall(activity, prefix, prefixName, callee);
        } else {
            blindTransferCall(activity, prefix + callee);
        }
    }

    //咨询转
    private void attendedTransferCall(Activity activity, String prefix, String prefixName, String calleeStr) {
        Object obj = null;//查询转移号码的信息
        String transferName = calleeStr;
        makeTransferCall(activity, transferName, calleeStr, prefixName, prefix, obj);
        finishAllTransferFragment(activity);
    }

    //盲转
    private void blindTransferCall(Context context, String callOutNumber) {
        if (YlsCallManager.getInstance().isInCall()) {
            LogUtil.w("盲转移 通话情况:" + YlsCallManager.getInstance().getCallList() + ";呼出号码:" + callOutNumber);
            InCallVo inCallVo = YlsCallManager.getInstance().getFirstCall();
            String currentCallNumber = inCallVo.getCallNumber();
            YlsCallManager.getInstance().blindTransferCall(context, callOutNumber);
            finishAllTransferFragment((Activity) context);
        }
    }

    public void finishAllCall(Context context) {
        LogUtil.w("finishAllCall");
        CallManager.getInstance().setUnfoldDialPad(false);
        finishAllCallActivity();
        YlsCallManager.getInstance().setInTransfer(false);
        SoundManager.getInstance().stopPlay();
        //此时场景可能正在进行系统通话,呼入linkus拒接,因此不进行sco关闭
        if (!isTelephonyCalling()) {
            MediaUtil.getInstance().closeSco();
        }
        //解决主动挂断通话不会注销audio相关广播
        MediaUtil.getInstance().unRegisterAudioChangeReceiver();
        RemoteControlUtil.getInstance().close(context);
        MediaUtil.getInstance().setCurrentAudioMode(AudioManager.MODE_NORMAL);
        SoundManager.getInstance().setAudioRoute(YlsConstant.AUDIO_DEFAULT);
        if (CallManager.getInstance().getMicroPhoneServiceIntent() != null) {//通话结束取消前台服务
            LogUtil.w("stopService MicroPhoneService");
            context.stopService(CallManager.getInstance().getMicroPhoneServiceIntent());
        }
    }

    public InCallVo findInCallModelByCallId(int callId) {
        LinkedList<InCallVo> list = new LinkedList<>(YlsCallManager.getInstance().getCallList());
        return findInCallModelByCallId(callId, list);
    }

    public InCallVo findInCallModelByCallId(int callId, LinkedList<InCallVo> callList) {
        if (CommonUtil.isListNotEmpty(callList)) {
            for (InCallVo inCallVo : callList) {
                if (callId == inCallVo.getCallId()) {
                    return inCallVo;
                }
            }
        }
        return null;
    }

    public void answerCall(int callId) {
        PermissionRequest request = new PermissionRequest(CallManager.getInstance().getCallActivity(),
                new PermissionRequest.PermissionCallback() {
                    @Override
                    public void onSuccessful(List<String> permissions) {
                        YlsCallManager.getInstance().answerCall(callId);
                        SoundManager.getInstance().stopPlay();
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

    /**
     * 普通来电拨出
     *
     * @param calleeName 被叫名称
     * @param number     号码
     * @param trunkName  前缀名称
     * @param route      路由
     * @return 结果
     */
    public void makeCall(Context context, String calleeName, String number, String trunkName,
                         String route, int isVideo) {
        LogUtil.w("makeCall   calleeName==" + calleeName + "  number==" + number +
                "  trunkName==" + trunkName + "  route==" + route + "  isVideo==" + isVideo);
        boolean netWorkAvailable = NetWorkUtil.isNetworkConnected(context);
        //已经有microphone前台服务不用重新启动
        if (CallManager.getInstance().getMicroPhoneServiceIntent() == null) {
            CallManager.getInstance().makeMicroPhoneNotification(context);
        }
        YlsCallManager.getInstance().makeNewCall(number, netWorkAvailable);
    }

    /**
     * 转移通话呼出
     *
     * @param calleeName 被叫名称
     * @param number     号码
     * @param trunkName  前缀名称
     * @param route      路由
     */
    public void makeTransferCall(Context context, String calleeName, String number, String trunkName,
                                 String route, Object object) {
        LogUtil.w("makeTransferCall   calleeName==" + calleeName + "  number==" + number +
                "  trunkName==" + trunkName + "  route==" + route);
        if (YlsCallManager.getInstance().isInCall()) {
            YlsCallManager.getInstance().makeTransferCall(context, calleeName, number, trunkName, route, object);
        } else {
            finishAllCall(context);
        }
    }

    public void confirmTransfer() {
        if (!YlsCallManager.getInstance().isInCall()) {
            return;
        }
        YlsCallManager.getInstance().confirmTransfer(App.getInstance().getContext());
    }

    @SuppressLint("MissingPermission")
    public boolean isTelephonyCalling() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(App.getInstance().getContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                return false;
            } else {
                TelecomManager telecomManager = (TelecomManager) App.getInstance().getSystemService(Context.TELECOM_SERVICE);
                return telecomManager.isInCall();
            }
        } else {
            TelephonyManager telephonyManager =
                    (TelephonyManager) App.getInstance().getContext().getSystemService(Context.TELEPHONY_SERVICE);
            return telephonyManager != null
                    && TelephonyManager.CALL_STATE_OFFHOOK == telephonyManager.getCallState();
        }
    }

    /**
     * 切换音频路由的弹窗
     *
     * @param context
     */
    public void showSoundChannelSelector(Context context) {
        String blueTooth = context.getString(R.string.call_bluetooth);
        String[] earpieceNames = new String[]{context.getString(R.string.call_earpiece), context.getString(R.string.call_audio_speaker)};
        String[] headsetNames = new String[]{context.getString(R.string.call_handset), context.getString(R.string.call_audio_speaker)};
        List<String> earpieceList = new ArrayList<>();
        earpieceList.addAll(Arrays.asList(earpieceNames));
        earpieceList.add(blueTooth);
        List<String> headsetList = new ArrayList<>();
        headsetList.addAll(Arrays.asList(headsetNames));
        headsetList.add(blueTooth);
        int[] earpieceVal = new int[]{YlsConstant.AUDIO_EARPIECE, YlsConstant.AUDIO_SPEAKER, YlsConstant.AUDIO_BLUETOOTH};
        int[] headsetVal = new int[]{YlsConstant.AUDIO_WIRED_HEADSET, YlsConstant.AUDIO_SPEAKER, YlsConstant.AUDIO_BLUETOOTH};
        int[] earpieceIcons = new int[]{R.mipmap.icon_earpiece_small, R.mipmap.icon_speaker_small, R.mipmap.icon_bluetooth_small};
        int[] headsetIcons = new int[]{R.mipmap.icon_headset_small, R.mipmap.icon_speaker_small, R.mipmap.icon_bluetooth_small};
        boolean isHeadset = MediaUtil.getInstance().isWiredHeadset(context);
        basePopupView = new XPopup.Builder(context)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asCustom(isHeadset ? new AudioPopupView(context, headsetList, headsetIcons, headsetVal,
                        position -> {
                            int val = headsetVal[position];
                            SoundManager.getInstance().changeAudioRoute(val, context);
                            notifyAudioChange();
                            basePopupView = null;

                        }) : new AudioPopupView(context, earpieceList, earpieceIcons, earpieceVal,
                        position -> {
                            int val = earpieceVal[position];
                            SoundManager.getInstance().changeAudioRoute(val, context);
                            notifyAudioChange();
                            basePopupView = null;
                        }))
                .show();
    }

    /**
     * 音频路由弹窗消失
     */
    private void dismissPopupView() {
        if (basePopupView != null) {
            basePopupView.dismiss();
        }
    }

    /**
     * 通过eventBus通知UI变更
     */
    private void notifyAudioChange() {
        EventBus.getDefault().post(new AudioRouteEvent());
    }


    /**
     * 是否跳转到通话界面
     *
     * @param notifyType
     * @param linkedId
     * @return
     */
    public boolean isJumpToCallActivity(int notifyType, String linkedId) {
        return !Utils.isAppOnForeground(App.getInstance().getContext())
                && isPushCallAlive(linkedId);
    }

    /**
     * 判断推送来电是否仍然存在
     *
     * @param linkedId
     * @return
     */
    private boolean isPushCallAlive(String linkedId) {
        LinkedList<InCallVo> callList = YlsCallManager.getInstance().getCallList();
        if (CommonUtil.isListNotEmpty(callList)) {
            for (InCallVo inCallVo : callList) {
                if (inCallVo.getLinkedId().equals(linkedId)) {
                    return true;
                }
            }
        }
        return false;
    }

}