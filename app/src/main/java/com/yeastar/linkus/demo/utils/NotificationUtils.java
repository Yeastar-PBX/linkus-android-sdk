package com.yeastar.linkus.demo.utils;

import static android.content.Context.NOTIFICATION_SERVICE;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.service.notification.StatusBarNotification;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.yeastar.linkus.demo.Constant;
import com.yeastar.linkus.demo.R;
import com.yeastar.linkus.service.log.LogUtil;

/**
 * Created by ted on 17-11-29.
 */

public class NotificationUtils {

    private static String sApplicationName;

    private static String getApplicationName(Context mContext) {
        Context context = mContext.getApplicationContext();
        if (sApplicationName == null) {
            PackageManager pm;
            ApplicationInfo ai;
            try {
                pm = context.getPackageManager();
                ai = pm.getApplicationInfo(context.getPackageName(), 0);
                sApplicationName = pm.getApplicationLabel(ai).toString();
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                sApplicationName = context.getPackageName();
            }
        }
        return sApplicationName;
    }

    private static NotificationManager notificationManager;


    private static NotificationManager getNotificationManager(Context context) {
        if (notificationManager == null) {
            notificationManager = (NotificationManager) context.getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
        }
        return notificationManager;
    }

    public static void cancelAllNotification(Context context) {
        LogUtil.w("NotificationUtils cancelAllNotification");
        NotificationManager notificationManager = getNotificationManager(context.getApplicationContext());
        if (notificationManager != null) {
            notificationManager.cancelAll();
        }
    }

    public static void cancelNotificationById(Context context, int notificationId) {
        NotificationManager notificationManager = getNotificationManager(context.getApplicationContext());
        if (notificationManager != null) {
            notificationManager.cancel(notificationId);
        }
    }

    public static void sendPushNotification(Context mContext, PendingIntent pendingIntent, int notificationId, String title, String message, String group, int groupId) {
        Context context = mContext.getApplicationContext();
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, Constant.NOTIFICATION_CHANNEL_PUSH_ID)
                .setSmallIcon(R.drawable.notifycation_100)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(soundUri)
                .setContentIntent(pendingIntent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            notificationBuilder.setGroup(group);
        }
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(notificationId, notificationBuilder.build());
        }
        sendGroupNotification(context, group, groupId, title);
    }

    public static void sendNewCallNotification(Context context, PendingIntent pendingIntent, String title, String message, boolean isFullScreen) {
        Notification notification = getNewCallNotification(context, pendingIntent, title, message, isFullScreen);
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(Constant.NEW_CALL_NOTIFICATION_ID, notification);
        }
    }

    private static Notification getNewCallNotification(Context context, PendingIntent pendingIntent, String title, String message, boolean isFullScreen) {
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, Constant.NOTIFICATION_CHANNEL_NEW_CALL_ID)
                .setSmallIcon(R.drawable.notifycation_100)
                .setContentTitle(title)
                .setCategory(Notification.CATEGORY_CALL)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setAutoCancel(true)
                .setSound(soundUri);
        notificationBuilder.setContentIntent(pendingIntent);
        if (isFullScreen) {//兼容某些版本的miui对单纯的设置全屏intent不生效的bug
            notificationBuilder.setFullScreenIntent(pendingIntent, true);
        }
        return notificationBuilder.build();
    }

    public static Notification getMicroPhoneNotification(Context context, PendingIntent pendingIntent, String title) {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, Constant.NOTIFICATION_CHANNEL_NEW_CALL_ID)
                .setSmallIcon(R.drawable.notifycation_100)
                .setContentTitle(title)
                .setCategory(Notification.CATEGORY_CALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setSound(null);
        notificationBuilder.setContentIntent(pendingIntent);
        return notificationBuilder.build();
    }

    private static void sendGroupNotification(Context context, String group, int groupId, String title) {
        NotificationManager notificationManager = getNotificationManager(context.getApplicationContext());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && notificationManager != null) {
            int notificationNum = getNumberOfNotifications(notificationManager, group);
            if (notificationNum > 1) {
                //将通知添加/更新归类到同一组下面
                Notification notification = new NotificationCompat.Builder(context.getApplicationContext(), Constant.NOTIFICATION_CHANNEL_PUSH_ID)
                        .setContentTitle(title)
                        .setContentText(title + " +" + notificationNum)
                        .setSmallIcon(R.drawable.notifycation_100)
                        //添加富样式到通知的显示样式中，如果当前系统版本不支持，那么将不起作用，依旧用原来的通知样式
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .setSummaryText(title + " +" + notificationNum))
                        .setGroup(group) //设置类组key，说明此条通知归属于哪一个归类
                        .setGroupSummary(true)//这句话必须和上面那句一起调用，否则不起作用
                        .build();
                notificationManager.notify(groupId, notification);
            }
        }
    }

    /**
     * 获取当前状态栏具有统一id的通知的数量
     *
     * @return 数量
     */
    @RequiresApi(Build.VERSION_CODES.M)
    private static int getNumberOfNotifications(NotificationManager notificationManager, String group) {
        int notificationsNum = 0;
        StatusBarNotification[] activeNotifications = notificationManager
                .getActiveNotifications();

        //获取当前通知栏里头，NOTIFICATION_GROUP_SUMMARY_ID归类id的组别
        //因为发送分组的通知也算一条通知，所以需要-1
        for (StatusBarNotification statusBarNotification : activeNotifications) {
            Notification notification = statusBarNotification.getNotification();
            if (group.equals(notification.getGroup()) && statusBarNotification.getId() > Constant.RE_USER_LOGIN_GROUP_ID) {
                notificationsNum++;
            }
        }
        return notificationsNum;
    }

    @TargetApi(Build.VERSION_CODES.O)
    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                NotificationChannel pushMsgChannel = notificationManager.getNotificationChannel(Constant.NOTIFICATION_CHANNEL_PUSH_ID);
                String pushName = context.getString(R.string.Phonesetting_notification_push);
                if (pushMsgChannel != null) {
                    pushMsgChannel.setName(pushName);
                } else {
                    pushMsgChannel = new NotificationChannel(Constant.NOTIFICATION_CHANNEL_PUSH_ID, pushName, NotificationManager.IMPORTANCE_HIGH);
                }
                NotificationChannel newCallChannel = notificationManager.getNotificationChannel(Constant.NOTIFICATION_CHANNEL_NEW_CALL_ID);
                String newCallName = context.getString(R.string.Phonesetting_notification_callinfo);
                if (newCallChannel != null) {
                    newCallChannel.setName(newCallName);
                } else {
                    newCallChannel = new NotificationChannel(Constant.NOTIFICATION_CHANNEL_NEW_CALL_ID, newCallName, NotificationManager.IMPORTANCE_HIGH);
                }
                newCallChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                notificationManager.createNotificationChannel(pushMsgChannel);
                notificationManager.createNotificationChannel(newCallChannel);
            }
        }
    }

    //调用该方法获取是否开启通知栏权限
    public static boolean isNotifyEnabled(Context context) {
        return NotificationManagerCompat.from(context).areNotificationsEnabled();
    }

    /**
     * 跳转到通知权限申请页面
     *
     * @param context
     */
    public static void requestNotificationPermission(Context context) {
        Intent localIntent = new Intent();
        //直接跳转到应用通知设置的代码：
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {//8.0及以上
            localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            localIntent.setData(Uri.fromParts("package", context.getPackageName(), null));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {//5.0以上到8.0以下
            localIntent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
            localIntent.putExtra("app_package", context.getPackageName());
            localIntent.putExtra("app_uid", context.getApplicationInfo().uid);
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {//4.4
            localIntent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            localIntent.addCategory(Intent.CATEGORY_DEFAULT);
            localIntent.setData(Uri.parse("package:" + context.getPackageName()));
        } else {
            //4.4以下没有从app跳转到应用通知设置页面的Action，可考虑跳转到应用详情页面,
            localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (Build.VERSION.SDK_INT >= 9) {
                localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                localIntent.setData(Uri.fromParts("package", context.getPackageName(), null));
            } else if (Build.VERSION.SDK_INT <= 8) {
                localIntent.setAction(Intent.ACTION_VIEW);
                localIntent.setClassName("com.android.settings", "com.android.setting.InstalledAppDetails");
                localIntent.putExtra("com.android.settings.ApplicationPkgName", context.getPackageName());
            }
        }
        context.startActivity(localIntent);
    }

}
