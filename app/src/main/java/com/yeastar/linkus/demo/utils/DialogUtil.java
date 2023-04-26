package com.yeastar.linkus.demo.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.yeastar.linkus.demo.R;


public class DialogUtil {

    public static void showSingleDialog(Context context, int title,
                        int msg, int positive, DialogInterface.OnClickListener l, boolean cancelable) {
        AlertDialog singleDialog = createSingleDialog(context, title, msg, positive, l);
        singleDialog.setCancelable(cancelable);
        singleDialog.show();
        singleDialog.getButton(AlertDialog.BUTTON_POSITIVE).setAllCaps(false);
    }

    public static void showSingleDialog(Context context, String title,
                                        String msg, int positive, DialogInterface.OnClickListener l, boolean cancelable) {
        AlertDialog singleDialog = createSingleDialog(context, title, msg, positive, l);
        singleDialog.setCancelable(cancelable);
        singleDialog.show();
        singleDialog.getButton(AlertDialog.BUTTON_POSITIVE).setAllCaps(false);
    }

    public static AlertDialog showDoubleDialog(Context context, int title, int msg, int positive,
            int negative, DialogInterface.OnClickListener l1, DialogInterface.OnClickListener l2, boolean cancelable) {
        String titleStr = title != 0 ? context.getString(title) : "";
        return showDoubleDialog(context, titleStr, context.getString(msg), positive, negative, l1, l2, cancelable);
    }

    public static AlertDialog showDoubleDialog(Context context, String title, String msg, int positive,
                                        int negative, DialogInterface.OnClickListener l1, DialogInterface.OnClickListener l2, boolean cancelable) {
        AlertDialog doubleDialog = createDoubleDialog(context, title, msg, positive, negative, l1, l2);
        doubleDialog.setCancelable(cancelable);
        doubleDialog.show();
        doubleDialog.getButton(AlertDialog.BUTTON_POSITIVE).setAllCaps(false);
        doubleDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setAllCaps(false);
        return doubleDialog;
    }

    private static AlertDialog createSingleDialog(Context context, String title, String msg, int positive, DialogInterface.OnClickListener l) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.BDAlertDialog);
        if (!TextUtils.isEmpty(title)) {
            SpannableStringBuilder spTitle = getSp(context, title, R.color.text_title);
            builder.setTitle(spTitle);
        }
        builder.setMessage(msg).setPositiveButton(positive, l);
        return builder.create();
    }

    private static AlertDialog createSingleDialog(Context context, int title, int msg, int positive, DialogInterface.OnClickListener l) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.BDAlertDialog);
        if (title != 0) {
            SpannableStringBuilder spTitle = getSp(context, title, R.color.text_title);
            builder.setTitle(spTitle);
        }
        builder.setMessage(msg).setPositiveButton(positive, l);
        return builder.create();
    }

    private static AlertDialog createDoubleDialog(Context context, String title, String msg, int positive,
                                                  int negative, DialogInterface.OnClickListener l1, DialogInterface.OnClickListener l2) {
        SpannableStringBuilder spNegative = getSp(context, negative, R.color.text_sub_title);
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.BDAlertDialog);
        if (!TextUtils.isEmpty(title)) {
            SpannableStringBuilder spTitle = getSp(context, title, R.color.text_title);
            builder.setTitle(spTitle);
        }
        builder.setMessage(msg)
                .setPositiveButton(positive, l1)
                .setNegativeButton(spNegative, l2);
        return builder.create();
    }

    private static SpannableStringBuilder getSp(Context context, int txt, int intColor) {
        String negativeStr = context.getString(txt);
        SpannableStringBuilder sp = new SpannableStringBuilder(negativeStr);
        int color = ContextCompat.getColor(context, intColor);
        ForegroundColorSpan blueSpan = new ForegroundColorSpan(color);
        sp.setSpan(blueSpan,0, negativeStr.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return sp;
    }

    private static SpannableStringBuilder getSp(Context context, String txt, int intColor) {
        SpannableStringBuilder sp = new SpannableStringBuilder(txt);
        int color = ContextCompat.getColor(context, intColor);
        ForegroundColorSpan blueSpan = new ForegroundColorSpan(color);
        sp.setSpan(blueSpan,0, txt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return sp;
    }
}
