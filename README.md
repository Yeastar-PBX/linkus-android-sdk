# linkus-android-sdk-vivo

> Linkus Mobile 客户端将 Android 手机与 Yeastar P 系列 PBX 系统、Cloud PBX和S 系列 VoIP PBX集成在一起。它通过一致的办公室体验和强大的协作功能提高效率，离开办公室工作时绝不会错过任何一个电话，通过公司 PBX 拨打 VoIP 电话可降低移动语音费用。YLS-SDK基于Linkus近十年在VOIP领域的技术沉淀， 致力于帮助开发者轻松集成语音通话能力，满足语音通讯的需求。

## 1.前置条件

- 请先和星纵信息的商务达成合作协议
- 已经部署了Yeastar P系列的IPPBX
- 拥有自己的Android应用（原生开发）
- 准备如下开发环境：

  ```
  Android Studio Arctic Fox 及以上
  Java 11
  Gradle 7.0.2
  Android Gradle Plugin 7.0.2
  Android v5.0 及以上版本
  ```

## 2. 接入流程

### 2.1 导入SDK

#### 2.1.1 手动导入aar包

直接导入"linkus-sdk-vivo.aar"

#### 2.1.2 通过maven导入

#### 2.1.3 混淆配置

### 2.2 初始化

#### 2.2.1 一键初始化

> 一键初始化，默认的SDK信息保存地址是{应用沙盒内的file}/yls_sdk
>
> 注意：初始化只能执行一次，必须在主进程中执行

```java
YlsBaseManager.getInstance().initYlsSDK(this, null);
```

#### 2.2.2 初始化参数设置

> 除了使用默认参数，用户也能自己设置初始化参数，目前开放的有SDK信息保存地址、自动增益、降噪、回音消除等

```java
YlsInitConfig config = new YlsInitConfig.Builder(projectPath)//SDK信息保存地址
                    	.supportCallWaiting(true)//是否支持CallWaiting
                    	.agc(true)//开启自动增益
                    	.ec(true)//开启回音消除
                    	.nc(true).build();//开启主动降噪
YlsBaseManager.getInstance().initYlsSDK(this, config);
```

### 2.3 登录

#### 2.3.1 首次登录

```java
boolean result = YlsLoginManager.getInstance().login(this, userName, password, localeIp,
                    localePortI, remoteIp, remotePortI);
```

#### 2.3.2 缓存登录

```java
int networkType = NetWorkUtil.getNetWorkType(this);
YlsLoginManager.getInstance().cacheLogin(networkType);
```

### 2.4 通话相关操作

#### 2.4.1 呼叫

```java
YlsCallManager.getInstance().makeNewCall(number, netWorkAvailable);
```



#### 2.4.2 接听来电

```java
YlsCallManager.getInstance().answerCall(callId);
```



#### 2.4.3 拒接

```java
YlsCallManager.getInstance().answerBusy(context, callId);
```



#### 2.4.4 通话结束挂断

```java
YlsCallManager.getInstance().hangUpCall(context, callId);
```



#### 2.4.5 hold通话

```java
YlsCallManager.getInstance().holdCall(inCallVo);
```



#### 2.4.6 解hold通话

```java
YlsCallManager.getInstance().unHoldCall(getContext(), inCallVo);
```



#### 2.4.7 咨询转

```java
YlsCallManager.getInstance().makeTransferCall(context, calleeName, number, trunkName, route, object);
```



#### 2.4.8 咨询转确定转移

```java
YlsCallManager.getInstance().confirmTransfer(App.getInstance().getContext());
```



#### 2.4.9 盲转

```java
YlsCallManager.getInstance().blindTransferCall(context, callOutNumber);
```



#### 2.4.10 静音/取消静音

```java
if (inCallVo.isMute()) {
    YlsCallManager.getInstance().unMute(inCallVo);
} else {
    YlsCallManager.getInstance().mute(inCallVo);
}
```



#### 2.4.11 录音/dtmf

```java
YlsCallManager.getInstance().record(callId, number);
```



#### 2.4.12 显示通话质量

```java
CallQualityVo callQualityVo = YlsCallManager.getInstance().getCallQuality();
```



#### 2.4.13 通话状态回调

```java
    YlsCallManager.getInstance().setCallStateInterface(new CallStateInterface() {
        //通话回调
        @Override
        public void onCallStateChange(CallStateVo callStateVo) {
            EventBus.getDefault().post(new CallStateEvent(callStateVo));
        }

        //通话质量等级回调
        @Override
        public void onNetWorkLevelChange(int callId, int networkLevel) {
            EventBus.getDefault().postSticky(new NetWorkLevelEvent(callId, networkLevel));
        }

        //网络连接变化回调
        @Override
        public void onConnectChange() {
            EventBus.getDefault().postSticky(new ConnectionChangeEvent());
        }

        //录音状态回调
        @Override
        public void onRecordChange(boolean isRecording) {
            EventBus.getDefault().post(new RecordEvent(isRecording));
        }

    });
```



#### 2.4.15 通话音频回调

```java
    YlsCallManager.getInstance().setSoundPlayInterface(new SoundPlayInterface() {
        @Override
        public void playDtmf(int dtmf) {
            SoundManager.getInstance().startPlay(context, dtmf);
        }

        @Override
        public void playRing() {
            SoundManager.getInstance().startPlay(context, YlsConstant.SOUND_RING_TYPE);
        }

        @Override
        public void playCallWaiting() {
            SoundManager.getInstance().startPlay(context, YlsConstant.SOUND_CALL_WAITING_TYPE);
        }

        @Override
        public void playDisconnect() {
            SoundManager.getInstance().startPlay(context, YlsConstant.SOUND_DISCONNECT_TYPE);
        }

        @Override
        public void stopPlay() {
            SoundManager.getInstance().stopPlay();
        }

        @Override
        public void openRemote() {
            RemoteControlUtil.getInstance().open(context);
            MediaUtil.getInstance().registerAudioChangeReceiver();
        }

        @Override
        public void setAudioRouter() {
            MediaUtil.getInstance().setCurrentAudioMode(AudioManager.MODE_IN_COMMUNICATION);
            if (isBluetoothAudio()) {
                MediaUtil.getInstance().routeToBluetooth(context);
            } else if (isSpeakerOn()) {
                MediaUtil.getInstance().routeToSpeaker(context);
            } else {
                MediaUtil.getInstance().routeToWiredOrEarpiece(context);
            }
        }
    });
```



#### 2.4.16 通话UI回调

```java
    YlsCallManager.getInstance().setActionInterface(new ActionInterface() {
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
    });
```



#### 2.4.17 CallKit回调

```java
    YlsCallManager.getInstance().setCallKitInterface(new CallKitInterface() {
        @Override
        public void placeCall(String callee) {

        }

        @Override
        public void inComingCall(String caller) {

        }

        @Override
        public void setDisConnectedCauseAll() {

        }

        @Override
        public void setDisConnectedCause(String number, int cause) {

        }

        @Override
        public void setActive(String number) {

        }

        @Override
        public void setHold(String number) {

        }
    });
```



### 2.5 推送

#### 2.5.1 推送设置

```java
YlsBaseManager.getInstance().setPushInfo("GETUI",clientid);
```



#### 2.5.2 推送处理

```java
    String data = new String(payload);
    JSONObject jsonObject = null;
    try {
        jsonObject = new JSONObject(data);
    } catch (JSONException e) {
        e.printStackTrace();
    }
    YlsCallManager.getInstance().handlerPushMessage(context, jsonObject);
```



### 2.6 CDR

### 2.7 UI界面
