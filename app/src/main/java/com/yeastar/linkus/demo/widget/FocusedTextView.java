package com.yeastar.linkus.demo.widget;

import android.content.Context;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

public class FocusedTextView extends AppCompatTextView {
    public FocusedTextView(Context context) {
        super(context);
    }

    public void init() {
        setFocusable(true);
        setFocusableInTouchMode(true);
        setSingleLine();
        setEllipsize(TextUtils.TruncateAt.END);
        setMarqueeRepeatLimit(-1);
    }

    public FocusedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FocusedTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        if(focused) {
            super.onFocusChanged(true, direction, previouslyFocusedRect);
        }
    }

    public void onWindowFocusChanged(boolean hasWindowFocus) {
        if(hasWindowFocus) {
            super.onWindowFocusChanged(true);
        }
    }

    @Override
    public boolean isFocused() {
        return true;
    }
}
