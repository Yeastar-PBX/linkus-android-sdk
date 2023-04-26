package com.yeastar.linkus.demo.utils;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Region;
import android.media.AudioManager;
import android.os.Build;
import android.os.Looper;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import androidx.annotation.RequiresApi;

import com.yeastar.linkus.service.log.LogUtil;
import com.yeastar.linkus.utils.CommonUtil;
import com.yxf.clippathlayout.Config;
import com.yxf.clippathlayout.NativePathRegion;
import com.yxf.clippathlayout.PathInfo;
import com.yxf.clippathlayout.PathRegion;

import java.util.List;

public class Utils {

    private static final String TAG = getTAG(Utils.class);

    public static boolean DEUBG = false;

    public static void clipOutPath(Canvas canvas, Path path) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            canvas.clipOutPath(path);
        } else {
            canvas.clipPath(path, Region.Op.DIFFERENCE);
        }
    }

    public static void clipPath(Canvas canvas, Path path, int clipType) {
        if (clipType == PathInfo.CLIP_TYPE_IN) {
            canvas.clipPath(path);
        } else if (clipType == PathInfo.CLIP_TYPE_OUT) {
            clipOutPath(canvas, path);
        } else {
            Log.e(TAG, "clipPath: unsupported clip type : " + clipType);
        }
    }

    public static boolean isInUiThread() {
        return Thread.currentThread() == Looper.getMainLooper().getThread();
    }

    public static boolean isViewCanUse(View view) {
        return isInUiThread() && (view.getWidth() > 0);
    }

    public static void runOnUiThreadAfterViewCanUse(View view, Runnable runnable) {
        if (isViewCanUse(view)) {
            runnable.run();
        } else {
            view.post(runnable);
        }
    }

    public static String getTAG(Class c) {
        return Config.libTAG + "." + c.getSimpleName();
    }


    public static Rect maxContainSimilarRange(Path path, Rect similar, int boundWidth, int boundHeight) {
        PathRegion region = new NativePathRegion(path, PathInfo.CLIP_TYPE_IN);
        if (isRectInRegion(region, similar)) {
            return similar;
        }
        Rect result = similar;
        int centerX = boundWidth / 2;
        int centerY = boundHeight / 2;

        int outLeft, outTop, outRight, outBottom;
        int inLeft, inTop, inRight, inBottom;
        int left, top, right, bottom;
        outLeft = similar.left;
        outTop = similar.top;
        outRight = similar.right;
        outBottom = similar.bottom;
        inLeft = centerX;
        inTop = centerY;
        inRight = centerX;
        inBottom = centerY;
        while (true) {
            left = (outLeft + inLeft) / 2;
            top = (outTop + inTop) / 2;
            right = (outRight + inRight) / 2;
            bottom = (outBottom + inBottom) / 2;
            if (isRectInRegion(region, left, top, right, bottom)) {
                inLeft = left;
                inTop = top;
                inRight = right;
                inBottom = bottom;
            } else {
                outLeft = left;
                outTop = top;
                outRight = right;
                outBottom = bottom;
            }
            if (Math.abs(outLeft - inLeft) <= 1 &&
                    Math.abs(outTop - inTop) <= 1 &&
                    Math.abs(outRight - inRight) <= 1 &&
                    Math.abs(outBottom - inBottom) <= 1) {
                result.set(inLeft, inTop, inRight, inBottom);
                break;
            }
        }
        return result;
    }

    public static boolean isRectInRegion(PathRegion region, Rect rect) {
        return isRectInRegion(region, rect.left, rect.top, rect.right, rect.bottom);
    }

    public static boolean isRectInRegion(PathRegion region, int left, int top, int right, int bottom) {
        return (region.isInRegion(left, top) &&
                region.isInRegion(right, top) &&
                region.isInRegion(left, bottom) &&
                region.isInRegion(right, bottom));
    }

    /**
     * 实现来电音乐暂停，通话结束后自动播放音乐
     *
     * @param context 上下文对象
     * @param isPause 是否暂停
     */
    public static void playOrPauseMusic(Context context, boolean isPause) {
        if (context == null) {
            return;
        }
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (am != null) {
            if (isPause) {
                am.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
            } else {
                am.abandonAudioFocus(null);
            }
        }
    }

    /**
     * 程序是否在前台运行
     * (context=)getApplicationContext()
     */
    public static boolean isAppOnForeground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        String packageName = context.getPackageName();
        List<ActivityManager.RunningAppProcessInfo> appProcesses = null;
        if (activityManager != null) {
            appProcesses = activityManager
                    .getRunningAppProcesses();
        }
        if (!CommonUtil.isListNotEmpty(appProcesses)) {
            return false;
        }
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            // The name of the process that this object is associated with.
            if (appProcess.processName.equals(packageName)
                    && appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断某个界面是否在前台
     *
     * @param context   上下文对象
     * @param className 某个界面名称
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static boolean isActivityForeground(Context context, String className) {
        boolean isActivityForeground = false;
        if (context == null || TextUtils.isEmpty(className)) {
            isActivityForeground = false;
        } else {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> list = null;
            if (am != null) {
                list = am.getRunningTasks(1);
            }
            if (CommonUtil.isListNotEmpty(list)) {
                ComponentName cpn = list.get(0).topActivity;
                isActivityForeground = className.equals(cpn.getClassName());
            }
        }
        LogUtil.w("%s isActivityForeground:" + isActivityForeground, className);
        return isActivityForeground;
    }

    /**
     * 判断是否锁屏（包括滑动解锁）
     *
     * @param context 上下文对象
     * @return 是否锁屏
     */
    public static boolean isKeyguardLocked(Context context) {
        KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        return km != null && km.isKeyguardLocked();
    }

    /**
     * 把后台的activity移动到前台
     *
     * @param context 上下文对象
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static void moveToFront(Context context) {
        //修复Buggly上出现的#252064 java.lang.NoSuchFieldError问题
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return;
        }
        final ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null) {
            List<ActivityManager.AppTask> appTasks = activityManager.getAppTasks();
            if (CommonUtil.isListNotEmpty(appTasks)) {
                for (int i = 0; i < appTasks.size(); i++) {
                    ActivityManager.RecentTaskInfo taskInfo = appTasks.get(i).getTaskInfo();
                    if (taskInfo.baseActivity != null && taskInfo.baseActivity.toShortString().contains("com.yeastar.linkus")) {
                        LogUtil.w("App moveToFront");
                        appTasks.get(i).moveToFront();
                    }
                }
            }
        }
    }

    public static boolean isDarkMode(Context context) {
        int mode = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return mode == Configuration.UI_MODE_NIGHT_YES;
    }

    public static boolean isXiaomi() {
        return Build.MANUFACTURER.equals("Xiaomi");
    }

    public static boolean isVivo() {
        return Build.MANUFACTURER.equals("vivo");
    }

    public static boolean isShowBannerNotification() {
        return (isXiaomi() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                || (isVivo() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q);
    }

    /**
     * 判断是否在主进程
     *
     * @param context 上下文对象
     * @return 是否在主进程
     */
    public static boolean isMainProcesses(Context context) {
        return isThatProcesses(context, context.getPackageName());
    }

    private static boolean isThatProcesses(Context context, String thatProcess) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> processInfoList = null;
        if (am != null) {
            processInfoList = am.getRunningAppProcesses();
        }
        if (processInfoList == null) {
            return false;
        }
        for (ActivityManager.RunningAppProcessInfo processInfo : processInfoList) {
            if (processInfo.pid == Process.myPid() && processInfo.processName.equals(thatProcess)) {
                return true;
            }
        }
        return false;
    }

}
