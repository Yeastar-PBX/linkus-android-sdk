package com.yeastar.linkus.demo.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.yeastar.linkus.demo.R;

public class AvatarImageView extends AppCompatImageView {
    public AvatarImageView(@NonNull Context context) {
        super(context);
    }

    public AvatarImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AvatarImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void loadCirclePhotoUrl(String photoUrl) {
        Drawable placeholder = getDrawable();
        if (placeholder == null) {//平滑刷新头像,避免更新头像的时候先出现默认头像再出现更换后的头像
            placeholder = getResources().getDrawable(R.mipmap.default_contact_avatar);
        }
        Glide.with(this)
                .load(photoUrl)
                .apply(new RequestOptions()
                        .optionalCircleCrop()
                        .skipMemoryCache(false)
                        .dontAnimate()
                        .placeholder(placeholder)
                        .error(R.mipmap.default_contact_avatar))
                .into(this);
    }

    public void loadCirclePhotoUrlContext(Context context, String photoUrl) {
        Drawable placeholder = getDrawable();
        if (placeholder == null) {//平滑刷新头像,避免更新头像的时候先出现默认头像再出现更换后的头像
            placeholder = getResources().getDrawable(R.mipmap.default_contact_avatar);
        }
        Glide.with(context)
                .load(photoUrl)
                .apply(new RequestOptions()
                        .optionalCircleCrop()
                        .skipMemoryCache(false)
                        .dontAnimate()
                        .placeholder(placeholder)
                        .error(R.mipmap.default_contact_avatar))
                .into(this);
    }
}
