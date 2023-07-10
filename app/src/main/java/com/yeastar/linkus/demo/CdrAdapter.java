package com.yeastar.linkus.demo;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.yeastar.linkus.constant.YlsConstant;
import com.yeastar.linkus.service.call.vo.CdrVo;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class CdrAdapter extends BaseQuickAdapter<CdrVo, BaseViewHolder> {
    public CdrAdapter() {
        super(R.layout.item_calllog);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder baseViewHolder, CdrVo cdrVo) {
        boolean isCallOut = cdrVo.getStatus().equals(CdrVo.CALL_STATUS_CALLOUT);
        String number = isCallOut ? cdrVo.getCallee() : cdrVo.getCaller();
        baseViewHolder.setGone(R.id.iv_type_icon, !isCallOut);
        if (Objects.equals(cdrVo.getConference(), YlsConstant.CONFERENCE)) {
            number = "<Conference> "+number;
        }
        baseViewHolder.setText(R.id.tv_name, number);
        if (!TextUtils.isEmpty(cdrVo.getStartTime()) && TextUtils.isDigitsOnly(cdrVo.getStartTime())) {
            DateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            long ms = Long.valueOf(cdrVo.getStartTime()) * 1000;
            String timeStr = dateTimeFormat.format(new Date(ms));
            baseViewHolder.setText(R.id.tv_call_log_datetime, timeStr);
        }
    }
}
