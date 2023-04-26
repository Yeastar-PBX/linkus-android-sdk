package com.yeastar.linkus.demo.utils.permission;

import android.app.Activity;

import com.permissionx.guolindev.PermissionX;

/**
 * Created by ted on 17-8-11.
 */

public class PermissionUtil {
    private Activity activity;

    public PermissionUtil(Activity activity) {
        this.activity = activity;
    }

    public boolean hasPermissions(String permissions){
        return PermissionX.isGranted(activity, permissions);
    }

}
