package com.yeastar.linkus.demo.utils;

import android.annotation.SuppressLint;
import android.view.Gravity;

import com.hjq.toast.ToastUtils;

public class ToastUtil {

    @SuppressLint("ShowToast")
    public static void showToast(String msg) {
        ToastUtils.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, 0, 240);
        ToastUtils.show(msg);
    }

    @SuppressLint("ShowToast")
    public static void showToastInCenter(String msg) {
        ToastUtils.setGravity(Gravity.CENTER, 0, 0);
        ToastUtils.show(msg);
    }

    @SuppressLint("ShowToast")
    public static void showToast(int msgId) {
        ToastUtils.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, 0, 240);
        ToastUtils.show(msgId);
    }

    @SuppressLint("ShowToast")
    public static void showToastInCenter(int msgId) {
        ToastUtils.setGravity(Gravity.CENTER, 0, 0);
        ToastUtils.show(msgId);
    }

    @SuppressLint("ShowToast")
    public static void showLongToast(String msg) {
        ToastUtils.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, 0, 240);
        ToastUtils.show(msg);
    }

    @SuppressLint("ShowToast")
    public static void showLongToast(int msgId) {
        ToastUtils.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, 0, 240);
        ToastUtils.show(msgId);
    }

    @SuppressLint("ShowToast")
    public static void showToastView(int msgId, int layoutId) {
        ToastUtils.setView(layoutId);
        ToastUtils.setGravity(Gravity.CENTER);
        ToastUtils.show(msgId);
    }
}
