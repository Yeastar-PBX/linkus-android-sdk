package com.yeastar.linkus.demo.call.dialpad;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.yeastar.linkus.demo.R;
import com.yeastar.linkus.utils.MediaUtil;
import com.yeastar.linkus.utils.SoundManager;

import kale.adapter.item.AdapterItem;

public class CallDialPadItem implements AdapterItem<CallDialPadVo> {

    private View view;
    private ImageView mInCallBtnIconIv;
    private TextView mInCallBtnContentTv;
    private CallDialPad.DialPadCallBack callBack;
    private CallDialPadVo callDialPadVo;
    private int textColorNormal;
    private int textColorDisable;

    public CallDialPadItem(CallDialPad.DialPadCallBack callBack) {
        this.callBack = callBack;
    }

    @Override
    public int getLayoutResId() {
        return R.layout.item_incall_btn;
    }

    @Override
    public void bindViews(@NonNull View root) {
        view = root;
        mInCallBtnIconIv = root.findViewById(R.id.incall_btn_icon_iv);
        mInCallBtnContentTv = root.findViewById(R.id.incall_btn_content_tv);
        textColorDisable = mInCallBtnContentTv.getResources().getColor(R.color.color_text_disable);
        textColorNormal = mInCallBtnContentTv.getResources().getColor(R.color.color_text_normal);
    }

    @Override
    public void setViews() {
        view.setOnClickListener(v -> {
            if (callBack != null && callDialPadVo != null) {
                callBack.cllBack(callDialPadVo.getAction());
            }
        });
    }

    @Override
    public void handleData(CallDialPadVo callDialPadVo, int position) {
        this.callDialPadVo = callDialPadVo;
        if (callDialPadVo.isEnabled()) {
            mInCallBtnContentTv.setTextColor(textColorNormal);
        } else {
            mInCallBtnContentTv.setTextColor(textColorDisable);
        }
        if (callDialPadVo.isPressed()) {
            mInCallBtnIconIv.setBackgroundResource(R.drawable.shape_circle_dial_pressed);
        } else {
            mInCallBtnIconIv.setBackgroundResource(R.drawable.shape_circle_dial_normal);
        }
        switch (callDialPadVo.getAction()) {
            case CallDialPad.HOLD:
                mInCallBtnContentTv.setText(R.string.call_hold);
                if (callDialPadVo.isPressed()) {
                    mInCallBtnIconIv.setImageResource(R.mipmap.icon_hold_pressed);
                } else {
                    mInCallBtnIconIv.setImageResource(callDialPadVo.isEnabled() ? R.mipmap.icon_hold : R.mipmap.icon_hold_disable);
                }
                break;
            case CallDialPad.MUTE:
                mInCallBtnContentTv.setText(R.string.conference_mute);
                if (callDialPadVo.isPressed()) {
                    mInCallBtnIconIv.setImageResource(R.mipmap.icon_mute_pressed);
                } else {
                    mInCallBtnIconIv.setImageResource(callDialPadVo.isEnabled() ? R.mipmap.icon_mute : R.mipmap.icon_mute_disable);
                }
                break;
            case CallDialPad.AUDIO:
                //有蓝牙耳机时
                if (MediaUtil.getInstance().isBTConnected()) {
                    if (SoundManager.getInstance().isBluetoothAudio()) {
                        mInCallBtnContentTv.setText(R.string.call_bluetooth);
                        mInCallBtnIconIv.setImageResource(R.mipmap.icon_bluetooth);
                    } else if (SoundManager.getInstance().isSpeakerOn()) {
                        mInCallBtnContentTv.setText(R.string.call_audio_speaker);
                        mInCallBtnIconIv.setImageResource(R.mipmap.icon_speaker_pressed);
                    } else if (SoundManager.getInstance().isWiredHeadset()) {
                        mInCallBtnContentTv.setText(R.string.call_handset);
                        mInCallBtnIconIv.setImageResource(R.mipmap.icon_headset);
                    } else {
                        mInCallBtnContentTv.setText(R.string.call_earpiece);
                        mInCallBtnIconIv.setImageResource(R.mipmap.icon_earpiece);
                    }
                    mInCallBtnIconIv.setBackgroundResource(R.drawable.shape_circle_dial_pressed);
                } else {
                    mInCallBtnContentTv.setText(R.string.call_audio_speaker);
                    if (SoundManager.getInstance().isSpeakerOn()) {
                        mInCallBtnIconIv.setImageResource(R.mipmap.icon_speaker_pressed);
                        mInCallBtnIconIv.setBackgroundResource(R.drawable.shape_circle_dial_pressed);
                    } else {
                        mInCallBtnIconIv.setImageResource(R.mipmap.icon_speaker);
                        mInCallBtnIconIv.setBackgroundResource(R.drawable.shape_circle_dial_normal);
                    }
                }
                break;
            case CallDialPad.END_CALL:
                mInCallBtnContentTv.setText(R.string.call_endcall);
                mInCallBtnIconIv.setImageResource(R.mipmap.icon_end_call);
                mInCallBtnIconIv.setBackgroundResource(R.drawable.shape_circle_dial_red);
                break;
            case CallDialPad.ADD:
                mInCallBtnContentTv.setText(R.string.call_add);
                mInCallBtnIconIv.setImageResource(callDialPadVo.isEnabled() ? R.mipmap.icon_add : R.mipmap.icon_add_disable);
                break;
            case CallDialPad.DIAL_PAD:
                mInCallBtnContentTv.setText(R.string.public_dialpad);
                mInCallBtnIconIv.setImageResource(callDialPadVo.isEnabled() ? R.mipmap.icon_dial_pad : R.mipmap.icon_dial_pad_disable);
                break;
            case CallDialPad.RECORD:
                mInCallBtnContentTv.setText(R.string.record_record);
                if (callDialPadVo.isPressed()) {
                    mInCallBtnIconIv.setImageResource(callDialPadVo.isEnabled() ? R.mipmap.icon_record_pressed : R.mipmap.icon_record_pressed_disable);
                } else {
                    mInCallBtnIconIv.setImageResource(callDialPadVo.isEnabled() ? R.mipmap.icon_record : R.mipmap.icon_record_disable);
                }
                break;
            case CallDialPad.ATTENDED_TRANSFER:
                mInCallBtnContentTv.setText(R.string.call_tansfer_attended);
                mInCallBtnIconIv.setImageResource(callDialPadVo.isEnabled() ? R.mipmap.icon_attend : R.mipmap.icon_attend_disable);
                break;
            case CallDialPad.BLIND_TRANSFER:
                mInCallBtnContentTv.setText(R.string.call_tansfer_blind);
                mInCallBtnIconIv.setImageResource(callDialPadVo.isEnabled() ? R.mipmap.icon_blind : R.mipmap.icon_blind_disable);
                break;
            case CallDialPad.TRANSFER_CONFIRM:
                mInCallBtnContentTv.setText(R.string.call_tansfer_attended);
                mInCallBtnIconIv.setBackgroundResource(R.drawable.shape_circle_dial_green);
                mInCallBtnIconIv.setImageResource(callDialPadVo.isEnabled() ? R.mipmap.icon_attend : R.mipmap.icon_attend_disable);
                break;
            case CallDialPad.CANCEL:
                mInCallBtnContentTv.setText(R.string.call_cancel);
                mInCallBtnIconIv.setBackgroundResource(R.drawable.shape_circle_dial_red);
                mInCallBtnIconIv.setImageResource(R.mipmap.icon_cancel);
                break;
        }
        view.setEnabled(callDialPadVo.isEnabled());
    }
}
