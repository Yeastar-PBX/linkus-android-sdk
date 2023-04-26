package com.yeastar.linkus.demo.call.Audio;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.yeastar.linkus.demo.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class AudioAdapter extends BaseQuickAdapter<AudioVo, BaseViewHolder> {

    public AudioAdapter(@Nullable List<AudioVo> data) {
        super(R.layout.item_popup_sound, data);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder baseViewHolder, AudioVo audioVo) {
        baseViewHolder.setText(R.id.tv_sound_name, audioVo.getName());
        baseViewHolder.setImageResource(R.id.iv_sound, audioVo.getIconResId());
        baseViewHolder.setVisible(R.id.iv_sound_selector, audioVo.isSelected());
        if (audioVo.isLast()){
            baseViewHolder.setGone(R.id.line_divider, true);
            baseViewHolder.setBackgroundResource(R.id.cl_sound,R.drawable.selector_bottom_round_rect);
        } else {
            baseViewHolder.setGone(R.id.line_divider, false);
            baseViewHolder.setBackgroundResource(R.id.cl_sound,R.drawable.selector_dialog_item);
        }
    }
}
