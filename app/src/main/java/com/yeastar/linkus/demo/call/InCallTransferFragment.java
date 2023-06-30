package com.yeastar.linkus.demo.call;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;

import com.yeastar.linkus.demo.Constant;
import com.yeastar.linkus.demo.R;
import com.yeastar.linkus.demo.eventbus.AgentEvent;
import com.yeastar.linkus.demo.widget.Dialpad.DialPadLayout;

import org.greenrobot.eventbus.EventBus;


/**
 * Created by lwh on 18-4-28.
 */

@SuppressWarnings("Convert2Lambda")
public class InCallTransferFragment extends InCallRelatedFragment {

    // 0:咨询转  1:盲转
    private int transferType = 0;
    private LinearLayout mBackLayout;
    private LinearLayout historyContainer = null;
    private LinearLayout contactContainer = null;
    private LinearLayout callContainer = null;
    private String number = "";
    private DialPadLayout dialpadLayout;

    public InCallTransferFragment() {
        super(R.layout.fragment_incall_transfer);
    }

    @Override
    public void findView(View parent) {
        super.findView(parent);
        mBackLayout = parent.findViewById(R.id.back_layout);
        dialpadLayout = parent.findViewById(R.id.dial_pad_layout);
        historyContainer = parent.findViewById(R.id.incall_history_container);
        contactContainer = parent.findViewById(R.id.incall_contact_container);
        callContainer = parent.findViewById(R.id.incall_call);
        Bundle bundle = getArguments();
        if (bundle != null) {
            transferType = bundle.getInt(Constant.EXTRA_DATA, 0);
        }
        setDialIvEnable(false);
        setListener();
    }

    public void setListener() {

        mBackLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.getSupportFragmentManager().popBackStack();
            }
        });

        dialpadLayout.setDialPadCallBack(new DialPadLayout.DialPadCallBack() {
            @Override
            public void onDialNumber(String number) {
                if (TextUtils.isEmpty(number)) {
                    setDialIvEnable(false);
                } else {
                    setDialIvEnable(true);
                }
            }
        });

        historyContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        contactContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        callContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                number = dialpadLayout.getInputNumber();
                CallManager.getInstance().transfer(activity, number,
                        "", "", transferType == 0);
            }
        });

        callContainer.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                number = dialpadLayout.getInputNumber();
                CallManager.getInstance().transfer(activity, number,
                        "", "", transferType == 0);
                return false;
            }
        });

    }

    public void setDialIvEnable(boolean enable) {
        callContainer.setEnabled(enable);
        callContainer.setAlpha(enable ? 1f : 0.5f);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().post(new AgentEvent(
                0, Activity.RESULT_CANCELED, null));
    }
}
