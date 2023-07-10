package com.yeastar.linkus.demo.conference;

import android.graphics.drawable.Animatable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.yeastar.linkus.demo.R;
import com.yeastar.linkus.demo.widget.AvatarImageView;
import com.yeastar.linkus.service.conference.vo.ConferenceMemberVo;

import kale.adapter.item.AdapterItem;

/**
 * Created by ted on 17-4-27.
 */

public class ConferenceInCallItem implements AdapterItem<ConferenceMemberVo> {

    private OnConferenceInCallListener inCallListener;

    ConferenceInCallItem(OnConferenceInCallListener inCallListener) {
        this.inCallListener = inCallListener;
    }

    @Override
    public int getLayoutResId() {
        return R.layout.item_conference_incall;
    }

    private View avatarContainer = null;
    private AvatarImageView avatarCiv = null;
    private TextView nameTv = null;
    private TextView numberTv = null;
    private ImageView statusIv = null;
    private View coverContainer;
    private ImageView avatarCoverIv;
    private ConferenceMemberVo memberModel;

    @Override
    public void bindViews(@NonNull View view) {
        avatarContainer = view.findViewById(R.id.avatar_container);
        avatarCiv = view.findViewById(R.id.avatar_civ);
        nameTv = view.findViewById(R.id.name_tv);
        statusIv = view.findViewById(R.id.call_status);
        numberTv = view.findViewById(R.id.number_tv);
        coverContainer = view.findViewById(R.id.rl_cover);
        avatarCoverIv = view.findViewById(R.id.avatar_cover);
    }

    @Override
    public void setViews() {
        avatarContainer.setOnClickListener(v -> {
            if (inCallListener != null) {
                if (memberModel.getTag() == ConferenceMemberVo.MemberTag.NORMAL) {
                    inCallListener.operate(memberModel);
                } else if (memberModel.getTag() == ConferenceMemberVo.MemberTag.ADD) {
                    inCallListener.onAddMemberClick();
                }
            }
        });
    }

    @Override
    public void handleData(ConferenceMemberVo model, final int position) {
        if (model.getTag() == ConferenceMemberVo.MemberTag.NORMAL) {
            avatarContainer.setBackgroundResource(R.drawable.colorful_ring);
            avatarCiv.setVisibility(View.VISIBLE);
            avatarCiv.loadCirclePhotoUrl(model.getPhotoUri());
            nameTv.setText(model.getName());
            int statusDrawableId;
            if (model.isMute()) {
                statusDrawableId = R.drawable.conference_status_succ;
                avatarCoverIv.setImageResource(R.drawable.conference_item_mute);
                coverContainer.setVisibility(View.VISIBLE);
            } else {
                statusDrawableId = R.drawable.conference_status_succ;
                coverContainer.setVisibility(View.GONE);
            }
            if (model.getStatus() == 0) {
                statusDrawableId = R.drawable.conference_status_incall;
                avatarCoverIv.setImageDrawable(nameTv.getContext().getApplicationContext().getResources().getDrawable(R.drawable.animlist_ring));
                Animatable drawable = (Animatable) avatarCoverIv.getDrawable();
                drawable.start();
                coverContainer.setVisibility(View.VISIBLE);
            } else if (model.getStatus() == 1) {
                statusDrawableId = R.drawable.conference_status_succ;
            } else if (model.getStatus() == 5) {
                statusDrawableId = R.drawable.conference_status_err;
            } else if (model.getStatus() == 6) {
                statusDrawableId = R.drawable.conference_status_offline;
            }
            statusIv.setBackgroundResource(statusDrawableId);
            numberTv.setText(model.getOriginalNumber());
        } else if (model.getTag() == ConferenceMemberVo.MemberTag.ADD) {
            avatarContainer.setVisibility(View.VISIBLE);
            avatarCiv.setVisibility(View.GONE);
            nameTv.setText(nameTv.getContext().getString(R.string.conference_add, 8 - position));
            statusIv.setVisibility(View.GONE);
            numberTv.setVisibility(View.INVISIBLE);
            avatarContainer.setBackgroundResource(R.drawable.selector_in_call_add_member);
            coverContainer.setVisibility(View.GONE);
        }

        memberModel = model;
    }

    public interface OnConferenceInCallListener {

        void operate(ConferenceMemberVo memberModel);

        void onAddMemberClick();
    }
}
