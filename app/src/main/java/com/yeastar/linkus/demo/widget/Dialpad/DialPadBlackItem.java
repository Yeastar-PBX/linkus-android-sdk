package com.yeastar.linkus.demo.widget.Dialpad;

import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;


import com.yeastar.linkus.demo.R;
import com.yeastar.linkus.utils.CommonUtil;

import kale.adapter.item.AdapterItem;

/**
 * Created by ted on 17-10-23.
 */

public class DialPadBlackItem implements AdapterItem<DialPadModel> {

    private DialPadLayout.DialCallBack callBack;
    private int position;
    private String callNumber;

    DialPadBlackItem(DialPadLayout.DialCallBack callBack) {
        this.callBack = callBack;
    }

    @Override
    public int getLayoutResId() {
        return R.layout.item_dialpad_black;
    }

    private TextView incallBtnMainTv;
    private TextView incallBtnContentTv;
    private View view;

    @Override
    public void bindViews(@NonNull View view) {
        this.view = view;
        incallBtnMainTv = view.findViewById(R.id.incall_btn_main_tv);
        incallBtnMainTv.setTypeface(CommonUtil.getBoldTypeface());
        incallBtnContentTv = view.findViewById(R.id.incall_btn_content_tv);
    }

    @Override
    public void setViews() {
        if (view != null) {
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (callBack != null) {
                        callBack.onItemClick(position, callNumber);
                    }
                }
            });

            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (callBack != null) {
                        callBack.onItemKLongClick(position, callNumber);
                    }
                    return true;
                }
            });
        }
    }

    @Override
    public void handleData(DialPadModel dialpadModel, int position) {
        this.position = position;
        callNumber = dialpadModel.getMainText();
        String mainText = dialpadModel.getMainText();
        if (!TextUtils.isEmpty(mainText)) {
            incallBtnMainTv.setVisibility(View.VISIBLE);
            incallBtnMainTv.setText(mainText);
        } else {
            incallBtnMainTv.setVisibility(View.GONE);
        }
        String subText = dialpadModel.getSubText();
        if (!TextUtils.isEmpty(subText)) {
            incallBtnContentTv.setVisibility(View.VISIBLE);
            incallBtnContentTv.setText(subText);
        } else {
            incallBtnContentTv.setVisibility(View.GONE);
        }
        if (position == 9) {
            callNumber = "*";
        }
    }
}
