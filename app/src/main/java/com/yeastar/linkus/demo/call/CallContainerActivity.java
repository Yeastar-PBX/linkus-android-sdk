package com.yeastar.linkus.demo.call;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.PowerManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyCallback;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.yeastar.linkus.demo.App;
import com.yeastar.linkus.demo.Constant;
import com.yeastar.linkus.demo.R;
import com.yeastar.linkus.demo.base.BaseActivity;
import com.yeastar.linkus.demo.call.ring.RingFragment;
import com.yeastar.linkus.demo.utils.StatusBarUtil;
import com.yeastar.linkus.demo.utils.Utils;
import com.yeastar.linkus.service.call.YlsCallManager;
import com.yeastar.linkus.service.call.vo.InCallVo;
import com.yeastar.linkus.service.log.LogUtil;
import com.yeastar.linkus.utils.CommonUtil;
import com.yeastar.linkus.utils.SoundManager;

import java.lang.ref.WeakReference;
import java.util.LinkedList;


/*
    整合通话界面使用fragment切换实现
 */
public class CallContainerActivity extends BaseActivity {
    private SensorManager sensorManager = null;
    private SensorEventListener sensorEventListener = null;
    private PowerManager.WakeLock screenWakeLock = null;
    public AudioManager audioManager;
    private CallingContract.PhoneStateCallback phoneStateCallback;
    private int count = 0;

    public void setPhoneStateCallback(CallingContract.PhoneStateCallback callback) {
        this.phoneStateCallback = callback;
    }

    //监听来电状态的监听事件
    private void handlePhoneState(int state) {
        if (phoneStateCallback != null) {
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    count++;
                    //输出来电号码
                    phoneStateCallback.onRing();
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    count++;
                    phoneStateCallback.onCalling();
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    if (count == 1) {
                        phoneStateCallback.onAnswerBusy();
                    } else if (count == 2) {
                        phoneStateCallback.onHangup();
                    }
                    count = 0;
                    break;
                default:
                    break;
            }
        }
    }

    private void registerCallStateListener() {
        if (!callStateListenerRegistered) {
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                    telephonyManager.registerTelephonyCallback(getMainExecutor(), callStateListener);
                    callStateListenerRegistered = true;
                }
            } else {
                telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
                callStateListenerRegistered = true;
            }
        }
    }

    private void unRegisterCallStateListener() {
        if (callStateListenerRegistered) {
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                    telephonyManager.unregisterTelephonyCallback(callStateListener);
                    callStateListenerRegistered = false;
                }
            } else {
                telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
                callStateListenerRegistered = false;
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private static abstract class CallStateListener extends TelephonyCallback implements TelephonyCallback.CallStateListener {
        @Override
        abstract public void onCallStateChanged(int state);
    }

    private boolean callStateListenerRegistered = false;

    private CallStateListener callStateListener = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) ?
            new CallStateListener() {
                @Override
                public void onCallStateChanged(int state) {
                    // Handle call state change
                    handlePhoneState(state);
                }
            }
            : null;

    private PhoneStateListener phoneStateListener = (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) ?
            new PhoneStateListener() {
                @Override
                public void onCallStateChanged(int state, String phoneNumber) {
                    // Handle call state change
                    handlePhoneState(state);
                }
            }
            : null;

    public CallContainerActivity() {
        super(R.layout.activity_call_container);
    }

    @Override
    public void beforeSetView() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
    }

    @Override
    protected void setStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            int navigationBarColor = ContextCompat.getColor(this, android.R.color.transparent);
            if (Utils.isDarkMode(activity)) {
                getWindow().setNavigationBarColor(navigationBarColor);
            }
        } else {
            StatusBarUtil.setColor(activity, getResources().getColor(R.color.all_bg), 1);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void findView() {
//        App.getInstance().setInCall(true);
        initSensorManager();
        registerCallStateListener();
        audioManager = (AudioManager) App.getInstance().getContext().getSystemService(Context.AUDIO_SERVICE);
        initIntent();
        CallManager.getInstance().setCallActivity(new WeakReference<>(activity));
    }

    private void initIntent() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(Constant.EXTRA_DATA)) {
            InCallVo inCallVo = (InCallVo) intent.getSerializableExtra(Constant.EXTRA_DATA);
            if (!YlsCallManager.getInstance().isInCall()) {
                YlsCallManager.getInstance().getCallList().add(inCallVo);
            }
//            SearchCallModel searchCallModel = GlobalCache.findSearchCallModel(inCallVo.getCallNumber(), true);//主动呼出的也需要获取最新的Contacts信息
//            inCallVo.setCallName(searchCallModel.getCallName());
//            inCallVo.setCompany(searchCallModel.getCompany());
//            inCallVo.setObject(searchCallModel.getObject());
        }
        LinkedList<InCallVo> list = new LinkedList<>(YlsCallManager.getInstance().getCallList());
        if (CommonUtil.isListNotEmpty(list)) {
            InCallVo inCallVo = list.getFirst();
            if (inCallVo.isAccept() || inCallVo.isAnswer()) {//已接听
//                if (TextUtils.isEmpty(inCallVo.getConfId())) {//普通来电
                    InCallFragment inCall = new InCallFragment();
                    inCall.setContainerId(R.id.call_container);
                    switchContent(inCall, Constant.TAG_FRAGMENT_CALL);
//                } else if (!Constant.PUSH_CONFERENCE.equals(inCallVo.getConfId())) {//会议室来电
//                    ConferenceInCallFragment conferenceInCall = new ConferenceInCallFragment();
//                    conferenceInCall.setContainerId(R.id.call_container);
//                    switchContent(conferenceInCall, Constant.TAG_FRAGMENT_CONFERENCE);
//                } else {//会议室加载页面
//                    ConferenceLoadingFragment loadingFragment = new ConferenceLoadingFragment();
//                    loadingFragment.setContainerId(R.id.call_container);
//                    switchContent(loadingFragment);
//                }
            } else {
                if (inCallVo.isCallOut()) {
                    InCallFragment inCall = new InCallFragment();
                    inCall.setContainerId(R.id.call_container);
                    switchContent(inCall, Constant.TAG_FRAGMENT_CALL);
                } else {
                    RingFragment ring = new RingFragment();
                    ring.setContainerId(R.id.call_container);
                    switchContent(ring);
                }
            }
        } else {//异常情况进入通话页面需要结束掉
            finish();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (intent != null) {
            intent.putExtra(Constant.EXTRA_ON_NEW_INTENT, true);
            setIntent(intent);
        }
        super.onNewIntent(intent);
        initIntent();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initSensorManager() {
        if (sensorManager == null) {
            sensorManager = (SensorManager) App.getInstance().getContext().getSystemService(Context.SENSOR_SERVICE);
            PowerManager powerManager = (PowerManager) App.getInstance().getContext().getSystemService(Context.POWER_SERVICE);
            if (powerManager != null && powerManager.isWakeLockLevelSupported(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK)) {
                screenWakeLock = powerManager.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, TAG + "_bright");
            }
            Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
            sensorEventListener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    float[] values = event.values;
                    if (values != null && values.length > 0) {
                        if (values[0] < sensor.getMaximumRange()) {
                            if (screenWakeLock != null && !screenWakeLock.isHeld()) {
                                screenWakeLock.acquire();
                                LogUtil.w("距离感应器被挡住 屏幕暗掉");
                            }
                        } else {
                            if (screenWakeLock != null && screenWakeLock.isHeld()) {
                                screenWakeLock.release();
                                LogUtil.w("距离感应器无遮挡 屏幕亮起");
                            }
                        }
                    }
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {
                }
            };
            sensorManager.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

    }

    private void releaseWakeLock() {
        if (screenWakeLock != null) {
            if (screenWakeLock.isHeld()) {
                screenWakeLock.release();
                screenWakeLock = null;
            }
        }
    }

    @Override
    protected void onDestroy() {
//        App.getInstance().setInCall(false);
        CallManager.getInstance().setCallActivity(new WeakReference<>(null));
        releaseSensor();
        if (audioManager != null) {
            audioManager = null;
        }
        unRegisterCallStateListener();
        super.onDestroy();
    }

    private void releaseSensor() {
        releaseWakeLock();
        if (sensorManager != null) {
            sensorManager.unregisterListener(sensorEventListener);
        }
    }

    public void backPressed(boolean isBackPress) {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.call_container);
        if (fragment instanceof RingFragment) {
            backToHome(false, isBackPress);
        } else if (fragment instanceof InCallFragment) {
            backToHome(false, isBackPress);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        LogUtil.i("keyCode" + keyCode + "   KeyEvent==" + event);
        switch (keyCode) {
            case KeyEvent.KEYCODE_HOME:
                backPressed(false);
                return super.onKeyDown(keyCode, event);
            case KeyEvent.KEYCODE_BACK:
                backPressed(true);
                return super.onKeyDown(keyCode, event);
            case KeyEvent.KEYCODE_VOLUME_UP:
                LogUtil.w("音量调节+");
//                MediaUtil.getInstance().stopPlay();
                SoundManager.getInstance().stopPlay();
                getCurrentVoiceCallVolume();
                return super.onKeyDown(keyCode, event);
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                LogUtil.w("音量调节-");
//                MediaUtil.getInstance().stopPlay();
                SoundManager.getInstance().stopPlay();
                getCurrentVoiceCallVolume();
                return super.onKeyDown(keyCode, event);
            default:
                LogUtil.w("CallContainerActivity  onKeyDown" + keyCode);
                return super.onKeyDown(keyCode, event);
        }
    }

    private void getCurrentVoiceCallVolume() {
        int streamVolume = audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
        LogUtil.w("音量调节 当前通话音量=%d", streamVolume);
    }

    private void backToHome(boolean isConference, boolean isBack) {
        if (isBack) {
            moveTaskToBack(true);
        }
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        backPressed(false);
    }
}
