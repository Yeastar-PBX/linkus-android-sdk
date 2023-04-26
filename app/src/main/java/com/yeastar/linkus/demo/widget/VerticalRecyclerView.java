package com.yeastar.linkus.demo.widget;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class VerticalRecyclerView extends RecyclerView {
    private LinearLayoutManager layoutManager;

    public VerticalRecyclerView(@NonNull Context context) {
        this(context, null);
    }

    public VerticalRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VerticalRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.layoutManager = new WrapContentLinearLayoutManager(context);
        setLayoutManager(layoutManager);
    }

    @Nullable
    @Override
    public LinearLayoutManager getLayoutManager() {
        return layoutManager;
    }
}
