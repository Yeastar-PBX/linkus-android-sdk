# linkus-android-sdk

> Linkus Mobile 客户端将 Android 手机与 Yeastar P 系列 PBX 系统、Cloud PBX和S 系列 VoIP PBX集成在一起。它通过一致的办公室体验和强大的协作功能提高效率，离开办公室工作时绝不会错过任何一个电话，通过公司 PBX 拨打 VoIP 电话可降低移动语音费用。YLS-SDK基于Linkus近十年在VOIP领域的技术沉淀， 致力于帮助开发者轻松集成语音通话能力，满足语音通讯的需求。

## 1.前置条件

- 请先和星纵信息的商务达成合作协议
- 已经部署了Yeastar P系列的IPPBX
- 拥有自己的Android应用（原生开发）
- 准备如下开发环境：

  ```
  Android Studio Arctic Fox 及以上
  Java 11
  Gradle 6.5
  Android Gradle Plugin 4.1.1
  Android v5.0 及以上版本
  ```

## 2. 接入流程

### 2.1 导入SDK

#### 2.1.1 手动导入aar包

直接导入"linkus-sdk.aar"

#### 2.1.2 通过maven导入

#### 2.1.3 混淆配置
>aar包中已包含混淆文件，无需特殊配置

### 2.2 初始化

```java
    /**
     * SDK初始化
     *
     * @param context 上下文
     * @param config 配置信息
     */
    public ResultVo initYlsSDK(Context context, YlsInitConfig config)
```

#### 2.2.1 一键初始化

> 一键初始化，默认的SDK信息保存地址是{应用沙盒内的file}/yls_sdk
>
> 注意：**初始化只能执行一次，必须在主进程中执行**

```java
YlsBaseManager.getInstance().initYlsSDK(this, null);
```

#### 2.2.2 初始化参数设置

> 除了使用默认参数，用户也能自己设置初始化参数，目前开放的有SDK信息保存地址、自动增益、降噪、回音消除、是否pad等

```java
YlsInitConfig config = new YlsInitConfig.Builder(projectPath)//SDK信息保存地址（包括SDK日志信息的地址）
        .supportCallWaiting(false)//是否支持CallWaiting，默认不支持
        .agc(true)//开启自动增益,默认开启
        .ec(true)//开启回音消除，默认开启
        .nc(true)//开启主动降噪，默认开启
    	.setPad(true)//是否pad，不设置的时候由sdk自行判断，如果sdk的判断有误，可自行设置
        .key("")//数据库密码
        .build();
YlsBaseManager.getInstance().initYlsSDK(this, config);
```

#### 2.2.3 自动增益开关

```java
/**
 * agc音频自动增益
 *
 * @return
 */
public void agcSetting(Context context, boolean isOpen)

```

> 使用方法

```java
YlsBaseManager.getInstance().agcSetting(getContext(), true);
```

#### 2.2.4 回音消除开关

```java
/**
 * 回音消除
 *
 * @return
 */
public void echoSetting(Context context, boolean isOpen)
```

> 使用方法

```java
YlsBaseManager.getInstance().echoSetting(getContext(), true);
```

#### 2.2.5 主动降噪开关

```java
/**
 * 主动降噪
 *
 * @return
 */
public void ncSetting(Context context, boolean isOpen)
```

> 使用方法

```java
YlsBaseManager.getInstance().ncSetting(getContext(), true);
```

#### 2.2.6 设置SIP编码

```java
/**
* 设置编解码
* 注意：设置编解码后，新的编码格式只对新的通话生效，对已经建立的通话不生效
*/
public void setCodec(Context context, String codec)
```

> 使用方法

```java
YlsCallManager.getInstance().setCodec(getContext(), (String) newValue);
```

#### 2.2.7 设置是否呼叫等待

```java
/**
* 设置是否支持callWaiting
* @param supportCallWaiting
*/
public void setSupportCallWaiting(boolean supportCallWaiting)
```

> 使用方法

```java
YlsCallManager.getInstance().setSupportCallWaiting(false);
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
```

>linkus sdk mobile端登录错误码：
>1： 连接不上服务器
>-5： 连接服务器成功但是登录请求没有响应
>403：用户名或密码错误
>405：客户端被禁用
>407：账号被锁定
>416：请求ip被禁止（pbx开启国家防御）

> 使用方法

```java
YlsLoginManager.getInstance().loginBlock(this, userName, password, localeIp,localePortI, remoteIp, remotePortI, new RequestCallback() {
	@Override
	public void onSuccess(Object result) {
		closeProgressDialog();
		startActivity(new Intent(LoginActivity.this, DialPadActivity.class));
	}

	@Override
	public void onFailed(int code) {
		closeProgressDialog();
		String failStr = getString(R.string.login_tip_login_failed,code);
		Toast.makeText(LoginActivity.this, failStr, Toast.LENGTH_LONG).show();
	}

	@Override
	public void onException(Throwable exception) {
		closeProgressDialog();
		Toast.makeText(LoginActivity.this, R.string.login_tip_login_exception, Toast.LENGTH_LONG).show();
	}
});
```

#### 2.3.2 缓存登录

```java
public void cacheLogin(int networkType) 
```

> 使用方法

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
```

> 使用方法

```java
boolean isLoginEd = YlsLoginManager.getInstance().isLoginEd();
```

#### 2.3.4 连接状态

```java
/**
 * 与服务器的连接状态
 * @return
 */
public synchronized boolean isConnected() 
```

> 使用方法

```java
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
//重连成功通知
@Override
public void onReconnectSuccess() {}
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

SdkEventCode.P_EVENT_SDK_STATUS_CHANGE://20153 sdk状态变更

SdkEventCode.P_EVENT_SDK_ACCESSKEY_CHANGE://20154 sdk accesskey变更
   
```

### 2.4 通话相关操作

> **通话相关大部分在YlsCallManager**

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
```

> 使用方法

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
/**
 * 通话界面进行mute unMute操作
 */
public void mute(InCallVo inCallVo)
```



#### 2.4.11 录音

```java
/**
 * 录音
 *
 * @param vo
 * @return
 */
public int record(InCallVo vo)
```



#### 2.4.12 发送DTMF

```java
/**
 * dtmf
 *
 * @param callId
 * @return
 */
public int sendDtmf(int callId, String recordCode)
```



#### 2.4.13 显示通话质量

```java
CallQualityVo callQualityVo = YlsCallManager.getInstance().getCallQuality();
```



#### 2.4.14 通话状态回调

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
    	//结束通话回调
        finishAllCall(context);
        }

@Override
public void onNewCall() {
    	//来电弹窗回调
        jump2CallActivity(context);
        }

@Override
public void onCallWaiting() {
    	//call waiting回调
        EventBus.getDefault().post(new CallWaitingEvent());
        SoundManager.getInstance().startPlay(context, YlsConstant.SOUND_CALL_WAITING_TYPE);
        }

@Override
public void onMissCallClick() {
		//收到misscall的回调
        }

@Override
public void onStopMicroPhoneService() {
		//Android11以上通话在后台时需要开启前台服务，通话结束后停止前台服务的回调
        }

@Override
public void onDismissPopupView() {
    	//音频路由弹窗消失的回调
        dismissPopupView();
        }

@Override
public void onNotifyAudioChange() {
    	//音频路由变更的回调
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
 * @param mode: getui, huawei, xiaomi
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

> 获取最多N条通话记录
>
> **注意：cdr相关接口都在YlsCallLogManager**

```java
/**
 * 获取cdr数据
 *
 * @return
 */
public List<CdrVo> getCdrList(int limit);
//调用示例
List<CdrVo> cdrVoList = YlsCallLogManager.getInstance().getCdrList(1000);
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

### 2.7 多方通话（至多五方）

> **注意：多方通话接口都在YlsCallManager里**

#### 2.7.1 添加多方通话

```java
/**
 * 多方通话呼出
 */
public void makeMultipartyCall(String number, String trunkName, String route, Activity activity, Object obj)
```



#### 2.7.2 挂断多方通话中的单通

```java
/**
 * 多方通话中挂断单通通话
 *
 * @return
 */
public void hangUpSingleCall(Context context, int callId)
```



#### 2.7.3 静音/取消静音多方通话中的单通

```java
/**
 * 多方通话中对单个成员进行mute unMute操作
 */
public void muteSingleMember(InCallVo inCallVo)
```



#### 2.7.4 多方通话其他接口

```java
/**
 * 多方通话中所有通话的callID数组
 */
public int[] getCallIdArrays()

/**
 * 多方通话中所有mute通话的callID数组
 */
public int[] getMuteArrays()

/**
 * 多方通话中所有hold通话的callID数组
 */
public int[] getHoldArrays()

/**
 * 是否在多方通话中
 *
 * @return
 */
public boolean isInMultipartyCall()

/**
 * 设置是否在多方通话中
 *
 * @param inMultipartyCall
 */
public void setInMultipartyCall(boolean inMultipartyCall)

/**
 * 多方通话是否hold所有人
 *
 * @return
 */
public boolean isInMultipartyHold()

/**
 * 获取多方通话hold开始时间
 *
 * @return
 */
public long getMultipartyHoldStartTime()

/**
 * 多方通话是否全体静音
 *
 * @return
 */
public boolean isMultipartyMute()

/**
 * 设置多方通话是否全体静音
 *
 * @param multipartyMute
 */
public void setMultipartyMute(boolean multipartyMute)

/**
 * 获取多方通话开始时间
 *
 * @return
 */
public long getMultipartyCallStartTime()

/**
 * 是否达到多方通话最大值(4通)
 *
 * @return
 */
public boolean reachMultiPartyCallsLimit()

/**
 * 判断多方通话是否在录音
 *
 * @return
 */
public boolean isMultipartyCallRecord(LinkedList<InCallVo> list)

/**
 * 判断多方通话的录音是否可用
 *
 * @return
 */
public boolean isMultiPartyCallRecordAble()

/**
 * 判断多方通话的录音是否禁用
 *
 * @return
 */
public boolean isMultiPartyCallAlwaysRecordDisable()
```



### 2.8 会议室

#### 2.8.1会议室功能初始化

> **会议室初始化最好在Application主进程进行**
>
> **会议室相关方法都在YlsConferenceManager里**

```java
YlsConferenceManager.getInstance().setConferenceCallback(context, new ConferenceCallback() {
@Override
public void onConferenceException(ConferenceVo conferenceVo) {
        //异常会议室回调
        EventBus.getDefault().postSticky(new ConferenceExceptionEvent(conferenceVo));
        }

@Override
public void onConferenceStatusChange(String conferenceId, String number, int status) {
        //会议室成员状态回调
        EventBus.getDefault().post(new ConferenceStatusEvent(conferenceId, number, status));
        }
        });
```

#### 2.8.2 获取会议室记录列表

```java
conferenceModelList = YlsConferenceManager.getInstance().getConferenceList();
```

#### 2.8.3 开始会议室

> **会议室名称限制：**
>
> **1.不能使用包含 :、!、$、(、)、/、#、;、,、[、]、"、=、<、>、&、\、'、```、^、%、@、{、}、|、空格**
>
> **2.长度不能超过63g**

```java
    /**
 * 发起会议室
 *
 * @param context
 * @param conferenceName
 * @param memberArray
 * @param requestCallback
 */
public void startConference(Context context, String conferenceName, String[] memberArray, RequestCallback requestCallback)
```

> 使用方法

```java
YlsConferenceManager.getInstance().startConference(activity, conferenceVo.getName(), numberArray, new RequestCallback() {
            @Override
            public void onSuccess(Object result) {
                view.dismissProgressDialog();
                activity.finish();
                Intent intent = new Intent(activity, CallContainerActivity.class);
                intent.putExtra(Constant.EXTRA_CONFERENCE, conferenceVo);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivity(intent);
            }

            @Override
            public void onFailed(int code) {
                view.dismissProgressDialog();
                YlsConferenceManager.getInstance().setConferenceVo(null);
                switch (code) {
                    case YlsConstant.CONFERENCE_NAME_LENGTH_ERROR:
                        ToastUtil.showToast("会议室名称长度不能超过63个字节");
                        break;
                    case YlsConstant.CONFERENCE_NAME_REGEX_ERROR:
                        ToastUtil.showLongToast("会议室名称不能使用包含 :、!、$、(、)、/、#、;、,、[、]、\"、=、<、>、&、\\、'、```、^、%、@、{、}、|、空格");
                        break;
                    case YlsConstant.CONFERENCE_IN_USE_ERROR:
                        ToastUtil.showToast(R.string.conference_tip_meeting);
                        break;
                    case YlsConstant.SDK_NETWORK_DISABLE:
                    case YlsConstant.SDK_LOGIN_DISABLE:
                        ToastUtil.showToast(R.string.connectiontip_connect_fail);
                        break;

                    default:
                        break;
                }
            }

            @Override
            public void onException(Throwable exception) {
                view.dismissProgressDialog();
            }
        });
```



#### 2.8.4 返回异常会议室

```java
/**
 * 返回异常会议室
 *
 * @param conferenceId
 * @param member
 * @return
 */
public ResultVo returnConferenceBlock(String conferenceId, String member)
```

#### 2.8.5 会议进行中的接口

```java
/**
 * 会议室中
 * 会议室主持人
 * 静音/取消静音 所有成员
 *
 * @param conferenceId
 * @param member
 * @param isMute
 */
public ResultVo muteAllConferenceMemberBlock(String conferenceId, String member, boolean isMute)

/**
 * 会议室中
 * 会议室主持人
 * 静音/取消静音 单个会议室成员
 *
 * @param conferenceId
 * @param number
 * @param isMute
 */
public ResultVo muteConferenceMemberBlock(String conferenceId, String number, boolean isMute)

/**
 * 会议室中
 * 会议室主持人
 * 删除成员
 *
 * @param conferenceId
 * @param number
 */
public ResultVo kickConferenceMemberBlock(String conferenceId, String number)

/**
 * 会议室中
 * 邀请成员参加会议室
 *
 * @param conferenceId
 * @param number
 */
public ResultVo inviteConferenceMemberBlock(String conferenceId, String number)

/**
 * 会议室中
 * 重新邀请会议室成员(之前邀请了,但未进入的成员)
 *
 * @param conferenceId
 * @param number
 */
public ResultVo reInviteConferenceMemberBlock(String conferenceId, String number)

/**
 * 结束会议室
 *
 * @param context
 * @param callId
 * @param conferenceVo
 * @param callback
 * @return
 */
public void endConferenceBlock(Context context, int callId, ConferenceVo conferenceVo, RequestCallback callback)

/**
 * 返回异常会议室
 *
 * @param context
 * @param conferenceId
 * @param member
 * @return
 */
public ResultVo returnConferenceBlock(Context context, String conferenceId, String member)

```

#### 2.8.6 会议室其他接口

```java
    /**
 * 获取当前会议室缓存
 * @return
 */
public ConferenceVo getConferenceVo()

/**
 * 设置当前会议室缓存
 * @param conferenceVo
 */
public void setConferenceVo(ConferenceVo conferenceVo)

/**
 * 获取会议室倒计时的时间
 * 只有倒计时为负数时可以发起新的会议室
 *
 * @return
 */
public long getCountDownTime()

/**
 * 设置会议室结束时间
 *
 * @param endConferenceTime
 */
public void setEndConferenceTime(long endConferenceTime)

/**
 * 删除会议室记录
 *
 * @param conferenceId
 */
public void deleteConferenceLog(String conferenceId)

/**
 * 在小于9个成员前需要添加 [新增成员选项]
 *
 * @param memberList
 */
public void addNullMember(List<ConferenceMemberVo> memberList)

/**
 * 会议室主持人
 * 在小于9个成员的时候添加 [新增成员选项]
 * 在大于1个成员的时候添加 [删除成员选项]
 *
 * @param memberList 成员list
 */
public void addNullMemberByAdmin(List<ConferenceMemberVo> memberList)

/**
 * 删除 [新增成员选项]
 *
 * @param memberList
 */
public void removeNullMember(List<ConferenceMemberVo> memberList)

/**
 * 删除所有的会议室记录
 */
public void deleteAllConferenceLog()        
```

### 2.9 权限相关
> **通话相关的权限已经包括在aar的AndroidManifest.xml里了，我们遵循按需获取权限的原则申请危险权限**
```java
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
```

### 2.10 其他事项

> 其他未尽事项可以结合demo来看

## 3. 更新日志

- 2024/02/05 增加对Pad的支持，开放编码、agc、ec、nc的单独设置接口
- 2023/09/22 新增通话UI回调说明
- 2023/09/19 新增权限申请说明
- 2023/08/11 新增退出的回调码【SdkEventCode.P_EVENT_SDK_STATUS_CHANGE://20153 sdk状态变更关闭sdk 或 sdk到期通知; SdkEventCode.P_EVENT_SDK_ACCESSKEY_CHANGE ：刷新access_key】 新增onReconnectSuccess()回调接口
- 2023/07/24 更新登录错误码
- 2023/07/10 提交1.0.9版本
  1. 新增多方通话功能
  2. 新增会议室功能
  3. SDK初始化接口变更，新增数据库加密秘钥设置
- 2023/06/28 提交1.0.8版本
- 2023/06/26 默认关闭callwaiting，新增cdr获取接口配置项，提交1.0.7版本