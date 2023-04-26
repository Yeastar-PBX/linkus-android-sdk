package com.yeastar.linkus.demo.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;

import com.yeastar.linkus.demo.R;


public class CrossFadeImageView extends AppCompatImageButton {
    private LayerDrawable layerDrawable;
    private float srcAlpha;
    private float altSrcAlpha;

    public CrossFadeImageView(@NonNull Context context) {
        super(context);
    }

    public CrossFadeImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public CrossFadeImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(@Nullable AttributeSet attrs) {
        TypedArray typedArray = this.getContext().obtainStyledAttributes(attrs, R.styleable.CrossFadeImageView);
        Drawable drawable = typedArray.getDrawable(R.styleable.CrossFadeImageView_altSrc);
        typedArray.recycle();
        if (drawable != null) {
            drawable.setAlpha(0);
            this.layerDrawable = new LayerDrawable(new Drawable[]{this.getDrawable(), drawable});
            super.setImageDrawable(this.layerDrawable);
        }
    }

    public final float getSrcAlpha() {
        return this.srcAlpha;
    }

    public final void setSrcAlpha(float value) {
        this.srcAlpha = value;
        LayerDrawable layerDrawable = this.layerDrawable;
        if (layerDrawable != null) {
            Drawable drawable = layerDrawable.getDrawable(0);
            if (drawable != null) {
                drawable.setAlpha((int)((float)255 * value));
            }
        }

        this.invalidate();
    }

    public final float getAltSrcAlpha() {
        return this.altSrcAlpha;
    }

    public final void setAltSrcAlpha(float value) {
        this.altSrcAlpha = value;
        LayerDrawable layerDrawable = this.layerDrawable;
        if (layerDrawable != null) {
            Drawable drawable = layerDrawable.getDrawable(1);
            if (drawable != null) {
                drawable.setAlpha((int)((float)255 * value));
            }
        }

        this.invalidate();
    }
}
