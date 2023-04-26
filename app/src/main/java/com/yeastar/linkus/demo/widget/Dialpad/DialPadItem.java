package com.yeastar.linkus.demo.widget.Dialpad;

import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.widget.TextViewCompat;


import com.yeastar.linkus.demo.R;

import kale.adapter.item.AdapterItem;

/**
 * Created by ted on 17-4-13.
 */

public class DialPadItem implements AdapterItem<DialPadModel> {


    private DialPadLayout.DialCallBack callBack;
    private int position;
    private String callNumber;
    private TextView mDialNumberTv;
    private TextView mDialLetterTv;
    private View view;

    DialPadItem(DialPadLayout.DialCallBack callBack) {
        this.callBack = callBack;
    }


    @Override
    public int getLayoutResId() {
        return R.layout.item_dialpad;
    }

    @Override
    public void bindViews(@NonNull View view) {
        this.view = view;
        mDialNumberTv = view.findViewById(R.id.dial_number_tv);
        mDialLetterTv = view.findViewById(R.id.dial_letter_tv);
    }

    @Override
    public void setViews() {
        view.setOnClickListener(v -> {
            if (callBack != null) {
                callBack.onItemClick(position, callNumber);
            }
        });

        view.setOnLongClickListener(v -> {
            if (callBack != null) {
                callBack.onItemKLongClick(position, callNumber);
            }
            return true;
        });
    }

    @Override
    public void handleData(DialPadModel dialpadModel, int position) {
        this.position = position;
        String mainText = dialpadModel.getMainText();
        callNumber = mainText;
        if (!TextUtils.isEmpty(mainText)) {
            mDialNumberTv.setVisibility(View.VISIBLE);
            mDialNumberTv.setText(mainText);
        } else {
            mDialNumberTv.setVisibility(View.GONE);
        }
        String subText = dialpadModel.getSubText();
        if (!TextUtils.isEmpty(subText)) {
            mDialLetterTv.setVisibility(View.VISIBLE);
            mDialLetterTv.setText(subText);
        } else {
            mDialLetterTv.setVisibility(View.GONE);
        }
        if (position == 9) {
            callNumber = "*";
            TextViewCompat.setAutoSizeTextTypeWithDefaults(mDialNumberTv, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);
            TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(mDialNumberTv, 30,38,2, TypedValue.COMPLEX_UNIT_SP);
        }
    }

}
