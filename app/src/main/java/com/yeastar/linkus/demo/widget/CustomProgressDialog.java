package com.yeastar.linkus.demo.widget;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yeastar.linkus.demo.R;


public class CustomProgressDialog extends Dialog {

    private static final int TYPE_TEXT_SINGLE = 0;
    public static final int TYPE_TEXT_MULTIPLE = 1;
    private static final int TEXTVIEW_WIDTH_SINGLE_DP = 100;
    private static final int TEXTVIEW_WIDTH_MULTIPLE_DP = 180;
    private TextView textView = null;
    private ImageView imageView = null;
    private ProgressBar progressBar = null;

    public CustomProgressDialog(Context context, String text, boolean showTextId) {
        super(context, R.style.ProgressDialogStyle);
        init(context, TYPE_TEXT_SINGLE, text, showTextId);
    }

    public CustomProgressDialog(Context context, int textId, boolean showTextId) {
        super(context, R.style.ProgressDialogStyle);
        String text = showTextId ? context.getString(textId) : "";
        init(context, TYPE_TEXT_SINGLE, text, showTextId);
    }

    public CustomProgressDialog(Context context, int type, int textId, boolean showTextId) {
        super(context, R.style.ProgressDialogStyle);
        String text = showTextId ? context.getString(textId) : "";
        init(context, type, text, showTextId);
    }

    public CustomProgressDialog(Context context, int type, String text, boolean showTextId) {
        super(context, R.style.ProgressDialogStyle);
        init(context, type, text, showTextId);
    }

    private void init(Context context, int type, String textId, boolean showTextId) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_progress_dialog, null);
        textView = view.findViewById(R.id.tv_dialog);
        imageView = view.findViewById(R.id.iv_dialog);
        progressBar = view.findViewById(R.id.progress_bar_dialog);
        if(showTextId) {
            textView.setText(textId);
        }else{
            textView.setText("");
        }
        float density = context.getResources().getDisplayMetrics().density;
        int width;
        if (type == TYPE_TEXT_SINGLE) {
            width = TEXTVIEW_WIDTH_SINGLE_DP;
        } else {
            width = TEXTVIEW_WIDTH_MULTIPLE_DP;
        }
        width = (int) (width * density + 0.5f);
        textView.setMinWidth(width);
        setCancelable(false);
        setCanceledOnTouchOutside(false);
        setContentView(view);
    }

    public void setText(int textId) {
        String text = getContext().getResources().getString(textId);
        setText(text);
    }

    public void setText(String text) {
        textView.setText(text);
    }

    public void setImageView(int imageRes){
        progressBar.setVisibility(View.GONE);
        imageView.setVisibility(View.VISIBLE);
        imageView.setImageResource(imageRes);
    }

}
