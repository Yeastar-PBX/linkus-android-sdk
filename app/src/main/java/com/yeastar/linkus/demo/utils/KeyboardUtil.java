package com.yeastar.linkus.demo.utils;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

/**
 * Created by root on 15-10-20.
 */
public class KeyboardUtil {
    /**
     * 打开输入键盘
     *
     * @param activity
     * @param editText
     */
    public static void showKeypad(Activity activity, View editText) {
        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
        editText.requestFocus();
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
    }

    /**
     * 关闭输入键盘
     *
     * @param context
     * @param edittext
     */
    public static void closeKeyboard(Context context, EditText edittext) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(edittext.getWindowToken(), 0);
    }

    /**
     * 关闭输入键盘
     *
     * @param activity
     */
    public static void closeKeypad(Activity activity) {
        View view = activity.getWindow().peekDecorView();
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
