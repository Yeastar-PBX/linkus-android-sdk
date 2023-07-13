package com.yeastar.linkus.demo;

import static com.yeastar.linkus.demo.utils.ToastUtil.showToast;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.motion.widget.MotionLayout;

import com.yeastar.linkus.demo.call.CallContainerActivity;
import com.yeastar.linkus.demo.call.CallManager;
import com.yeastar.linkus.demo.conference.ConferenceListActivity;
import com.yeastar.linkus.demo.eventbus.CallLogChangeEvent;
import com.yeastar.linkus.demo.eventbus.ClearNumberEvent;
import com.yeastar.linkus.demo.eventbus.ConferenceExceptionEvent;
import com.yeastar.linkus.demo.eventbus.ToggleDialPadEvent;
import com.yeastar.linkus.demo.utils.permission.PermissionRequest;
import com.yeastar.linkus.demo.widget.ActionSheetDialog;
import com.yeastar.linkus.demo.widget.ClickImageView;
import com.yeastar.linkus.demo.widget.CrossFadeImageView;
import com.yeastar.linkus.demo.widget.CustomProgressDialog;
import com.yeastar.linkus.demo.widget.Dialpad.DialPadLayout;
import com.yeastar.linkus.demo.widget.VerticalRecyclerView;
import com.yeastar.linkus.service.base.ConnectionUtils;
import com.yeastar.linkus.service.base.YlsBaseManager;
import com.yeastar.linkus.service.base.vo.ResultVo;
import com.yeastar.linkus.service.call.vo.CdrVo;
import com.yeastar.linkus.service.callback.RequestCallback;
import com.yeastar.linkus.service.cdr.YlsCallLogManager;
import com.yeastar.linkus.service.conference.YlsConferenceManager;
import com.yeastar.linkus.service.conference.vo.ConferenceVo;
import com.yeastar.linkus.service.log.LogUtil;
import com.yeastar.linkus.service.login.YlsLoginManager;
import com.yeastar.linkus.utils.BetterAsyncTask;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

public class DialPadActivity extends AppCompatActivity {

    private CrossFadeImageView callIv;
    private MotionLayout mMotionLayout;
    private ClickImageView mDialPadFold;
    private VerticalRecyclerView rv;
    private String number;
    private boolean dialPadShown = true;
    private DialPadLayout mDialPadLayout;
    private CdrAdapter adapter;
    private CustomProgressDialog progressDialog;
    private TextView tvTip;
    private static BetterAsyncTask<Void, Void, Integer> conferenceExceptionTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dial_pad);
        updateCdr();
        mDialPadLayout = findViewById(R.id.dial_pad_layout);
        mMotionLayout = findViewById(R.id.motionLayout);
        mDialPadFold = findViewById(R.id.dial_pad_fold);
        callIv = findViewById(R.id.tab_dial_call_iv);
        tvTip = findViewById(R.id.tv_tip);
        rv = findViewById(R.id.rv);
        adapter = new CdrAdapter();
        rv.setAdapter(adapter);
        setListener();
    }


    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void handlerClearNumber(ClearNumberEvent clearNumberEvent) {
        EventBus.getDefault().removeStickyEvent(clearNumberEvent);
        if (mDialPadLayout != null) {
            mDialPadLayout.setInputNumber("");
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handlerToggleDialPad(ToggleDialPadEvent toggleDialPadEvent) {
        toggleDialPad(false);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleCallLogChange(CallLogChangeEvent callLogChangeEvent) {
        int fetchCdrResult = callLogChangeEvent.getResult();
        LogUtil.i("CallLogChangeEvent result=%d", fetchCdrResult);
        initCdr();
        updateCdr();
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void handleConferenceException(ConferenceExceptionEvent conferenceExceptionEvent) {
        if (conferenceExceptionEvent.getConferenceVo() != null) {
            tvTip.setVisibility(View.VISIBLE);
        } else {
            tvTip.setVisibility(View.GONE);
            EventBus.getDefault().removeStickyEvent(conferenceExceptionEvent);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    public void setListener() {
        mDialPadLayout.setDialPadCallBack(str -> {
            number = str;
        });
        adapter.setOnItemClickListener((adapter, view, position) -> {
            List<CdrVo> list = (List<CdrVo>) adapter.getData();
            CdrVo cdrVo = list.get(position);
            boolean isCallOut = cdrVo.getStatus().equals(CdrVo.CALL_STATUS_CALLOUT);
            String number = isCallOut ? cdrVo.getCallee() : cdrVo.getCaller();
            CallManager.getInstance().call(DialPadActivity.this, number, number + "");
        });
        adapter.setOnItemLongClickListener((adapter, view, position) -> {
            ActionSheetDialog actionSheetDialog = new ActionSheetDialog(DialPadActivity.this)
                    .builder()
                    .setCancelable(true)
                    .setCanceledOnTouchOutside(true)
                    .addSheetItem(R.string.public_delete,
                            ActionSheetDialog.SheetItemColor.Black, which -> {
                                List<CdrVo> list = (List<CdrVo>) adapter.getData();
                                CdrVo cdrVo = list.get(position);
                                YlsCallLogManager.getInstance().deleteCdr(cdrVo.getId() + "");
                            });
            if (actionSheetDialog.getSheetItemCount() > 0) {
                actionSheetDialog.show();
            }
            return false;
        });
        callIv.setOnClickListener(v -> {
            if (callIv.getAltSrcAlpha() == 1) {
                toggleDialPad(true);
                return;
            }
            Editable text = mDialPadLayout.getTabDialInputTv().getText();
            if (text != null && TextUtils.isEmpty(text.toString().trim())) {
                Toast.makeText(getApplicationContext(), "号码不能为空!", Toast.LENGTH_LONG).show();
                return;
            }
            CallManager.getInstance().call(DialPadActivity.this, number, number + "");
        });
        mDialPadFold.setOnClickListener(v -> {
            toggleDialPad(false);
        });
        tvTip.setOnClickListener(v -> {
            requestConferencePermission(DialPadActivity.this);
        });
    }

    private void updateCdr() {
        int missCallCount = YlsCallLogManager.getInstance().getMissCallCdrCount();
        setTitle("未接来电数量:" + missCallCount);
    }

    private void toggleDialPad(boolean dialPadShown) {
        if (this.dialPadShown == dialPadShown) {
            return;
        }
        this.dialPadShown = dialPadShown;
        if (dialPadShown) {
            mMotionLayout.transitionToStart();
        } else {
            mMotionLayout.transitionToEnd();
        }
    }

    public String getNumber() {
        return number;
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        initCdr();
    }

    private void initCdr() {
        List<CdrVo> cdrVoList = YlsCallLogManager.getInstance().getCdrList(1000);
        adapter.setList(cdrVoList);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mDialPadLayout != null) {
            mDialPadLayout.mediaPlayRelease();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_logout:
                // 处理退出登录菜单项的点击事件
                showLargeProgressDialog();
                YlsLoginManager.getInstance().logout(this, new RequestCallback() {
                    @Override
                    public void onSuccess(Object result) {
                        finish();
                        startActivity(new Intent(DialPadActivity.this, LoginActivity.class));
                        closeProgressDialog();
                    }

                    @Override
                    public void onFailed(int code) {
                        closeProgressDialog();
                    }

                    @Override
                    public void onException(Throwable exception) {
                    }
                });

                return true;
            case R.id.menu_clear_cdr:
                // 处理清除所有cdr的点击事件
                YlsCallLogManager.getInstance().deleteAllCdr();
                return true;
            case R.id.menu_read_all:
                // 处理标记所有cdr已读的点击事件
                YlsCallLogManager.getInstance().readAllCdr();
                return true;
            case R.id.menu_agc_open:
                // 处理开启自动增益的点击事件
                YlsBaseManager.getInstance().agcSetting(true);
                return true;
            case R.id.menu_agc_close:
                // 处理关闭自动增益的点击事件
                YlsBaseManager.getInstance().agcSetting(false);
                return true;
            case R.id.menu_ec_open:
                // 处理开启回音消除的点击事件
                YlsBaseManager.getInstance().echoSetting(true);
                return true;
            case R.id.menu_ec_close:
                // 处理关闭回音消除的点击事件
                YlsBaseManager.getInstance().echoSetting(false);
                return true;
            case R.id.menu_nc_open:
                // 处理开启主动降噪的点击事件
                YlsBaseManager.getInstance().ncSetting(true);
                return true;
            case R.id.menu_nc_close:
                // 处理关闭主动降噪的点击事件
                YlsBaseManager.getInstance().ncSetting(false);
                return true;
            case R.id.menu_conference:
                ConferenceListActivity.start(DialPadActivity.this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showLargeProgressDialog() {
        showProgressDialog(R.string.setting_logging_out);
    }

    private void showProgressDialog(int tipRes) {
        if (progressDialog == null) {
            progressDialog = new CustomProgressDialog(this, CustomProgressDialog.TYPE_TEXT_MULTIPLE, tipRes, true);
        }
        if (!isDestroyed() && !isFinishing()) {
            progressDialog.show();
        }
    }

    private void closeProgressDialog() {
        if (isDestroyed() || isFinishing()) {
            return;
        }
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    /**
     * 返回异常会议室先请求权限
     */
    private void requestConferencePermission(Activity activity) {
        PermissionRequest request = new PermissionRequest(activity, new PermissionRequest.PermissionCallback() {
            @Override
            public void onSuccessful(List<String> permissions) {
                returnExceptionConference(activity);
            }

            @Override
            public void onFailure(List<String> permissions) {
            }

        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            request.hasPermission(android.Manifest.permission.READ_PHONE_STATE, android.Manifest.permission.RECORD_AUDIO, android.Manifest.permission.BLUETOOTH_CONNECT);
        } else {
            request.hasPermission(android.Manifest.permission.READ_PHONE_STATE, Manifest.permission.RECORD_AUDIO);
        }
    }

    private void returnExceptionConference(Activity activity) {
        //判断网络是否可用，服务器是否连上
        if (YlsLoginManager.getInstance().isConnected() && ConnectionUtils.isRegister()) {
            showProgressDialog(R.string.conference_tip_get_information);
            ConferenceExceptionEvent conferenceExceptionEvent = EventBus.getDefault().getStickyEvent(ConferenceExceptionEvent.class);
            if (conferenceExceptionEvent != null && conferenceExceptionEvent.getConferenceVo() != null) {
                ConferenceVo conferenceVo = conferenceExceptionEvent.getConferenceVo();
                YlsConferenceManager.getInstance().setConferenceVo(conferenceVo);
                conferenceExceptionTask = new BetterAsyncTask<Void, Void, Integer>() {
                    @Override
                    public Integer doInBackground(Void... params) {
                        if (!TextUtils.isEmpty(YlsLoginManager.getInstance().getMyExtension())) {
                            ResultVo resultVo = YlsConferenceManager.getInstance().returnConferenceBlock(conferenceVo.getConferenceId(), YlsLoginManager.getInstance().getMyExtension());
                            return resultVo.getCode();
                        } else {
                            return 1;
                        }
                    }

                    @Override
                    public void onPostExecute(Integer returnFlag) {
                        //0.成功 1.失败 2.超时 3.服务器异常 4.会议结束
                        if (returnFlag == 0) {
                            returnException(activity, conferenceVo);

                        } else if (returnFlag == 4) {
                            showToast(R.string.conference_tip_end);
                            YlsConferenceManager.getInstance().setConferenceVo(null);
                        } else {
                            YlsConferenceManager.getInstance().setConferenceVo(null);
                            showToast(R.string.nonetworktip_error);
                        }
                        EventBus.getDefault().removeStickyEvent(ConferenceExceptionEvent.class);
                        tvTip.setVisibility(View.GONE);
                        closeProgressDialog();
                    }
                };
                conferenceExceptionTask.executeParallel();
            } else {
                closeProgressDialog();
            }
        }
    }

    private void returnException(Context context, ConferenceVo conferenceVo) {
        LogUtil.w("返回异常会议室");
        Intent intent = new Intent(context, CallContainerActivity.class);
        intent.putExtra(Constant.EXTRA_CONFERENCE, conferenceVo);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        //已经有microphone前台服务不用重新启动
        if (CallManager.getInstance().getMicroPhoneServiceIntent() == null) {
            CallManager.getInstance().makeMicroPhoneNotification(context);
        }
    }

}