package com.yeastar.linkus.demo.call.multipartyCall;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;

import com.yeastar.linkus.demo.R;
import com.yeastar.linkus.demo.call.CallManager;
import com.yeastar.linkus.demo.call.InCallRelatedFragment;
import com.yeastar.linkus.demo.eventbus.AgentEvent;
import com.yeastar.linkus.demo.widget.Dialpad.DialPadLayout;
import com.yeastar.linkus.service.call.YlsCallManager;

import org.greenrobot.eventbus.EventBus;


public class MultipartyCallFragment extends InCallRelatedFragment {

    private LinearLayout mBackLayout;
    private LinearLayout historyContainer = null;
    private LinearLayout contactContainer = null;
    private LinearLayout dialContainer = null;
    private String number = "";
    private DialPadLayout dialpadLayout;

    public MultipartyCallFragment() {
        super(R.layout.fragment_incall_transfer);
    }

    @Override
    public void findView(View parent) {
        super.findView(parent);
        mBackLayout = parent.findViewById(R.id.back_layout);
        dialpadLayout = parent.findViewById(R.id.dial_pad_layout);
        historyContainer = parent.findViewById(R.id.incall_history_container);
        contactContainer = parent.findViewById(R.id.incall_contact_container);
        dialContainer = parent.findViewById(R.id.incall_call);
        setDialIvEnable(false);
        setListener();
    }

    @Override
    public void onResume() {
        super.onResume();
        YlsCallManager.getInstance().setInMultipartyCall(true);
    }

    public void setListener() {

        mBackLayout.setOnClickListener(v -> {
            doBackPressed();
            activity.getSupportFragmentManager().popBackStack();
        });

        dialpadLayout.setDialPadCallBack(number -> {
            if (TextUtils.isEmpty(number)) {
                setDialIvEnable(false);
            } else {
                setDialIvEnable(true);
            }
        });

        historyContainer.setOnClickListener(v -> startFragment());

        contactContainer.setOnClickListener(v -> startFragment());

        dialContainer.setOnClickListener(v -> {
            number = dialpadLayout.getInputNumber();
            CallManager.getInstance().makeMultipartyCall(number, "", "", activity);
        });

        dialContainer.setOnLongClickListener(v -> {
            number = dialpadLayout.getInputNumber();
            CallManager.getInstance().makeMultipartyCall(number, "", "", activity);
            return false;
        });

    }

    private void startFragment() {
    }

    public void setDialIvEnable(boolean enable) {
        dialContainer.setEnabled(enable);
        dialContainer.setAlpha(enable ? 1f : 0.5f);
    }

    public void doBackPressed() {
        EventBus.getDefault().postSticky(new AgentEvent(1, Activity.RESULT_CANCELED, null));
    }
}
