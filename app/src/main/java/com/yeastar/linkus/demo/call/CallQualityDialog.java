package com.yeastar.linkus.demo.call;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;


import com.yeastar.linkus.demo.R;
import com.yeastar.linkus.service.call.vo.CallQualityVo;
import com.yeastar.linkus.service.log.LogUtil;

import java.util.Locale;


public class CallQualityDialog extends Dialog {

    private TextView mTvTxLossRate;
    private TextView mTvTxTotal;
    private TextView mTvTxLoss;
    private TextView mTvTxDup;
    private TextView mTvTxRecord;
    private TextView mTvTxJitter;
    private TextView mTvRxLossRate;
    private TextView mTvRxTotal;
    private TextView mTvRxLoss;
    private TextView mTvRxDup;
    private TextView mTvRxRecord;
    private TextView mTvRxJitter;
    private TextView mTvRemoteAddr;
    private Context mContext;
    private TextView mTvTxLevel;
    private TextView mTvRxLevel;

    private int callId = -1;


    public CallQualityDialog(Context context) {
        super(context, R.style.CustomDialogStyle);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.layout_call_quality_dialog, null);
        mTvTxLossRate = view.findViewById(R.id.tv_tx_loss_rate);
        mTvTxTotal = view.findViewById(R.id.tv_tx_total);
        mTvTxLoss = view.findViewById(R.id.tv_tx_loss);
        mTvTxDup = view.findViewById(R.id.tv_tx_dup);
        mTvTxRecord = view.findViewById(R.id.tv_tx_record);
        mTvTxJitter = view.findViewById(R.id.tv_tx_jitter);
        mTvRxLossRate = view.findViewById(R.id.tv_rx_loss_rate);
        mTvRxTotal = view.findViewById(R.id.tv_rx_total);
        mTvRxLoss = view.findViewById(R.id.tv_rx_loss);
        mTvRxDup = view.findViewById(R.id.tv_rx_dup);
        mTvRxRecord = view.findViewById(R.id.tv_rx_record);
        mTvRxJitter = view.findViewById(R.id.tv_rx_jitter);
        mTvRemoteAddr = view.findViewById(R.id.tv_remote_addr);
        mTvTxLevel = view.findViewById(R.id.tv_tx_level);
        mTvRxLevel = view.findViewById(R.id.tv_rx_level);
        mContext = context;
        setCancelable(true);
        setCanceledOnTouchOutside(true);
        setContentView(view);
    }

    public void showCallQuality(CallQualityVo callQualityModel) {
        LogUtil.w("callQualityModel=%s", callQualityModel.toString());
        mTvTxLossRate.setText(String.format(Locale.getDefault(), "Sender loss rate： %s", callQualityModel.getTxLossRate()));
        mTvTxTotal.setText(String.format(Locale.getDefault(), "Send RTP packets： %s", callQualityModel.getTxTotal()));
        mTvTxLoss.setText(String.format(Locale.getDefault(), "%s loss", callQualityModel.getTxLoss()));
        mTvTxDup.setText(String.format(Locale.getDefault(), "%s duplicate", callQualityModel.getTxDup()));
        mTvTxRecord.setText(String.format(Locale.getDefault(), "%s out-of-order", callQualityModel.getTxRecord()));
        mTvTxJitter.setText(String.format(Locale.getDefault(), "Jitter buffer size： %sms", callQualityModel.getTxJitter()));
        mTvTxLevel.setText(String.format(Locale.getDefault(), "TX Level： %s", callQualityModel.getTxLevel()));
        mTvRxLossRate.setText(String.format(Locale.getDefault(), "Receiver loss rate： %s", callQualityModel.getRxLossRate()));
        mTvRxTotal.setText(String.format(Locale.getDefault(), "Received RTP packets： %s", callQualityModel.getRxTotal()));
        mTvRxLoss.setText(String.format(Locale.getDefault(), "%s loss", callQualityModel.getRxLoss()));
        mTvRxDup.setText(String.format(Locale.getDefault(), "%s duplicate", callQualityModel.getRxDup()));
        mTvRxRecord.setText(String.format(Locale.getDefault(), "%s out-of-order", callQualityModel.getRxRecord()));
        mTvRxJitter.setText(String.format(Locale.getDefault(), "Jitter buffer size： %sms", callQualityModel.getRxJitter()));
        mTvRxLevel.setText(String.format(Locale.getDefault(), "RX Level： %s", callQualityModel.getRxLevel()));
        mTvRemoteAddr.setText(String.format(Locale.getDefault(), "From IP Address： %s", callQualityModel.getRemoteAddr()));
    }

    public void setCurrentCallId(int callId) {
        this.callId = callId;
    }

    public int getCallId() {
        return callId;
    }
}
