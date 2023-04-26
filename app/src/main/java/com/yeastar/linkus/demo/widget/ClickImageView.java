package com.yeastar.linkus.demo.widget;

import android.content.Context;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.appcompat.widget.AppCompatImageView;

public class ClickImageView extends AppCompatImageView {

    private int disableColor = -1;

    public ClickImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public ClickImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ClickImageView(Context context) {
        super(context);
        init();
    }


    private void init() {
        setOnTouchListener(onTouchListener);
    }

    private OnTouchListener onTouchListener = (v, event) -> {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                setImageAlpha(153);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                setImageAlpha(255);
                break;
            default:
                break;
        }
        return false;
    };

    public void setDisableColor(int disableColor) {
        this.disableColor = disableColor;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (enabled) {
            setImageAlpha(255);
            clearColorFilter();
        } else {
            if (disableColor != -1) {
                setColorFilter(getResources().getColor(disableColor), PorterDuff.Mode.SRC_IN);
            } else {
                setImageAlpha(127);
            }
        }
    }
}

