package com.yeastar.linkus.demo.utils.permission;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.permissionx.guolindev.PermissionMediator;
import com.permissionx.guolindev.PermissionX;
import com.yeastar.linkus.demo.R;

import java.util.List;

/**
 * Created by ted on 17-8-3.
 */

public class PermissionRequest {

    private Activity mActivity;
    private Fragment fragment;
    private PermissionCallback mCallback;

    public PermissionRequest(Activity activity, PermissionCallback callback) {
        this.mActivity = activity;
        this.mCallback = callback;
    }

    public PermissionRequest(Fragment fragment, PermissionCallback callback) {
        this.fragment = fragment;
        this.mCallback = callback;
    }

    public void hasPermission(String... permissions) {
        PermissionMediator permissionMediator = mActivity != null ? PermissionX.init((FragmentActivity) mActivity)
                : PermissionX.init(fragment);
        Context context = mActivity != null ? mActivity.getApplicationContext() : fragment.getContext();
        permissionMediator.permissions(permissions)
                .request((allGranted, grantedList, deniedList) -> {
                    if (allGranted) {
                        if (mCallback != null) {
                            mCallback.onSuccessful(grantedList);
                        }
                    } else {
                        if (mCallback != null) {
                            mCallback.onFailure(deniedList);
                            Toast.makeText(context, R.string.permission_grant, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public interface PermissionCallback {

        void onSuccessful(List<String> permissions);

        void onFailure(List<String> permissions);

    }

}

