package com.yeastar.linkus.demo.conference.detail;

import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.yeastar.linkus.demo.R;
import com.yeastar.linkus.demo.utils.OnNoDoubleClickListener;
import com.yeastar.linkus.demo.widget.AvatarImageView;
import com.yeastar.linkus.service.conference.vo.ConferenceMemberVo;

import kale.adapter.item.AdapterItem;


/**
 * Created by ted on 17-5-11.
 */

public class ConferenceDetailItem implements AdapterItem<ConferenceMemberVo> {

    @Override
    public int getLayoutResId() {
        return R.layout.item_conference_new;
    }

    private AvatarImageView avatarCiv = null;
    private ImageView deleteIv = null;
    private TextView nameTv = null;
    private TextView numberTv = null;
    private View rlAvatarContainer = null;
    private int position;
    private ItemCallBack itemCallBack;

    ConferenceDetailItem(ItemCallBack itemCallBack) {
        this.itemCallBack = itemCallBack;
    }

    @Override
    public void bindViews(@NonNull View convertView) {
        rlAvatarContainer = convertView.findViewById(R.id.avatar_container);
        avatarCiv = convertView.findViewById(R.id.avatar_civ);
        deleteIv = convertView.findViewById(R.id.delete_iv);
        nameTv = convertView.findViewById(R.id.name_tv);
        numberTv = convertView.findViewById(R.id.number_tv);
    }

    @Override
    public void setViews() {
        if (itemCallBack != null) {
            avatarCiv.setOnClickListener(new OnNoDoubleClickListener() {
                @Override
                protected void onNoDoubleClick(View v) {
                    ConferenceMemberVo.MemberTag tag = (ConferenceMemberVo.MemberTag) nameTv.getTag();
                    if (tag == ConferenceMemberVo.MemberTag.ADD) {
                        itemCallBack.addMember();
                    } else if (tag == ConferenceMemberVo.MemberTag.DELETE) {
                        itemCallBack.removeMember();
                    }
                }
            });
            deleteIv.setOnClickListener(new OnNoDoubleClickListener() {
                @Override
                protected void onNoDoubleClick(View v) {
                    itemCallBack.deleteContact(position);
                }
            });
        }
    }

    @Override
    public void handleData(ConferenceMemberVo model, int position) {
        this.position = position;
        nameTv.setTag(model.getTag());
        if (isAdd(model)) {
            avatarCiv.setImageResource(R.drawable.selector_conference_add_member);
            nameTv.setText(nameTv.getContext().getString(R.string.conference_add, 8 - position));
            numberTv.setText("");
        } else if (isDelete(model)) {
            avatarCiv.setImageResource(R.drawable.selector_conference_remove_member);
            nameTv.setText(R.string.public_delete);
            numberTv.setText("");
        } else {
            String photoUrl = model.getPhotoUri();
            String name = model.getName();
            avatarCiv.loadCirclePhotoUrl(photoUrl);
            nameTv.setText(TextUtils.isEmpty(name) ? model.getOriginalNumber() : name);
            numberTv.setText(model.getOriginalNumber());
        }

        boolean able = model.isDeleteAble();
        Animation animation = AnimationUtils.loadAnimation(rlAvatarContainer.getContext().getApplicationContext(), R.anim.anim_item_shake);
        if (able) {
            deleteIv.setVisibility(View.VISIBLE);
            rlAvatarContainer.startAnimation(animation);
        } else {
            deleteIv.setVisibility(View.GONE);
            rlAvatarContainer.clearAnimation();
        }
    }

    private boolean isAdd(ConferenceMemberVo model) {
        return model.getTag() == ConferenceMemberVo.MemberTag.ADD;
    }

    private boolean isDelete(ConferenceMemberVo model) {
        return model.getTag() == ConferenceMemberVo.MemberTag.DELETE;
    }

    public interface ItemCallBack {

        void deleteContact(int position);

        void removeMember();

        void addMember();

    }

}
