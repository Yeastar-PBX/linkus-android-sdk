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
>aar包中已包含混淆文件，无需特殊配置

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
YlsInitConfig config = new YlsInitConfig.Builder(projectPath)//SDK信息保存地址（包括SDK日志信息的地址）
                    	.supportCallWaiting(true)//是否支持CallWaiting
                    	.agc(true)//开启自动增益
                    	.ec(true)//开启回音消除
                    	.nc(true).build();//开启主动降噪
YlsBaseManager.getInstance().initYlsSDK(this, config);
```

#### 2.2.3 自动增益开关

```java
/**
 * agc音频自动增益
 *
 * @return
 */
public void agcSetting(boolean isOpen)
```

#### 2.2.4 回音消除开关

```java
/**
 * 回音消除
 *
 * @return
 */
public void echoSetting(boolean isOpen)
```

#### 2.2.5 主动降噪开关

```java
/**
 * 主动降噪
 *
 * @return
 */
public void ncSetting(boolean isOpen)
```



### 2.3 登录

#### 2.3.1 首次登录

> 注意，localeIp,localePortI和remoteIp, remotePortI必须有一组是有值的

```java
/**
 * 手动登录
 *
 * @param context
 * @param userName
 * @param passWord
 * @param localeIp
 * @param localePort
 * @param remoteIp
 * @param remotePort
 * @param requestCallback 登录结果回调
 * @return
 */
public void loginBlock(Context context, String userName, String passWord, String localeIp, int localePort,
                       String remoteIp, int remotePort, RequestCallback<Boolean> requestCallback)
//手动登录示例
YlsLoginManager.getInstance().loginBlock(this, userName, password, localeIp,
        localePortI, remoteIp, remotePortI, new RequestCallback<>() {
            @Override
            public void onSuccess(Boolean result) {
                closeProgressDialog();
                startActivity(new Intent(LoginActivity.this, DialPadActivity.class));
            }

            @Override
            public void onFailed(int code) {
                closeProgressDialog();
                Toast.makeText(LoginActivity.this, R.string.login_tip_login_failed, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onException(Throwable exception) {
                closeProgressDialog();
                Toast.makeText(LoginActivity.this, R.string.login_tip_login_failed, Toast.LENGTH_LONG).show();
            }
        });
```

#### 2.3.2 缓存登录

```java
int networkType = NetWorkUtil.getNetWorkType(this);
YlsLoginManager.getInstance().cacheLogin(networkType);
```

#### 2.3.3 登录状态

```java
/**
 * 判断是否登录上
 * @return
 */
public boolean isLoginEd()
//调用示例
boolean isLoginEd = YlsLoginManager.getInstance().isLoginEd();
```



#### 2.3.4 连接状态

```java
/**
 * 与服务器的连接状态
 * @return
 */
public synchronized boolean isConnected()
//调用示例
YlsLoginManager.getInstance().isConnected();
```



#### 2.3.4 sdk通知回调

```java
YlsBaseManager.getInstance().setSdkCallback(new SdkCallback() {
    //cdr变更通知
    @Override
    public void onCdrChange(int syncResult) {
        EventBus.getDefault().post(new CallLogChangeEvent(syncResult));
    }
    //退出登录通知
    @Override
    public void onLogout(int type) {
        context.startActivity(new Intent(context, LoginActivity.class));
    }
});
```


> 其中退出类型如下

```
SdkEventCode.EVENT_USER_RELOGIN://1005 帐号在其他地方登录
               
SdkEventCode.P_EVENT_DISABLE_LINKUS_APP://20014 linkus被禁用了
   
SdkEventCode.EVENT_LOGIN_LOCKED://1009 账号被锁住

SdkEventCode.EVENT_LOGIN_INFO_ILLEGAL://1010 缓存登录信息错误
    
SdkEventCode.EVENT_CACHE_LOGIIN_USER_NOTFOUND://1011 缓存登录信息为空
    
SdkEventCode.P_EVENT_LOGIN_MODE_CHANGE://20008 登录模式变更
   
SdkEventCode.P_EVENT_COUNTRY_IP_LIMIT://20083 ip登录限制
   
SdkEventCode.P_EVENT_LICENSE_EXPIRE://20093 pbx未激活
   
```

### 2.4 通话相关操作

> 通话相关大部分在YlsCallManager

#### 2.4.1 呼叫

```java
/**
 * 拨打电话
 *
 * @param callNumber
 * @param netWorkAvailable 网络是否可用
 * @return
 */
public void makeNewCall(String callNumber, boolean netWorkAvailable)
//调用方式如下
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
YlsCallManager.getInstance().setCallStateCallback(new CallStateCallback() {
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



#### 2.4.15 通话UI回调

```java
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
```



### 2.5 推送

#### 2.5.1 推送设置

```java
/**
 * 设置推送信息
 *
 * @param mode
 * @param token
 * @param requestCallback
 * @return
 */
public void setPushInfo(String mode, String token, RequestCallback requestCallback)
//使用方法示例
YlsBaseManager.getInstance().setPushInfo("GETUI", clientid, new RequestCallback() {
    @Override
    public void onSuccess(Object result) {

    }

    @Override
    public void onFailed(int code) {

    }

    @Override
    public void onException(Throwable exception) {

    }
});
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



### 2.6 通话记录

#### 2.6.1 获取通话记录

> 获取最多100条通话记录

```java
/**
 * 获取cdr数据
 *
 * @return
 */
public List<CdrVo> getCdrList();
//调用示例
List<CdrVo> cdrVoList = YlsCallLogManager.getInstance().getCdrList();
```

#### 2.6.2 删除通话记录

```java
/**
 * 删除cdr
 *
 * @param cdrIds 以","分隔的CdrVo的id字符串
 * @return
 */
public int deleteCdr(String cdrIds)
```

#### 2.6.3 删除所有通话记录

```java
/**
 * 删除所有cdr
 *
 * @return
 */
public int deleteAllCdr()
//调用示例
btnCdrClear.setOnClickListener(v -> YlsCallLogManager.getInstance().deleteAllCdr());
```

#### 2.6.4 标记所有未读为已读

```java
/**
 * 标记已读
 *
 * @return
 */
public void readAllCdr()
```

#### 2.6.5 未接来电数量

```java
/**
 * 未接来电cdr数量
 *
 * @return
 */
public int getMissCallCdrCount();
```



### 2.7 其他事项

> 其他未尽事项可以结合demo来看
