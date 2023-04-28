package com.yeastar.linkus.demo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.motion.widget.MotionLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.chad.library.adapter.base.listener.OnItemLongClickListener;
import com.yeastar.linkus.demo.call.CallManager;
import com.yeastar.linkus.demo.eventbus.CallLogChangeEvent;
import com.yeastar.linkus.demo.eventbus.ClearNumberEvent;
import com.yeastar.linkus.demo.eventbus.ToggleDialPadEvent;
import com.yeastar.linkus.demo.widget.ActionSheetDialog;
import com.yeastar.linkus.demo.widget.ClickImageView;
import com.yeastar.linkus.demo.widget.CrossFadeImageView;
import com.yeastar.linkus.demo.widget.CustomProgressDialog;
import com.yeastar.linkus.demo.widget.Dialpad.DialPadLayout;
import com.yeastar.linkus.demo.widget.VerticalRecyclerView;
import com.yeastar.linkus.service.base.YlsBaseManager;
import com.yeastar.linkus.service.call.YlsCallManager;
import com.yeastar.linkus.service.call.vo.CdrVo;
import com.yeastar.linkus.service.callback.RequestCallback;
import com.yeastar.linkus.service.cdr.YlsCallLogManager;
import com.yeastar.linkus.service.log.LogUtil;
import com.yeastar.linkus.service.login.YlsLoginManager;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dial_pad);
        updateCdr();
        mDialPadLayout = findViewById(R.id.dial_pad_layout);
        mMotionLayout = findViewById(R.id.motionLayout);
        mDialPadFold = findViewById(R.id.dial_pad_fold);
        callIv = findViewById(R.id.tab_dial_call_iv);
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
        List<CdrVo> cdrVoList = YlsCallLogManager.getInstance().getCdrList();
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showLargeProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new CustomProgressDialog(this, CustomProgressDialog.TYPE_TEXT_MULTIPLE, R.string.setting_logging_out, true);
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
}