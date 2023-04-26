package com.yeastar.linkus.demo.base;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.yeastar.linkus.service.log.LogUtil;
import com.yeastar.linkus.demo.utils.ToastUtil;
import com.yeastar.linkus.demo.widget.CustomProgressDialog;

public abstract class BaseFragment extends Fragment implements BaseFragmentInterface {

    protected FragmentActivity activity = null;
    protected LayoutInflater inflater = null;
    private int layoutResID;
    private CustomProgressDialog progressDialog = null;
    private int containerId;
    protected String TAG = this.getClass().getSimpleName();
    private static final Handler handler = new Handler();
    public AudioManager audioManager;

    int getContainerId() {
        return containerId;
    }

    public void setContainerId(int containerId) {
        this.containerId = containerId;
    }

    public BaseFragment(int layoutResID) {
        this.layoutResID = layoutResID;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        LogUtil.w("fragment lifecycle:%s onAttach", this.getClass().getSimpleName());
        this.activity = (FragmentActivity) activity;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (activity == null) {
            activity = getActivity();
        }
        LogUtil.w("fragment lifecycle:%s onCreate", this.getClass().getSimpleName());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (activity == null) {
            activity = getActivity();
        }
        LogUtil.w("fragment lifecycle:%s onResume", this.getClass().getSimpleName());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LogUtil.w("fragment lifecycle:%s onCreateView", this.getClass().getSimpleName());
        this.inflater = inflater;
        if (activity == null) {
            activity = getActivity();
        }
        audioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
        View view = inflater.inflate(layoutResID, container, false);
        findView(view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (activity == null) {
            activity = getActivity();
        }
        LogUtil.w("fragment lifecycle:%s onViewCreated", this.getClass().getSimpleName());
    }

    @Override
    public void onStart() {
        super.onStart();
        if (activity == null) {
            activity = getActivity();
        }
        LogUtil.w("fragment lifecycle:%s onStart", this.getClass().getSimpleName());
    }

    @Override
    public void onPause() {
        super.onPause();
        LogUtil.w("fragment lifecycle:%s onPause", this.getClass().getSimpleName());
    }

    @Override
    public void onStop() {
        super.onStop();
        LogUtil.w("fragment lifecycle:%s onStop", this.getClass().getSimpleName());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LogUtil.w("fragment lifecycle:%s onDestroyView", this.getClass().getSimpleName());
    }

    @Override
    public void onDetach() {
        super.onDetach();
        LogUtil.w("fragment lifecycle:%s onDetach", this.getClass().getSimpleName());
    }

    public void showToast(int tipResId) {
        ToastUtil.showToast(tipResId);
    }

    public void showToast(String str) {
        ToastUtil.showToast(str);
    }

    public void showProgressDialog(int textId) {
        if (progressDialog == null) {
            if (textId != -1) {
                progressDialog = new CustomProgressDialog(activity, textId, true);
            } else {
                progressDialog = new CustomProgressDialog(activity, textId, false);
            }
            progressDialog.show();
        }
    }

    public void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtil.w("fragment lifecycle:%s onDestroy", this.getClass().getSimpleName());
    }

    protected final Handler getHandler() {
        return handler;
    }

    protected final void postRunnable(final Runnable runnable) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                // validate
                if (!isAdded()) {
                    return;
                }

                // run
                runnable.run();
            }
        });
    }

    protected final void postDelayed(final Runnable runnable, long delay) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // validate
                if (!isAdded()) {
                    return;
                }

                // run
                runnable.run();
            }
        }, delay);
    }

}
