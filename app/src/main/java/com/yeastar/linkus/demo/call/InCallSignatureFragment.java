package com.yeastar.linkus.demo.call;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.yeastar.linkus.demo.Constant;
import com.yeastar.linkus.demo.R;
import com.yeastar.linkus.demo.widget.Dialpad.DialPadLayout;
import com.yeastar.linkus.service.call.YlsCallManager;

/**
 * Created by lwh on 18-4-28.
 * 通话界面跳转的拨号界面
 */

public class InCallSignatureFragment extends InCallRelatedFragment {

    private DialPadLayout dialpadLayout;
    private int callId;
    private ConstraintLayout mClRoot;
    private LinearLayout mBackLayout;


    public InCallSignatureFragment() {
        super(R.layout.fragment_incall_signature);
    }

    @Override
    public void findView(View parent) {
        super.findView(parent);
        mBackLayout = parent.findViewById(R.id.back_layout);
        dialpadLayout = parent.findViewById(R.id.dial_pad_layout);
        mClRoot = parent.findViewById(R.id.signature_cl_root);
        Bundle bundle = getArguments();
        if (bundle != null) {
            callId = bundle.getInt(Constant.EXTRA_FROM, 100000);
        }
        setListener();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void setListener() {
        mBackLayout.setOnClickListener(v -> activity.getSupportFragmentManager().popBackStack());

        dialpadLayout.setDialNumberCallBack(number -> {
            if (!TextUtils.isEmpty(number)) {
                YlsCallManager.getInstance().record(callId, number);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dialpadLayout.mediaPlayRelease();
    }
}
