package com.yeastar.linkus.demo.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;

/**
 * Created by ted on 18-12-8.
 */
public class DensityUtil {

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dp(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 sp 的单位 转成为 px(像素)
     */
    public static float sp2px(Context context, float value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, value, context.getResources().getDisplayMetrics());
    }

    /**
     * Get Display
     * @param context Context for get WindowManager
     * @return Display
     */
    private static Display getDisplay(Context context) {
        WindowManager wm;
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            wm = activity.getWindowManager();
        } else {
            wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        }
        if (wm != null) {
            return wm.getDefaultDisplay();
        }
        return null;
    }

    public static int getScreenWidth(Context context) {
        Display display = getDisplay(context);
        if (display == null) {
            return 0;
        }
        Point point = new Point();
        display.getSize(point);
        return point.x;
    }

    public static int getScreenHeight(Context context) {
        Display display = getDisplay(context);
        if (display == null) {
            return 0;
        }
        Point point = new Point();
        display.getSize(point);
        return point.y;
    }
}
