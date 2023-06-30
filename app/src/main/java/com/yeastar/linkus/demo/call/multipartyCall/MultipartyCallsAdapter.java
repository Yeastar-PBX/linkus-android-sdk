package com.yeastar.linkus.demo.call.multipartyCall;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.yeastar.linkus.demo.R;
import com.yeastar.linkus.demo.widget.AvatarImageView;
import com.yeastar.linkus.service.call.vo.InCallVo;

import java.util.Objects;

public class MultipartyCallsAdapter extends BaseQuickAdapter<InCallVo, BaseViewHolder> {

    int callId;
    int level;
    boolean init;

    public MultipartyCallsAdapter() {
        super(R.layout.item_multi_party_call_manager);
        addChildClickViewIds(R.id.ivMute, R.id.ivRemove, R.id.ivQuailty);
    }

    public void setCallQualityLevel(int callId, int level) {
        this.init = true;
        this.callId = callId;
        this.level = level;
    }

    @Override
    protected void convert(@NonNull BaseViewHolder holder, InCallVo inCallModel) {
        AvatarImageView ivAvatar = holder.getView(R.id.ivAvatar);
        String callName = TextUtils.isEmpty(inCallModel.getCallName())?inCallModel.getCallNumber():inCallModel.getCallName();
        String number = inCallModel.getCallNumber();
//        ivAvatar.loadCirclePhotoUrl(photoUri);
        if (Objects.equals(callName, number)) {
            holder.setGone(R.id.tvNumber, true);
        } else {
            holder.setGone(R.id.tvNumber, false);
        }
        holder.setText(R.id.tvName, callName);
        holder.setText(R.id.tvNumber, number);
        boolean mute = inCallModel.isMute();
        holder.setImageResource(R.id.ivMute, mute ? R.mipmap.icon_mute_small : R.mipmap.icon_unmute_small);
        holder.setImageResource(R.id.ivRemove, R.mipmap.icon_call_remove);

        if (init) {
            if (callId == inCallModel.getCallId()) {
                if (level > 2) {
                    holder.setImageResource(R.id.ivQuailty, R.mipmap.icon_call_quality);
                } else {
                    holder.setImageResource(R.id.ivQuailty, R.mipmap.icon_call_quality_red);
                }
            }
        }
    }
}
